package es.vegamultimedia.standardform.DAO;

public class BeanDAOException extends Exception {

	private static final long serialVersionUID = 7024218868598613268L;

	public BeanDAOException(String message) {
		super(message);
	}
	
	public BeanDAOException(String message, Throwable cause) {
		super(message, cause);
	}
}