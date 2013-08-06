Behavior = {
	// Convert xp:pagers to Bootstrap
	"div.xspPagerContainer": {
		found: function(pager) {
			var bsPager = document.createElement("div");
			bsPager.className = "pagination";
			var ul = document.createElement("ul");
			bsPager.appendChild(ul);
                        
			dojo.forEach(dojo.query("span", pager), function(span) {
				// Check to see if it has children
				var spans = dojo.query("span", span);
				if(spans.length == 0) {
					var li = document.createElement("li");
					ul.appendChild(li);

					var links = dojo.query("a", span);
					if(links.length < 1) {
						li.className = /\bxspCurrentItem\b/.test(span.className) ? "active" : "disabled";
						var a = document.createElement("a");
						a.href = "javascript:void(0)";
						a.innerHTML = span.innerHTML == "" ? "&nbsp;" : span.innerHTML;
						li.appendChild(a);
					} else {
						li.appendChild(links[0]);
					}
				}
			});
                        
			pager.parentNode.insertBefore(bsPager, pager);
			pager.parentNode.removeChild(pager);
		}
	},
        
	// Convert xp:radioGroups to Bootstrap
	"table[role=presentation]": {
		found: function(table) {
			var radios = dojo.query("input[type=radio]", table);
			if(radios.length > 0) {
				var div = document.createElement("div");

				dojo.forEach(radios, function(radio) {
					var label = radio.parentNode;
					dojo.addClass(label, " radio inline");
					div.appendChild(label);
				});
				
				table.parentNode.insertBefore(div, table);
				table.parentNode.removeChild(table);
			}
		}
	},
        
	// Convert xe:dialogs to Bootstrap
	"div.dijitDialog": {
		found: function(div) {
			dojo.addClass(div, "modal");
			// Clear out the margin-left for the modal
			div.style.marginLeft = "inherit";

			// Clear out margins for the inner form
			dojo.query("form", div).style("margin", "0");

			var titleBar = dojo.query(".dijitDialogTitleBar", div)[0];
			dojo.addClass(titleBar, "modal-header");

			var titleNode = dojo.query(".dijitDialogTitle", titleBar)[0];
			var title = titleNode.innerHTML;
			titleNode.parentNode.removeChild(titleNode);
                        
			var icon = dojo.query(".dijitDialogCloseIcon", titleBar)[0];
			dojo.removeClass(icon, "dijitDialogCloseIcon");
			dojo.addClass(icon, "close");
			icon.innerHTML += "&times;";
			
			titleBar.innerHTML += "<h3>" + title + "</h3>";
		}
	}
};

dojo.ready(function() {
	dojo.behavior.add(Behavior);
	dojo.behavior.apply();
});

//Make sure that future pagers are also straightened out
dojo.subscribe("partialrefresh-complete", null, function(method, form, refreshId) {
        dojo.behavior.apply();
});