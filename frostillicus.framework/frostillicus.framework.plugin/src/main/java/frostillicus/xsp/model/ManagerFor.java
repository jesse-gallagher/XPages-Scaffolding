package frostillicus.xsp.model;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ManagerFor {
	Class<? extends ModelObject> value();
	String name() default "";
}
