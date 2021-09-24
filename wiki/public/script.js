for (const nav_dir of document.querySelectorAll("li.wiki_nav_directory")) {
	nav_dir.addEventListener("click", event => {
		if (event.target !== event.currentTarget)
			return;
		if (event.currentTarget.attributes["open"]) {
			event.currentTarget.removeAttribute("open");
		} else {
			event.currentTarget.setAttribute("open", "");
		}
	});
}
