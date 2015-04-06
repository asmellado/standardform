package es.vegamultimedia.standardform.DAO;

public class SearchCriterion {
	
	/**
	 * Criteria Type
	 */
	public enum SearchType {
		TEXT,
		ENUM,
		BEAN
	}
	
	private String nameField;

	private Object valueField;
	
	private SearchType typeCriteria;
		
	public SearchCriterion(String nameField, Object valueField, SearchType typeCriteria) {
		this.nameField = nameField;
		this.valueField = valueField;
		this.typeCriteria = typeCriteria;
	}

	public String getNameField() {
		return nameField;
	}

	public Object getValueField() {
		return valueField;
	}

	public SearchType getTypeCriteria() {
		return typeCriteria;
	}
}