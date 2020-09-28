package pw.jonwinters.exception;

/**
 * Indicated can't access @annotation marked field
 */
public class AnnotatedFieldAccessException extends RuntimeException {

    public AnnotatedFieldAccessException(String message, Throwable throwable) {
        super(message, throwable);
    }

    public AnnotatedFieldAccessException(Throwable throwable) {
        super("can't access this field ", throwable);
    }
}
