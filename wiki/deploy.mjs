import {existsSync} from 'https://deno.land/std/fs/mod.ts';
import {default as md, html} from 'https://lambdaurora.dev/lib.md/lib/index.mjs';

console.log('Creating deploy directory.');
if (existsSync('deploy_out'))
    await Deno.remove('deploy_out', {recursive: true});
await Deno.mkdir('deploy_out');

console.log('Deploying...');

await deploy_dir('.');
await deploy_dir('../images');

function deploy_path(path) {
    if (path === './public')
        return './deploy_out';
    else
        return path.replace(/^\.\.?/, './deploy_out')
}

async function deploy_dir(path, level = 0) {
    console.log(" ".repeat(level) + `Deploying "${path}"...`);

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
    fix_links(doc.blocks);

    let html_doc = html.create_element('html');
    html_doc.attr('lang', 'en');
    let head = html.create_element('head');
    {
        let charset_meta = html.create_element('meta');
        charset_meta.attr('charset', 'utf-8');
        head.append_child(charset_meta);

        let title = html.create_element('title');
        title.append_child(new html.Text(get_markdown_title(doc)));
        head.append_child(title);

        let stylesheet_meta = html.create_element('link');
        stylesheet_meta.attr('rel', 'stylesheet');
        stylesheet_meta.attr('type', 'text/css');
        stylesheet_meta.attr('href', 'https://lambdaurora.dev/style.css');
        head.append_child(stylesheet_meta);

        let style = html.create_element('style');
        style.append_child(new html.Text('.responsive-img {\n' +
            '    max-width: 100%;\n' +
            '    height: auto;\n' +
            '}'));
        head.append_child(style);

        head.append_child(new html.Comment('Let browser know website is optimized for mobile'));
        let viewport_meta = html.create_element('meta');
        viewport_meta.attr('name', 'viewport');
        viewport_meta.attr('content', 'width=device-width, initial-scale=1.0');
        head.append_child(viewport_meta);
    }
    html_doc.append_child(head);

    let body = html.create_element('body');
    html_doc.append_child(body);
    let main = html.create_element('main');
    body.append_child(main);
    main.children = md.render_to_html(doc, {image: {class_name: "responsive-img"}, spoiler: {enable: true}, parent: main}).children;

    const encoder = new TextEncoder();
    await Deno.writeFile(deploy_path(path.replace(/\.md$/, '.html')), encoder.encode('<!DOCTYPE html>\n' + html_doc.html()));
}

function fix_links(nodes) {
    for (let node of nodes) {
        if (node instanceof md.Link) {
            if (node.ref_name === '' && node.ref.url.startsWith('../')) {
                node.ref.url = node.ref.url.substr(3, node.ref.url.length - 3);
            }
        } else if (node instanceof md.BlockElement) {
            fix_links(node.nodes);
        }
    }
}

function get_markdown_title(doc) {
    for (const node of doc.blocks) {
        if (node instanceof md.Heading && node.level === 'h1') {
            return `Aurora's Decorations - ${node.toString().substr(2)}`;
        }
    }
    return "Aurora's Decorations";
}
