package es.vegamultimedia.standardform.DAO;

import java.io.Serializable;

public class SearchCriterion implements Serializable {
	
	private static final long serialVersionUID = 4531119044144781812L;

	/**
	 * Criteria Type
	 */
	public enum SearchType {
		TEXT,
		ENUM,
		BEAN
	}
	
	private String nameField;
	
	private String captionField;

	private Object valueField;
	
	private SearchType typeCriteria;
		
	public SearchCriterion(String nameField, String captionField,
			Object valueField, SearchType typeCriteria) {
		this.nameField = nameField;
		this.captionField = captionField;
		this.valueField = valueField;
		this.typeCriteria = typeCriteria;
	}

	public String getNameField() {
		return nameField;
	}

	public Object getValueField() {
		return valueField;
	}

	public String getCaptionField() {
		return captionField;
	}

	public SearchType getTypeCriteria() {
		return typeCriteria;
	}
}