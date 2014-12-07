package frostillicus.xsp.model.servlet.resources;

// Copied from the com.ibm.domino.httpmethod.PATCH class in the ExtLib,
// available from http://extlib.openntf.org

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import javax.ws.rs.HttpMethod;


@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@HttpMethod("PATCH")
public @interface PATCH {

}
