package frostillicus.xsp.model;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface RequiredFields {
	String[] value();
}
