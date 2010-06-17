package sync.gclient;

public class GDataException extends Exception {
    private Throwable cause;
    
    /** Creates a new instance of GDataException */
    public GDataException(String message, Throwable cause) {
        super(buildSuperMessage(message, cause));
        this.cause = cause;
    }
    
    private static String buildSuperMessage(String message, Throwable cause) {
        if(cause != null) {
            if(cause.getClass() != Exception.class && cause.getClass() != GDataException.class) {
                String className = cause.getClass().getName();
                if(className.indexOf('.') >= 0) {
                    className = className.substring(className.lastIndexOf('.'));
                }
                message += ": " + className + ( cause.getMessage() == null ? "" : (" - " + cause.getMessage()) );
            }
            else if(cause.getMessage() != null) {
                message += ": " + cause.getMessage();
            }
        }
        return message;
    }
    
    public GDataException(String className, String methodName, Throwable cause) {
        this("Error at " + className + "." + methodName, cause);
    }
    
    public GDataException(Class sourceClass, String methodName, Throwable cause) {
        this("Error at " + sourceClass.getName() + "." + methodName, cause);
    }
    
    public GDataException(String message, String className, String methodName, Throwable cause) {
        this(message + " at " + className + "." + methodName, cause);
    }
    
    public GDataException(String message) {
        this(message, (Throwable)null);
    }
    
    public GDataException(String className, String methodName) {
        this(className, methodName, (Throwable)null);
    }
    
    public GDataException(String message, String className, String methodName) {
        this(message, className, methodName, null);
    }
    
    public Throwable getCause() {
        return cause;
    }
    
}
