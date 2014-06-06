package frostillicus.xsp.model;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Properties {
	String[] value();
	boolean exhaustive() default false;
	boolean includeWithView() default false;
}