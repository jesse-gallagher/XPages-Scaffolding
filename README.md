XPages Scaffolding
==================

XPages Scaffolding is a basic database structure designed to quickly get started with a functional OneUI app, including a number of handy utilities. Currently, the main features are:

- A modified OneUI layout using Dojo Border Containers meant to create data-heavy UIs similiar to the Notes client
- flashScope (http://www.bleedyellow.com/blogs/andyc/entry/a_flash_scope_for_xpages?lang=en_gb)
- Linksbar based on an outline element, initially set to show all non-hidden views
- DynamicViewCustomizer (http://openntf.org/s/dynamicviewcustomizer)
- Controller classes (http://frostillic.us/f.nsf/posts/more-on--controller--classes)
- Light XML wrapper classes meant to be similar to Microsoft's XML library in use
- DominoDocumentMap, a bean for easily accessing document fields by UNID via EL
- A JSFUtil class with a number of handy utility methods including MIMEBean methods (http://www.timtripcony.com/blog.nsf/d6plinks/TTRY-8NKTPN)
- Some Notes-client design elements for good measure, such as:
	- A basic outline, frameset, and shared actions
	- A subform that does some simple modified-by tracking
	- A "Formula Console" form for testing/executing arbitrary formula/LS code in the context of the database

The scaffolding requires the Extension Library and probably Domino 8.5.3 or above (though it may work on 8.5.2).