package es.vegamultimedia.standardform.components;

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

public class SearchField<T extends Bean, K> extends CustomField<T> {
	
	private static final long serialVersionUID = -8424431382645013354L;

	private BeanUI<T, K> beanUI;
	private HorizontalLayout mainLayout;
	
	public SearchField(String caption, final BeanUI<T, K> beanUI, T element) {
		setCaption(caption);
		this.beanUI = beanUI;
		setInternalValue(element);
		
		mainLayout = new HorizontalLayout();
		
		// Campo de texto con el nombre del elemento
		final TextField textField = new TextField();
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
				setInternalValue(null);
				textField.setValue(null);
			}
			
		});
		mainLayout.addComponent(removeButton);
		
		// Botón para buscar
		Button searchButton = new Button("Buscar");
		searchButton.addClickListener(new ClickListener(){
			
			private static final long serialVersionUID = -8558245758755206742L;
			
			@Override
			public void buttonClick(ClickEvent event) {
				SearchWindow<T, K> searchForm = new SearchWindow<T, K>(beanUI, new SelectionListener<T>() {
					@Override
					public void select(T element) {
						setInternalValue(element);
						textField.setValue(element.toString());
					}
				});
				UI.getCurrent().addWindow(searchForm);
			}
			
		});
		mainLayout.addComponent(searchButton);
	}

	@Override
	public Class<T> getType() {
		return beanUI.getBeanClass();
	}

	@Override
	protected Component initContent() {
		return mainLayout;
	}

}
