package pw.jonwinters.exception;

public class IllegalEnumException extends RuntimeException{

    /**
     * Check if enum satisfied the requirement.
      */
    public IllegalEnumException() {
        super("Check if enum satisfied the requirement.");
    }

    public IllegalEnumException(String message) {
        super(message);
    }
}
