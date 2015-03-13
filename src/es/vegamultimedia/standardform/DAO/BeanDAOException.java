package es.vegamultimedia.standardform.DAO;

@SuppressWarnings("serial")
public class BeanDAOException extends Exception {

	public BeanDAOException(String message) {
		super(message);
	}
	
	public BeanDAOException(String message, Throwable cause) {
		super(message, cause);
	}
}