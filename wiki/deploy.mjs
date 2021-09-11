import {existsSync} from 'https://deno.land/std/fs/mod.ts';
import {default as md, html} from 'https://lambdaurora.dev/lib.md/lib/index.mjs';

const WEBSITE = "https://lambdaurora.dev/";
const WEBSITE_PREFIX = WEBSITE + "AurorasDecorations/";

console.log('Creating deploy directory.');
if (existsSync('deploy_out'))
    await Deno.remove('deploy_out', {recursive: true});
await Deno.mkdir('deploy_out');

console.log('Deploying...');

await deploy_dir('.');
await deploy_dir('../images');

function deploy_path(path) {
    if (path.startsWith('./public'))
        return path.replace(/^\.\/public/, './deploy_out');
    else
        return path.replace(/^\.\.?/, './deploy_out')
}

async function deploy_dir(path, level = 0) {
    console.log("  ".repeat(level) + `Deploying "${path}"...`);

    const deploy_dir_path = deploy_path(path);
    if (!existsSync(deploy_dir_path))
        await Deno.mkdir(deploy_dir_path);

    for await (const dir_entry of Deno.readDir(path)) {
        if (dir_entry.isFile && !path.startsWith('./public') && dir_entry.name.endsWith('.md')) {
            console.log(" ".repeat(level) + `  Loading ${dir_entry.name}...`);
            await deploy_markdown(path + '/' + dir_entry.name);
        } else if (dir_entry.isDirectory && dir_entry.name !== 'deploy_out') {
            await deploy_dir(path + '/' + dir_entry.name, level + 1);
        } else if (path.startsWith('./public') || path.startsWith('../images')) {
            console.log(" ".repeat(level) + `  Copying public file ${path + '/' + dir_entry.name}...`);
            await Deno.copyFile(path + '/' + dir_entry.name, deploy_path(path + '/' + dir_entry.name));
        }
    }
}

async function deploy_markdown(path) {
    const decoder = new TextDecoder('utf-8');
    const content = decoder.decode(await Deno.readFile(path));
    let doc = md.parser.parse(content);

    let page_description = "Welcome to the Aurora's Decorations wiki.";
    let page_thumbnail;

    function get_thumbnail_meta_tag() {
        if (page_thumbnail) {
            return `<meta property="og:image" content="${WEBSITE_PREFIX + page_thumbnail}"/>`;
        }
        return '';
    }

    let main = html.create_element('main');
    main.children = md.render_to_html(doc, {image: {class_name: "responsive-img"}, spoiler: {enable: true}, parent: main}).children
        .filter(node => {
            if (node instanceof html.Comment) {
                if (node.content.startsWith('description:')) {
                    page_description = node.content.substr('description:'.length);
                    return false;
                } else if (node.content.startsWith('thumbnail:')) {
                    page_thumbnail = node.content.substr('thumbnail:'.length);
                    return false;
                }
            }

            return true;
        });

    fix_links_in_html(main.children);
    console.log(JSON.stringify(main.toJSON(), null, '  '));

    let page = `<!DOCTYPE html>
<html lang="en">
  <head>
    <meta charset="utf-8" />

    <title>${get_markdown_title(doc)}</title>

    <meta property="og:type" content="website">
    <meta property="og:title" content="${get_markdown_title(doc)}">
    <meta property="og:site_name" content="Aurora's Decorations">
    <meta property="og:url" content="${WEBSITE_PREFIX + path.substr(2).replace(/\.md$/, '.html')}">
    <meta property="og:description" content="${page_description}">
    ${get_thumbnail_meta_tag()}

    <link rel="stylesheet" type="text/css" href="https://lambdaurora.dev/style.css" />
    <link rel="stylesheet" type="text/css" href="style.css" />

    <!--Let browser know website is optimized for mobile-->
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
  </head>
  <body>
    <main>
      ${main.inner_html()}
    </main>
    <footer class="ls-app-footer">
      <div class="ls-app-footer-license">
        <div class="right ls-subtitle2">
          Except where otherwise noted, content on this site is licensed under a <a rel="license" href="http://creativecommons.org/licenses/by/4.0/">CC-BY 4.0 International License</a>.
        </div>
      </div>
    </footer>
  </body>
</html>
`;

    const encoder = new TextEncoder();
    await Deno.writeFile(deploy_path(path.replace(/\.md$/, '.html')), encoder.encode(page));
}

function fix_links_in_html(nodes) {
    for (let node of nodes) {
        if (node instanceof html.Element) {
            for (let attr of node.attributes) {
                if (attr.name === 'href' || attr.name === 'src') {
                    let value = attr.value();
                    if (value.startsWith('../'))
                        node.attr(attr.name, value.substr(3));
                    if (value.endsWith('.md'))
                        node.attr(attr.name, value.replace(/\.md$/, '.html'));
                }
            }

            fix_links_in_html(node.children);
        }
    }
}

function get_markdown_title(doc) {
    for (const node of doc.blocks) {
        if (node instanceof md.Heading && node.level === 'h1') {
            let title = node.toString().substr(2);
            if (title.startsWith("Aurora's Decorations - "))
                return title;
            return `Aurora's Decorations - ${title}`;
        }
    }
    return "Aurora's Decorations";
}
