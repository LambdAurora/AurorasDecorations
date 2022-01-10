/**
 * This only exists because someone decided it was a good idea to make max-height capable of animation only if we use absolute values.
 * It's awful, I can't express how it's awful, it's argh...
 * So we have to use JavaScript, you could hardcode a max-height but it would look awfully unnatural.
 *
 * @returns {number} the actual height
 */
function get_scroll_height(element) {
	let scroll_height = element.scrollHeight;

	for (let node of element.children) {
		if (node.tagName === "LI" && node.classList.contains("wiki_nav_directory"))
			node = node.lastChild;
		if (node.tagName === "UL")
			scroll_height += get_scroll_height(node);
	}

	return scroll_height;
}

function open_directory(element) {
	element.setAttribute("open", "");

	const children_list = element.querySelector("ul");
	if (children_list) {
		let should_open_everything = true;

		for (const child of children_list.children) {
			console.log(child.firstChild)
			if (child.firstChild) {
				if (child.firstChild.tagName === "DIV") {
					should_open_everything = false;
				}
			}
		}

		if (should_open_everything) {
			for (const child of children_list.children) {
				open_directory(child);
			}
		}
	}

	element.lastChild.style["max-height"] = get_scroll_height(element.lastChild) + "px";
}

for (const nav_dir of document.querySelectorAll("li.wiki_nav_directory")) {
	nav_dir.addEventListener("click", event => {
		if (event.target !== event.currentTarget)
			return;
		if (event.currentTarget.attributes["open"]) {
			event.currentTarget.lastChild.style["max-height"] = "0";
			event.currentTarget.removeAttribute("open");
		} else {
			open_directory(event.currentTarget);
		}
	});
}
