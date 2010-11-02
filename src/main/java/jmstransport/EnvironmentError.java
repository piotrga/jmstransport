package jmstransport;

/**
 * For documentation see http://www.natpryce.com/articles/000698.html
 */
public class EnvironmentError extends RuntimeException {
	public EnvironmentError(String message, Throwable cause) {
		super(message, cause);
	}

    public EnvironmentError(String message) {
        super(message);
    }
}
