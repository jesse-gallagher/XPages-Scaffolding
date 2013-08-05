//via http://dontpanic82.blogspot.com/2010/01/xpages-hijackingpublishing-partial.html

XSP.addOnLoad(function() {
	// Hijack the partial refresh
	XSP._inheritedPartialRefresh = XSP._partialRefresh;
	XSP._partialRefresh = function( method, form, refreshId, options ){  
		// Publish init
		dojo.publish( 'partialrefresh-init', [ method, form, refreshId, options ]);
		this._inheritedPartialRefresh( method, form, refreshId, options );
	}

	// Publish start, complete and error states 
	dojo.subscribe( 'partialrefresh-init', function( method, form, refreshId, options ){

		if( options ){ // Store original event handlers
			var eventOnStart = options.onStart; 
			var eventOnComplete = options.onComplete;
			var eventOnError = options.onError;
		}

		options = options || {};  
		options.onStart = function(){
			dojo.publish( 'partialrefresh-start', [ method, form, refreshId, options ]);
			if( eventOnStart ){
				if( typeof eventOnStart === 'string' ){
					eval( eventOnStart );
				} else {
					eventOnStart();
				}
			}
		};

		options.onComplete = function(){
			dojo.publish( 'partialrefresh-complete', [ method, form, refreshId, options ]);
			if( eventOnComplete ){
				if( typeof eventOnComplete === 'string' ){
					eval( eventOnComplete );
				} else {
					eventOnComplete();
				}
			}
		};

		options.onError = function(){
			dojo.publish( 'partialrefresh-error', [ method, form, refreshId, options ]);
			if( eventOnError ){
				if( typeof eventOnError === 'string' ){
					eval( eventOnError );
				} else {
					eventOnError();
				}
			}
		};
	});
})