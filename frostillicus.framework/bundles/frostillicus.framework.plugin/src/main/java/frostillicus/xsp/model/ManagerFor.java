package frostillicus.xsp.model;

import java.lang.annotation.*;

/**
 * @since 1.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ManagerFor {
	Class<? extends ModelObject> value();
	String name() default "";
}
