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
	// h/t http://www.bootstrap4xpages.com/bs4xp/site.nsf/article.xsp?documentId=F7E581AA0B402846C1257B6B004582A1&action=openDocument
	"div.dijitDialog": {
		found: function(div) {
			// Clear out margins for the inner form
			dojo.query("form", div).style("margin", "0");

			var titleBar = $(".dijitDialogTitleBar", div).addClass("modal-header");

			var titleNode = $(".dijitDialogTitle", titleBar);
			var title = titleNode.text();
			titleNode.remove();
			
			$(".dijitDialogCloseIcon", titleBar).removeClass("dijitDialogCloseIcon").addClass("close");
			titleBar.append("<h3>" + title + "</h3>");
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