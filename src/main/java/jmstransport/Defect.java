package jmstransport;

/**
 * For documentation see http://www.natpryce.com/articles/000739.html
 */
public class Defect extends RuntimeException {
	public Defect(Throwable e) {
		super(e);
	}

	public Defect(String errorMessage) {
		super(errorMessage);
	}

    public Defect(String errorMessage, Throwable exception) {
        super(errorMessage, exception);
    }
}
