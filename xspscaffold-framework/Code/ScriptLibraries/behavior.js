Behavior = {
	
}

dojo.ready(function() {
	dojo.behavior.add(Behavior)
	dojo.behavior.apply()
})

//Make sure that future pagers are also straightened out
dojo.subscribe("partialrefresh-complete", null, function(method, form, refreshId) {
        dojo.behavior.apply()
})