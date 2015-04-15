package es.vegamultimedia.standardform.components;

import java.io.Serializable;

import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomField;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;

import es.vegamultimedia.standardform.BeanUI;
import es.vegamultimedia.standardform.SearchWindow;
import es.vegamultimedia.standardform.SearchWindow.SelectionListener;
import es.vegamultimedia.standardform.model.Bean;

public class SearchField<BEAN extends Bean, KEY> extends CustomField<BEAN> {
	
	private static final long serialVersionUID = -8424431382645013354L;

	/**
	 * Interface for listening for an event in a SearchField
	 * @param <BEAN>
	 */
	public interface SearchListener<BEAN> extends Serializable {
		/**
		 * Called when the user selects an element in the search field
		 * @param oldBean old selected element
		 * @param newBean new selected element
		 */
		public abstract void select(BEAN oldBean, BEAN newBean);
		/**
		 * Called when the user removes the element in the search field
		 * @param bean removed element
		 */
		public abstract void remove(BEAN bean);
	}
	
	private SearchListener<BEAN> searchListener;
	private BeanUI<BEAN, KEY> beanUI;
	private HorizontalLayout mainLayout;
	private TextField textField;
	
	public SearchField(String caption, final BeanUI<BEAN, KEY> beanUI, BEAN element) {
		setCaption(caption);
		this.beanUI = beanUI;
		setInternalValue(element);
		
		mainLayout = new HorizontalLayout();
		
		// Campo de texto con el nombre del elemento
		textField = new TextField();
		textField.setEnabled(false);
		textField.setNullRepresentation("");
		textField.addStyleName("standardform-field");
		if (element != null) {
			textField.setValue(element.toString());
		}
		mainLayout.addComponent(textField);
		
		// Botón para eliminar selección
		Button removeButton = new Button("Eliminar");
		removeButton.addClickListener(new ClickListener() {
			
			private static final long serialVersionUID = -4509944771963362198L;
			
			@Override
			public void buttonClick(ClickEvent event) {
				removeSelection();
			}
			
		});
		mainLayout.addComponent(removeButton);
		
		// Botón para buscar
		Button searchButton = new Button("Buscar");
		searchButton.addClickListener(new ClickListener(){
			
			private static final long serialVersionUID = -8558245758755206742L;
			
			@Override
			public void buttonClick(ClickEvent event) {
				SearchWindow<BEAN, KEY> searchWindow =
						new SearchWindow<BEAN, KEY>(beanUI, new SelectionListener<BEAN>() {
					private static final long serialVersionUID = -6402516322271114921L;

					@Override
					public void select(BEAN newElement) {
						setSelection(newElement);
					}
				});
				UI.getCurrent().addWindow(searchWindow);
			}
			
		});
		mainLayout.addComponent(searchButton);
	}
	
	/**
	 * Removes the current selection
	 */
	protected void removeSelection() {
		BEAN oldBean = getInternalValue();
		setInternalValue(null);
		textField.setValue(null);
		if (searchListener != null) {
			searchListener.remove(oldBean);
		}
	}

	/**
	 * Sets a new selection
	 * @param newBean
	 */
	protected void setSelection(BEAN newBean) {
		BEAN oldElement = getInternalValue();
		setInternalValue(newBean);
		textField.setValue(newBean.toString());
		if (searchListener != null) {
			searchListener.select(oldElement, newBean);
		}
	}

	@Override
	public Class<BEAN> getType() {
		return beanUI.getBeanClass();
	}

	@Override
	protected Component initContent() {
		return mainLayout;
	}
	
	public void setSearchListener(SearchListener<BEAN> searchListener) {
		this.searchListener = searchListener;
	}

}
