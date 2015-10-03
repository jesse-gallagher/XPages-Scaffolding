frostillic.us Framework
=======================

https://github.com/jesse-gallagher/XPages-Scaffolding

The frostillic.us Framework is the successor project to the original "XPages Scaffolding" NTF. It is intended to provide a set of foundational and utility classes for developing XPages applications with improved structure without imposing a great deal of performance or programmatic overhead.

There is an overview of the Framework's functionality available in [NotesIn9 158](http://www.notesin9.com/2014/10/10/notesin9-158-intro-to-the-frostillicus-framework/). Additionally, there is a tutorial series available [on frostillic.us](https://frostillic.us/f.nsf/posts/building-an-app-with-the-frostillic.us-framework--part-1).

Dependencies
------------

The Framework requires Domino 9.0.1+ and the OpenNTF Domino API 2.0.0+. Additionally, to use the model framework, the plugin must be given access to reflection via Java policy. The quickest (and time-tested) method to do this is to create a file named `java.pol` in your `jvm/lib/ext` directory in your Domino installation with the following contents:

    grant {
            permission java.security.AllPermission;
    }

Components
----------

There are several primary components, each of which can be used separately as desired:

#### Controller Classes

"Controller" classes (though admittedly an inaccurate name) are classes meant to be paired with a single XPage in your application. Their primary purpose is to get business logic that would otherwise be expressed in SSJS on the page into a consistently-named Java class, simplfying the XSP structure and making it easier to share and refactor page code.

Classes in your NSF that extend `frostillicus.xsp.controller.BasicXPageController` (or implement the `XPageController` interface), are in the `controller` package, and are named with the case-sensitive name of your XPage (e.g. `controller.home`) will be automatically instantiated on page load and will be available to the page as `controller`.

Note: due to the way XPages are initialized, in order to bind the controller class to the `beforePageLoad` and `afterPageLoad` events, they must be explicitly bound in the XSP source. For example:

	<?xml version="1.0" encoding="UTF-8"?>
    <xp:view xmlns:xp="http://www.ibm.com/xsp/core" xmlns:xe="http://www.ibm.com/xsp/coreex" xmlns:xc="http://www.ibm.com/xsp/custom"
        beforePageLoad="#{controller.beforePageLoad}" afterPageLoad="#{controller.afterPageLoad}">
	</xp:view>

There is an overview of controllers as a concept in [NotesIn9 106](http://www.notesin9.com/2013/04/08/notesin9-106-intro-to-java-controller-classes/).

#### Model Objects

The frostillic.us model-object framework is intended to be a straightforward way to build model logic in an XPage in a way that doesn't sacrifice the flexibility and ease of use of normal Domino document data sources.

A model object definition consists of a model class, which extends `AbstractDominoModel` for the Domino implementation (which is currently the *only* implementation), and a "manager" class, which extends `AbstractDominoManager<...>`. For example:

    package model;

    import javax.persistence.Table;

    import frostillicus.xsp.bean.ApplicationScoped;
    import frostillicus.xsp.bean.ManagedBean;
    import frostillicus.xsp.model.domino.AbstractDominoModel;
    import frostillicus.xsp.model.domino.AbstractDominoManager;

    @Table(name="Note")
    public class Note extends AbstractDominoModel {

		@NotEmpty String title;
        Date posted;
        String body;

        @ManagedBean(name="Notes")
        @ApplicationScoped
        public static class Manager extends AbstractDominoManager<Note> {

            @Override
            protected String getViewPrefix() {
                return "Notes\\";
            }
        }
    }

*Note: The use of the `model` package and an embedded class for the manager are conventions, not required by the framework.*

Getting and setting values on a model object is done through the `#getValue` and `#setValue` methods from `DataObject`, which will in turn reference properties and getter/setter methods on the object for extra logic and validation is present, and will otherwise pass the value through to the underlying Domino document directly.

For validation, it uses [Hibernate Validator](http://hibernate.org/validator/), which provides a superset of the standard `javax.validation` annotations.

For a mostly-detailed explanation of how to use these objects, see [the tutorial on frostillic.us](https://frostillic.us/f.nsf/posts/building-an-app-with-the-frostillic.us-framework--part-1).

Model objects can be bound to normal XPages controls, including file upload/download and rich text controls (though currently uploading an inline image in CKEditor does not work). The collections provided by the manager objects can be bound to normal repeat/iterator controls as well as `xp:viewPanel` and `xe:dataView`.

#### Simple Servlets

The Framework provides an abstract class and basic servlet factory to ease the process of writing FacesContext-aware servlets inside your NSF. By creating a class that extends `frostillicus.xsp.servlet.AbstractXSPServlet` in the `servlet` package in your NSF, the servlet will be available via `/yourapp.nsf/xsp/servletClassName`.

#### JSF Backports

The Framework includes a handful of JSF 2.x annotation backports for specifying managed beans inline in classes, without the need to add them in `faces-config.xml`. To declare a class as a managed bean, add an annotation of `@ManagedBean(name="yourBeanName")` to the class declaration, along with either `@ApplicationScoped`, `@SessionScoped`, `@ViewScoped`, `@RequestScoped`, or `@NoneScoped`.

#### Utility Class

The `frostillicus.xsp.util.FrameworkUtils` class provides a number of utility functions for dealing with the XPages environment and Domino data. In particular, the `getDatabase`, `getSession*`, and `get*Scope` methods are preferable to the `ExtLibUtil` equivalents because they are ODA-aware and will also work in OSGi servlet contexts (though the scope objects are not persistent).

<hr />

Implementation notes:

<h5>Specially-supported Model Field Types</h5>

- Enums
- java.sql.Time and java.sql.Date for specifying DateTime values as only Time or Date, respectively

<h5>Validation</h5>

- Validation is done via [Hibernate Validator](http://hibernate.org/validator/)
- @NotNull, @NotEmpty, and @Size(min=1+) cause the component map to set required=true for input components

<h5>Services</h5>

- *frostillicus.xsp.controller.ComponentMapAdapterFactory*: this service type defines a factory to produce objects implementing frostillicus.xsp.controller.ComponentMapAdapter; these are used in the controller class to add validators/converters/etc. to components based on model type

<h5>Localization</h5>

- The component map attempts to load a bundle named "model_translation" (e.g. "model_translation.properties", "model_translation_fr.properties", etc.) in the app and look
	for keys matching enum-class literal names (e.g. "model.Task$TaskType.Normal=Normal (en)")