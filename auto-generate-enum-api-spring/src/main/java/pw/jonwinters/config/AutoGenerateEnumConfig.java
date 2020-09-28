package pw.jonwinters.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "enums")
public class AutoGenerateEnumConfig {

    /**
     * all generated-url's base-path
     * full path will be like '/{base-path}/xxxx'
     * if given value is null or null , it won't work
     */
    private String basePath = "/debugs";


    /**
     * debug mode
     */
    private boolean debug = false;


    /**
     * if given debug value was set as true
     * then generated @RestController class will
     * be write to the filesystem for debugging,
     * default the debugPath will be set to '/tmp',
     * if this plugin not works correctly , you'd better chose some decompiler tools like JD-GUI
     * to check out what happen after generated operation
     */
    private String debugPath = "/tmp";


    /**
     * scan package
     */
    private String baseScanPackage = "pw.jonwinters";



}
