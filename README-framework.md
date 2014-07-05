Framework README
================

Specially-supported Model Field Types
-------------------------------------

- Enums
- java.sql.Time and java.sql.Date for specifying DateTime values as only Time or Date, respectively

Validation
----------

- Validation is done via [Hibernate Validator](http://hibernate.org/validator/)
- @NotNull, @NotEmpty, and @Size(min=1+) cause the component map to set required=true for input components

Services
--------

- *frostillicus.xsp.controller.ComponentMapAdapterFactory*: this service type defines a factory to produce objects implementing frostillicus.xsp.controller.ComponentMapAdapter; these are used in the controller class to add validators/converters/etc. to components based on model type

Localization
------------

- The component map attempts to load a bundle named "model_translation" (e.g. "model_translation.properties", "model_translation_fr.properties", etc.) in the app and look
	for keys matching enum-class literal names (e.g. "model.Task$TaskType.Normal=Normal (en)")