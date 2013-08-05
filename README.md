XPages Scaffolding
==================

XPages Scaffolding is a basic database structure designed to quickly get started with a functional OneUI app, including a number of handy utilities. Currently, the main features are:

- A Bootstrap-based layout with a couple useful custom controls
- flashScope ([http://www.bleedyellow.com/blogs/andyc/entry/a_flash_scope_for_xpages?lang=en_gb])
- DynamicViewCustomizer ([http://openntf.org/s/dynamicviewcustomizer])
- frostillicus.controller classes ([http://frostillic.us/f.nsf/posts/more-on--controller--classes])
- frostillicus.model classes
- frostillicus.event classes
- Light XML wrapper classes meant to be similar to Microsoft's XML library in use
- DominoDocumentMap, a bean for easily accessing document fields by UNID via EL
- A JSFUtil class with a number of handy utility methods including MIMEBean methods ([http://www.timtripcony.com/blog.nsf/d6plinks/TTRY-8NKTPN])
- A properties-based app config object ([https://frostillic.us/f.nsf/posts/expanding-your-use-of-el-(part-2)])
- A handful of converters for common needs
- Some Notes-client design elements for good measure, such as:
	- A basic outline, frameset, and shared actions
	- A subform that does some simple modified-by tracking
	- A "Formula Console" form for testing/executing arbitrary formula/LS code in the context of the database

The scaffolding requires the Extension Library and probably Domino 8.5.3 or above (though it may work on 8.5.2).


Note
====

The original release was based around OneUI and contained a number of elements that aided in mimicking traditional Notes apps. These have been removed from the current version, but are still available under the "R1" tagged release.