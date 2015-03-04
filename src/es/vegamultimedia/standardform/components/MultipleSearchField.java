package es.vegamultimedia.standardform.components;

import java.util.Collection;

import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomField;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Notification.Type;
import com.vaadin.ui.VerticalLayout;

import es.vegamultimedia.standardform.BeanUI;
import es.vegamultimedia.standardform.components.SearchField.SearchListener;
import es.vegamultimedia.standardform.model.Bean;

public class MultipleSearchField<T extends Collection<BEAN>, BEAN extends Bean, KEY>
		extends CustomField<T> {
	
	private static final long serialVersionUID = 2112023778178124159L;
	
	private Class<T> classCollection;
	private T elements;
	private BeanUI<BEAN, KEY> beanUI;
	private VerticalLayout mainLayout;
	private VerticalLayout searchLayout;
	private Button addButton;
	
	public MultipleSearchField(String caption, Class<T> classCollection,
			final BeanUI<BEAN, KEY> beanUI, final T elements) {
		setCaption(caption);
		this.elements = elements;
		this.beanUI = beanUI;
		setInternalValue(elements);
		
		mainLayout = new VerticalLayout();
		searchLayout = new VerticalLayout();
		mainLayout.addComponent(searchLayout);
		
		// Recorremos los elementos
		for (final BEAN element : elements) {
			// Por cada elemento creamos un SearchField
			addSearchField(element);
		}
		// Botón permitir añadir
		addButton = new Button("Añadir");
		addButton.addClickListener(new ClickListener() {

			private static final long serialVersionUID = 642468652946175278L;

			@Override
			public void buttonClick(ClickEvent event) {
				addSearchField(null);
				addButton.setVisible(false);
			}
			
		});
		mainLayout.addComponent(addButton);
	}
	
	// Añade un campo de búsqueda
	/**
	 * Adds a new search field with the specified bean
	 * @param bean
	 */
	private void addSearchField(final BEAN bean) {
		final SearchField<BEAN, KEY> searchField =
				new SearchField<BEAN, KEY>(null, beanUI, bean);
		searchField.setSearchListener(new SearchListener<BEAN>(){

			@Override
			public void remove(BEAN removedBean) {
				if (removedBean != null) {
					elements.remove(removedBean);
				}
				else {
					addButton.setVisible(true);
				}
				searchLayout.removeComponent(searchField);
			}

			@Override
			public void select(BEAN oldBean, BEAN newBean) {
				// Comprobamos si ya está el nuevo elemento en la lista
				for (BEAN element : elements) {
					if (element == newBean) {
						// Eliminamos el searchField y creamos otro nuevo
						searchLayout.removeComponent(searchField);
						addSearchField(oldBean);
						Notification.show("Aviso",
								"El elemento " + element + " ya está seleccionado",
								Type.WARNING_MESSAGE);
						return;
					}
				}
				if (oldBean != null) {
					elements.remove(oldBean);
				}
				if (newBean != null) {
					elements.add(newBean);
				}
				else {
					searchLayout.removeComponent(searchField);
				}
				if (oldBean == null && newBean != null) {
					addButton.setVisible(true);
				}
			}
			
		});
		searchLayout.addComponent(searchField);
	}

	@Override
	public Class<T> getType() {
		return classCollection;
	}

	@Override
	protected Component initContent() {
		return mainLayout;
	}

}
