package pw.jonwinters.common.annotations;

import javax.annotation.Resource;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that Enum class will be used to
 * generate a Spring RestController bean,
 * the generated Controller will provide a API method to expose Enum class
 */
@Documented
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface EnumApi {

    /**
     * enum's url
     */
    String value() default "/default";


    /**
     * swagger document support
     */
    String swaggerApiOperation() default "";
}
