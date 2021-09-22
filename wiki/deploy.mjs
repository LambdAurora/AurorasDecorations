import {existsSync} from "https://deno.land/std/fs/mod.ts";
import {default as md, html} from "https://lambdaurora.dev/lib.md/lib/index.mjs";

const WEBSITE = "https://lambdaurora.dev/";
const WEBSITE_PREFIX = WEBSITE + "AurorasDecorations/";
const TEXTURES_PATH = "../src/main/resources/assets/aurorasdeco/textures";
const ASSETS_PATH_REGEX = /..\/src\/main\/resources\/assets\/aurorasdeco\/textures\/((?:[a-z_]+\/)+[a-z_]+\.png)/;

console.log("Creating deploy directory.");
if (existsSync("deploy_out"))
    await Deno.remove("deploy_out", {recursive: true});
await Deno.mkdir("deploy_out");

console.log("Deploying...");

let markdown_pages = {};
let assets_to_copy = {};
await deploy_dir(".", path => path.startsWith("./public"), markdown_pages, assets_to_copy);
await deploy_dir("../images", path => path.startsWith("../images"));
await deploy_dir(TEXTURES_PATH, path =>  assets_to_copy[path.substr(TEXTURES_PATH.length + 1)] !== undefined);

for (const page of Object.values(markdown_pages)) {
    await deploy_markdown(markdown_pages, page);
}

function deploy_path(path) {
    if (path.startsWith("./public"))
        return path.replace(/^\.\/public/, "./deploy_out");
    else if (path.startsWith(TEXTURES_PATH))
        return path.replace(TEXTURES_PATH, "./deploy_out/images");
    else
        return path.replace(/^\.\.?/, "./deploy_out")
}

async function deploy_dir(path, filter = _ => true, markdown_pages = {}, assets_to_copy = {}, level = 0) {
    console.log("  ".repeat(level) + `Deploying "${path}"...`);

    const deploy_dir_path = deploy_path(path);
    if (!existsSync(deploy_dir_path))
        await Deno.mkdir(deploy_dir_path);

    for await (const dir_entry of Deno.readDir(path)) {
        if (dir_entry.isFile && !path.startsWith("./public") && !path.startsWith("../images") && dir_entry.name.endsWith(".md")) {
            console.log(" ".repeat(level) + `  Loading ${dir_entry.name}...`);
            const markdown_path = path + "/" + dir_entry.name;
            markdown_pages[markdown_path] = await load_markdown(markdown_path, assets_to_copy);
        } else if (dir_entry.isDirectory && dir_entry.name !== "deploy_out") {
            await deploy_dir(path + "/" + dir_entry.name, filter, markdown_pages, assets_to_copy, level + 1);
        } else if (filter(path + "/" + dir_entry.name)) {
            console.log(" ".repeat(level) + `  Copying public file ${path + "/" + dir_entry.name}...`);
            await Deno.copyFile(path + "/" + dir_entry.name, deploy_path(path + "/" + dir_entry.name));
        }
    }
}

async function load_markdown(path, assets_to_copy) {
    const decoder = new TextDecoder("utf-8");
    const content = decoder.decode(await Deno.readFile(path));
    const doc = md.parser.parse(content);

    let page_description = "Welcome to the Aurora's Decorations wiki.";
    let page_thumbnail;

    function get_thumbnail_meta_tag() {
        if (page_thumbnail) {
            return `<meta property="og:image" content="${WEBSITE_PREFIX + page_thumbnail}"/>`;
        }
        return "";
    }

    const main = html.create_element("main");
    main.children = md.render_to_html(doc, {image: {class_name: "ls_responsive_img"}, spoiler: {enable: true}, parent: main}).children
        .filter(node => {
            if (node instanceof html.Comment) {
                if (node.content.startsWith("description:")) {
                    page_description = node.content.substr("description:".length);
                    return false;
                } else if (node.content.startsWith("thumbnail:")) {
                    page_thumbnail = node.content.substr("thumbnail:".length);
                    return false;
                }
            }

            return true;
        });

    fix_links_in_html(main.children, assets_to_copy);

    const raw_title = get_raw_markdown_title(doc);
    return {
        path: path.replace(/\.md$/, ".html"),
        title: get_markdown_title(raw_title),
        raw_title: raw_title,
        main: main,
        description: page_description,
        thumbnail_meta: get_thumbnail_meta_tag()
    };
}

async function deploy_markdown(markdown_pages, page_data) {
    console.log(`Writing ${page_data.path}...`);

    page_data.main.children = page_data.main.children
        .map(node => {
            if (node instanceof html.Comment) {
                if (node.content.startsWith("include:")) {
                    const include = node.content.split(":");

                    if (markdown_pages[include[2]]) {
                        const include_page_data = markdown_pages[include[2]];

                        const div = html.create_element("details")
                            .with_child(html.create_element("summary")
                                .with_child("Related information from the page ")
                                .with_child(html.create_element("a").with_attr("href", include_page_data.path)
                                    .with_child(include_page_data.raw_title))
                                .with_child("."));

                        div.children = div.children.concat(include_page_data.main.children.map(included_node => {
                            if (included_node instanceof html.Element) {
                                if (included_node.tag.name.match(/^h[1-6]$/)) {
                                    let current_heading = parseInt(included_node.tag.name[1]);
                                    let offset = parseInt(include[1]);

                                    included_node.tag = html.Tag["h" + Math.min(current_heading + offset, 6)];
                                }
                            }

                            return included_node;
                        }));
                        return div;
                    } else {
                        return html.create_element("p").with_child(`Failed to include page "${include[2]}".`);
                    }
                }
            }

            return node;
        });

    const page = `<!DOCTYPE html>
<html lang="en">
  <head>
    <meta charset="utf-8" />

    <title>${page_data.title}</title>

    <meta property="og:type" content="website">
    <meta property="og:title" content="${page_data.title}">
    <meta property="og:site_name" content="Aurora's Decorations">
    <meta property="og:url" content="${WEBSITE_PREFIX + page_data.path.substr(2)}">
    <meta property="og:description" content="${page_data.description}">
    ${page_data.thumbnail_meta}

    <link rel="stylesheet" type="text/css" href="https://lambdaurora.dev/style.css" />
    <link rel="stylesheet" type="text/css" href="${relativize_from_root(page_data.path)}style.css" />

    <!--Let browser know website is optimized for mobile-->
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
  </head>
  <body>
    <main>
      ${page_data.main.inner_html()}
    </main>
    <footer class="ls_app_footer">
      <div class="ls_app_footer_license">
        <span>
          Hosted on <a href="https://pages.github.com">GitHub Pages</a>.
        </span>
        <span>
          Except where otherwise noted, content on this site is licensed under a <a rel="license" href="http://creativecommons.org/licenses/by/4.0/">CC-BY 4.0 International License</a>.
        </span>
      </div>
    </footer>
  </body>
</html>
`;

    const encoder = new TextEncoder();
    await Deno.writeFile(deploy_path(page_data.path), encoder.encode(page));
}

function fix_links_in_html(nodes, assets_to_copy) {
    for (const node of nodes) {
        if (node instanceof html.Element) {
            for (const attr of node.attributes) {
                if (attr.name === "href" || attr.name === "src") {
                    let value = attr.value();

                    let result;
                    if ((result = ASSETS_PATH_REGEX.exec(value))) {
                        assets_to_copy[result[1]] = "../src/main/resources/assets/aurorasdeco/textures/" + result[1];
                        node.attr(attr.name, value.replace(TEXTURES_PATH, 'images'));
                        continue;
                    }

                    if (value.startsWith("../"))
                        value = node.attr(attr.name, value.substr(3)).value();
                    if (value.includes('.md'))
                        node.attr(attr.name, value.replace(/\.md/, ".html"));
                }
            }

            fix_links_in_html(node.children, assets_to_copy);
        }
    }
}

function get_raw_markdown_title(doc) {
    for (const node of doc.blocks) {
        if (node instanceof md.Heading && node.level === "h1") {
            return node.toString().substr(2);
        }
    }
    return "Aurora's Decorations";
}

function get_markdown_title(title) {
    if (title.startsWith("Aurora's Decorations"))
        return title;
    else
        return `Aurora's Decorations - ${title}`;
}

function relativize_from_root(path) {
    return "../".repeat(path.split("/").length - 2);
}
