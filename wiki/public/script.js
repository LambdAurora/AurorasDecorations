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

for (const nav_dir of document.querySelectorAll("li.wiki_nav_directory")) {
	nav_dir.addEventListener("click", event => {
		if (event.target !== event.currentTarget)
			return;
		if (event.currentTarget.attributes["open"]) {
			event.currentTarget.lastChild.style["max-height"] = "0";
			event.currentTarget.removeAttribute("open");
		} else {
			event.currentTarget.setAttribute("open", "");
			event.currentTarget.lastChild.style["max-height"] = get_scroll_height(event.currentTarget.lastChild) + "px";
		}
	});
}
