package pw.jonwinters.common.model;

public interface Document<T> {

    /**
     * enum's name
     * @return the name of enum
     */
    String getName();

    /**
     * enum's code or value
     * generally be used for passing values
     * @return enum's code or value
     */
    T getCode();
}
