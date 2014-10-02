package es.vegamultimedia.standardform;

abstract public class Utils {
	
	/**
	 * Return the string with the first letter capitalized
	 * @param string
	 * @return
	 */
	public static String capitalizeFirstLetter(String string) {
		return string.substring(0, 1).toUpperCase() + string.substring(1);
	}
}