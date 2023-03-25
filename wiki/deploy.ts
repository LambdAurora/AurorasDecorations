import {html, HTML_TAGS_TO_PURGE_SUGGESTION, md} from "https://deno.land/x/libmd@v1.9.0/mod.mjs";

const WEBSITE = "https://lambdaurora.dev/";
const WEBSITE_PREFIX = WEBSITE + "AurorasDecorations/";
const TEXTURES_PATH = "../src/main/resources/assets/aurorasdeco/textures";
const ASSETS_PATH_REGEX = /..\/src\/main\/resources\/assets\/aurorasdeco\/textures\/((?:[a-z_]+\/)+[a-z_]+\.png)/;
const root = Deno.args[0] ? Deno.args[0] : "";

console.log("Creating deploy directory.");
try {
	await Deno.remove("deploy_out", {recursive: true});
} catch (e) {
	if (!(e instanceof Deno.errors.NotFound)) {
		throw e;
	}
}
await Deno.mkdir("deploy_out");

console.log("Deploying...");

interface MarkdownPage {
	path: string;
	title: string;
	raw_title: string;
	nav: NavigationData[];
	main: html.Element;
	description: string;
	thumbnail_meta: string;
}

type MarkdownPages = { [x: string]: MarkdownPage };

const markdown_pages: MarkdownPages = {};
const assets_to_copy: { [x: string]: string } = {};
await deploy_dir(".", path => path.startsWith("./public"), markdown_pages, assets_to_copy);
await deploy_dir("../images", path => path.startsWith("../images"));
await deploy_dir(TEXTURES_PATH, path => assets_to_copy[path.substring(TEXTURES_PATH.length + 1)] !== undefined);

Deno.copyFile("../src/main/resources/assets/aurorasdeco/icon.png", "./deploy_out/icon.png").then(() => console.log("Copied icon."));

for (const page of Object.values(markdown_pages)) {
	await deploy_markdown(markdown_pages, page);
}

function deploy_path(path: string) {
	if (path.startsWith("./public"))
		return path.replace(/^\.\/public/, "./deploy_out");
	else if (path.startsWith(TEXTURES_PATH))
		return path.replace(TEXTURES_PATH, "./deploy_out/images/assets");
	else
		return path.replace(/^\.\.?/, "./deploy_out")
}

async function deploy_dir(path: string, filter = (_: string) => true, markdown_pages: MarkdownPages = {}, assets_to_copy: { [x: string]: string } = {},
						  level = 0) {
	console.log("  ".repeat(level) + `Deploying "${path}"...`);

	const deploy_dir_path = deploy_path(path);
	await Deno.mkdir(deploy_dir_path, {recursive: true});

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

async function load_markdown(path: string, assets_to_copy: { [x: string]: string }): Promise<MarkdownPage> {
	const decoder = new TextDecoder("utf-8");
	const content = decoder.decode(await Deno.readFile(path));
	const doc = md.parser.parse(content, {
		inline_html: {
			disallowed_tags: HTML_TAGS_TO_PURGE_SUGGESTION.filter(tag => tag !== "svg")
		}
	});

	let page_description = "Welcome to the Aurora's Decorations wiki. Aurora's Decorations is a decorations-focused mod.";
	let page_thumbnail: string;

	function get_thumbnail_meta_tag() {
		if (page_thumbnail) {
			return `<meta property="og:image" content="${WEBSITE_PREFIX + page_thumbnail}"/>`;
		}
		return "";
	}

	const main = html.create_element("main") as html.Element;
	main.children = md.render_to_html(doc, {
		code: {
			process: (el: md.InlineCode) => {
				if (el.content.match(/^#[a-fA-F\d]{3}(?:[a-fA-F\d]{5}|[a-fA-F\d]{3})?$/)) {
					return html.create_element("span")
						.with_attr("class", "ls_color_ship")
						.with_child(html.create_element("span")
							.with_attr("style", `background-color: ${el.content};`)
						);
				} else {
					return el.as_html();
				}
			}
		},
		image: {
			class_name: "ls_responsive_img"
		},
		inline_html: {
			disallowed_tags: HTML_TAGS_TO_PURGE_SUGGESTION.filter(tag => tag !== "svg")
		},
		spoiler: {
			enable: true
		},
		table: {
			process: (table: html.Element) => table.with_attr("class", "ls_grid_table")
		},
		parent: main
	}).children.filter(node => {
		if (node instanceof html.Comment) {
			if (node.content?.startsWith("description:")) {
				page_description = node.content.substring("description:".length);
				return false;
			} else if (node.content?.startsWith("thumbnail:")) {
				page_thumbnail = node.content.substring("thumbnail:".length);
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
		nav: build_navigation_data(main),
		main: main,
		description: page_description,
		thumbnail_meta: get_thumbnail_meta_tag()
	};
}

async function deploy_markdown(markdown_pages: MarkdownPages, page_data: MarkdownPage) {
	console.log(`Writing ${page_data.path}...`);

	page_data.main.children = page_data.main.children
		.map(node => {
			if (node instanceof html.Comment) {
				if (node.content?.startsWith("include:")) {
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
									const current_heading = parseInt(included_node.tag.name[1]);
									const offset = parseInt(include[1]);

									included_node.tag = (html.Tag as { [x: string]: unknown })["h" + Math.min(current_heading + offset, 6)];
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
		<meta property="og:url" content="${WEBSITE_PREFIX + page_data.path.substring(2)}">
		<meta property="og:description" content="${page_data.description}">
		${page_data.thumbnail_meta}

		<link rel="icon" href="${relativize_from_root(page_data.path)}icon.png" />

		<link rel="stylesheet" type="text/css" href="https://lambdaurora.dev/style.css" />
		<link rel="stylesheet" type="text/css" href="${relativize_from_root(page_data.path)}style.css" />

		<!--Let browser know website is optimized for mobile-->
		<meta name="viewport" content="width=device-width, initial-scale=1.0" />
	</head>
	<body class="wiki_page">
		<div class="ls_navbar ls_fixed ls_show_on_small_only" ls_side="top">
			<div class="ls_nav_trigger_btn_wrapper">
				<label for="main_nav_trigger" class="ls_btn ls_nav_trigger_btn" ls_variant="icon" aria-role="menu" aria-label="Menu" aria-description="Open the navigation menu.">
					<svg width="40" height="40" viewBox="0 0 40 40" stroke="var(--ls_theme_on_primary)" stroke-width="2px" shape-rendering="crispedges">
						<line x1="12" y1="14" x2="28" y2="14"></line><line x1="12" y1="20" x2="28" y2="20"></line><line x1="12" y1="26" x2="28" y2="26"></line>
					</svg>
				</label>
			</div>
			<span class="ls_navbar_title">Aurora's Decorations</span>
		</div>
		<div>
			<input type="checkbox" id="main_nav_trigger" class="ls_sidenav_internal_trigger" aria-hidden="true">
			<nav id="main_nav" class="ls_sidenav">
				<a class="ls_nav_banner" href="${relativize_from_root(page_data.path)}">
					<img class="mod_icon ls_pixelated" src="${relativize_from_root(page_data.path)}icon.png" alt="Aurora's Decorations Icon">
					<span>Aurora's Decorations</span>
				</a>
				${build_navigation(markdown_pages, page_data).html()}
			</nav>
			<label for="main_nav_trigger" class="ls_sidenav_darkened"></label>
		</div>
		<div class="wiki_content ls_sidenav_neighbor">
			<main>
				<article>
					${page_data.main.inner_html()}
				</article>
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
		</div>

		<script src="${relativize_from_root(page_data.path)}script.js"></script>
	</body>
</html>
`;

	const encoder = new TextEncoder();
	await Deno.writeFile(deploy_path(page_data.path), encoder.encode(page));
}

function fix_links_in_html(nodes: html.Node[], assets_to_copy: { [x: string]: string }) {
	for (const node of nodes) {
		if (node instanceof html.Element) {
			for (const attr of node.attributes) {
				if (attr.name === "href" || attr.name === "src") {
					let value = attr.value();

					let result;
					if ((result = ASSETS_PATH_REGEX.exec(value))) {
						assets_to_copy[result[1]] = "../src/main/resources/assets/aurorasdeco/textures/" + result[1];
						node.attr(attr.name, value.replace(TEXTURES_PATH, 'images/assets'));
						continue;
					}

					if (value.startsWith("../") && !value.includes(".md"))
						value = node.attr(attr.name, value.substring(3)).value();
					if (value.includes('.md'))
						node.attr(attr.name, value.replace(/\.md/, ".html"));
				}
			}

			fix_links_in_html(node.children, assets_to_copy);
		}
	}
}

function get_raw_markdown_title(doc: md.MDDocument) {
	for (const node of doc.blocks) {
		if (node instanceof md.Heading && node.level === "h1") {
			return node.toString().substring(2);
		}
	}
	return "Aurora's Decorations";
}

function get_markdown_title(title: string) {
	if (title.startsWith("Aurora's Decorations"))
		return title;
	else
		return `Aurora's Decorations - ${title}`;
}

function relativize_from_root(path: string) {
	const result = "../".repeat(path.split("/").length - 2);
	if (result.length === 0) return "./";
	return result;
}

interface NavigationData {
	content: html.Node[];
	id: string;
	children: NavigationData[];
}

function build_navigation_data(html: html.Element, start = 0, level = 1) {
	let data: NavigationData[] = [];

	for (let i = start; i < html.children.length; i++) {
		const child = html.children[i] as html.Element;
		if (child.tag && child.tag.name.startsWith("h")) {
			const current_level = parseInt(child.tag.name[1]);
			if (current_level > 0) {
				if (current_level < level)
					break;
				else if (current_level > level && data.length === 0) {
					const child_data = build_navigation_data(html, i, current_level);
					if (data.length === 0) {
						data = child_data;
					}
				} else if (current_level === level)
					data.push({content: child.children, id: child.attr("id").value(), children: build_navigation_data(html, i + 1, level + 1)});
			}
		}
	}

	return data;
}

interface NavigationEntry {
	type: string;
	raw_title: string;
}

interface PageEntry extends NavigationEntry, MarkdownPage {
	type: "page";
}

interface DirEntry extends NavigationEntry {
	type: "dir";
	path: string;
	full_path: string[];
	entries: NavigationEntry[];
}

function build_navigation(pages: MarkdownPages, current_page: MarkdownPage) {
	const list = html.create_element("ul");
	const index_page = pages["./index.md"];

	index_page.nav[0].content = ["Main page"];

	const current_path = current_page.path.split("/");

	function build_tree(page: MarkdownPage, elements: NavigationData, first: boolean) {
		let path = root + page.path.replace(/^\.\//, "/").replace(/index\.html$/, "");
		if (!first) {
			path += "#" + elements.id;
		}

		const link = html.create_element("div")
			.with_child(html.create_element("a").with_attr("href", path));
		const tree = html.create_element("li").with_child(link);

		if (first && page.path === current_page.path) {
			tree.style("background-color", "rgba(0, 0, 0, 0.1)");
			tree.attr("open", "");
		}

		elements.content.forEach(child => link.children[0].append_child(child));

		if (elements.children.length > 0) {
			const subtree = html.create_element("ul");
			if (first) tree.attr("class", "wiki_nav_directory ls_nav_dir_entry");

			elements.children.forEach(item => subtree.append_child(build_tree(page, item, false)));

			tree.append_child(subtree);
		}

		return tree;
	}

	function find_or_append_directory(entries: NavigationEntry[], path: string[], level = 1): DirEntry {
		let result;
		if ((result = entries.find(entry => entry.type === "dir" && (entry as DirEntry).path === path[level]))) {
			if (path.length > level + 2) {
				return find_or_append_directory((result as DirEntry).entries, path, level + 1);
			}
			return result as DirEntry;
		} else {
			const entry: DirEntry = {
				type: "dir",
				path: path[level],
				full_path: path.filter((_, index) => index <= level),
				raw_title: path[level]
					.replace(/\w/, firstLetter => firstLetter.toUpperCase())
					.replace(/_\w/g, wordFirstLetter => " " + wordFirstLetter[1].toUpperCase()),
				entries: []
			};
			entries.push(entry);

			if (level !== path.length - 2) {
				return find_or_append_directory(entry.entries, path, level + 1);
			}

			return entry;
		}
	}

	list.append_child(build_tree(index_page, index_page.nav[0], true));

	const raw_entries = Object.entries(pages)
		.filter(([path, _]) => path !== "./index.md")
		.map(([path, page]) => [path.split("/"), page]);
	const entries: NavigationEntry[] = [];

	for (const [p, page] of raw_entries) {
		const path = p as string[];
		if (path.length > 2) {
			find_or_append_directory(entries, path).entries.push({type: "page", ...page} as PageEntry);
		} else {
			entries.push({type: "page", ...page} as PageEntry);
		}
	}

	function should_open_dir(entry: DirEntry) {
		for (let i = 0; i < entry.full_path.length; i++) {
			if (entry.full_path[i] !== current_path[i])
				return false;
		}

		return true;
	}

	function build_navigational_tree(tree: html.Element, entry: NavigationEntry) {
		if (entry.type === "dir") {
			const dir_entry = entry as DirEntry;
			const subtree = html.create_element("ul");
			for (const item of dir_entry.entries)
				build_navigational_tree(subtree, item);

			const li = html.create_element("li").with_attr("class", "wiki_nav_directory ls_nav_dir_entry")
				.with_child(dir_entry.raw_title)
				.with_child(subtree);

			if (should_open_dir(dir_entry)) {
				li.attr("open", "");
			}

			tree.append_child(li);
		} else {
			for (const h1 of (entry as PageEntry).nav) {
				tree.append_child(build_tree(entry as PageEntry, h1, true));
			}
		}
	}

	(function sort_entries(entries: NavigationEntry[]) {
		const sorted = entries.sort((page1, page2) => page1.raw_title.localeCompare(page2.raw_title));
		sorted.forEach(entry => {
			if (entry.type === "dir") {
				const dir_entry = entry as DirEntry;
				dir_entry.entries = sort_entries(dir_entry.entries);
			}
		})
		return sorted;
	})(entries)
		.forEach(entry => build_navigational_tree(list, entry));

	return html.create_element("nav").with_child(list);
}
