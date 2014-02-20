XPages Scaffolding
==================

XPages Scaffolding is a basic database structure designed to quickly get started with a functional OneUI app, including a number of handy utilities. Currently, the main features are:

- An applicationLayout-based layout with a couple useful custom controls
- flashScope ([http://www.bleedyellow.com/blogs/andyc/entry/a_flash_scope_for_xpages?lang=en_gb])
- DynamicViewCustomizer ([http://openntf.org/s/dynamicviewcustomizer])
- frostillicus.controller classes ([http://frostillic.us/f.nsf/posts/more-on--controller--classes])
- frostillicus.model classes ([http://frostillic.us/f.nsf/posts/my-current-model-framework--part-1])
- frostillicus.event classes ([http://frostillic.us/f.nsf/posts/a-prototype-in-app-messaging-system-for-xpages])
- DominoDocumentMap, a bean for easily accessing document fields by UNID via EL
- A JSFUtil class with a number of handy utility methods including MIMEBean methods ([http://www.timtripcony.com/blog.nsf/d6plinks/TTRY-8NKTPN])
- A properties-based app config object ([https://frostillic.us/f.nsf/posts/expanding-your-use-of-el-(part-2)])
- A handful of converters for common needs
- Some Notes-client design elements for good measure, such as:
	- A basic outline, frameset, and shared actions
	- A subform that does some simple modified-by tracking
	- A "Formula Console" form for testing/executing arbitrary formula/LS code in the context of the database
- Scaffolding for writing FacesContext-aware servlets ([http://frostillic.us/f.nsf/posts/building-xpages-servlets-with-facescontext-access])

The scaffolding requires the Extension Library and the OpenNTF Domino API.


Older Releases
==============

- R1: This release used a Dojo-BorderContainer-based layout using OneUI and contained a number of elements that aided in mimicking traditional Notes apps.
- R2: This release contained a layout control using Bootstrap elements. This has been switched to an applicationLayout control, meant to be paired with Bootstrap4XPages or another render kit. This release also didn't require the OpenNTF API.