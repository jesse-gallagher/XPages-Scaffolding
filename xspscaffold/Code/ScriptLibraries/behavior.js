MCLBehavior = {
	"div.lotusMain": {
		// Tag the content are with some extra classes if there are left and/or right columns
		found: function(div) {
			var leftCol = dojo.query("div.lotusColLeft").length > 0
			var rightCol = dojo.query("div.lotusColRight").length > 0
			if(leftCol) { dojo.addClass(div, "hasLeftCol") }
			if(rightCol) { dojo.addClass(div, "hasRightCol") }
			if(leftCol && rightCol) { dojo.addClass(div, "hasBothCols") }
		}
	}
}

dojo.ready(function() {
	dojo.behavior.add(MCLBehavior)
	dojo.behavior.apply()
})