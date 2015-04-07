package es.vegamultimedia.standardform.components;

import java.util.ArrayList;
import java.util.Arrays;

import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomField;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;

public class PaginationBar extends CustomField<Long> {
	
	private static final long serialVersionUID = 4629535328213002591L;
	
	public static final Integer[] DEFAULT_POSSIBLE_ELEMENTS_PER_PAGE = {15, 30, 50, 100};
	
    /**
	 * Interface for pagination
	 */
	public interface PaginationListener {
		/**
		 * Called to refresh the table with a new pagination
		 */
		public abstract void paginate(int firstElement);
		
		/**
		 * Sets a new number of elements per page
		 * @param elementsPerPage
		 */
		public abstract void setElementsPerPage(int elementsPerPage);
	}
	
	private HorizontalLayout mainLayout;
	
	public PaginationBar(final long numElements, final int currentFirstElement,
			final int elementsPerPage, final PaginationListener paginationListener) {
		mainLayout = new HorizontalLayout();
		if (currentFirstElement > 0) {
			// First page button
			Button firstButton = new Button("<<");
			firstButton.addClickListener(new ClickListener() {

				private static final long serialVersionUID = -2696243976509696042L;

				@Override
				public void buttonClick(ClickEvent event) {
					paginationListener.paginate(0);
				}
			});
			mainLayout.addComponent(firstButton);
			// Previous page button
			Button previousButton = new Button("<");
			previousButton.addClickListener(new ClickListener() {
				
				private static final long serialVersionUID = -2696243976509696042L;

				@Override
				public void buttonClick(ClickEvent event) {
					int newFirstElement = Math.max(0, currentFirstElement - elementsPerPage);
					paginationListener.paginate(newFirstElement);
				}
			});
			mainLayout.addComponent(previousButton);
		}
		
		// Number elements combo
		ArrayList<Integer> possibleNumElements =
				new ArrayList<Integer>(Arrays.asList(DEFAULT_POSSIBLE_ELEMENTS_PER_PAGE));
		final ComboBox numElementsCombo = new ComboBox(null, possibleNumElements);
		numElementsCombo.setNullSelectionAllowed(false);
		numElementsCombo.setWidth("80px");
		numElementsCombo.setTextInputAllowed(false);
		numElementsCombo.setValue(elementsPerPage);
		numElementsCombo.addValueChangeListener(new ValueChangeListener() {

			private static final long serialVersionUID = 5638986015301501212L;

			@Override
			public void valueChange(
					com.vaadin.data.Property.ValueChangeEvent event) {
				int elementsPerPage = (int) numElementsCombo.getValue();
				paginationListener.setElementsPerPage(elementsPerPage);
			}
			
		});
		mainLayout.addComponent(numElementsCombo);
		
		// Info label
		int currentLastElement = (int) Math.min(currentFirstElement + elementsPerPage, numElements);
		Label paginationLabel = new Label((currentFirstElement + 1) + 
				" a " + currentLastElement + " de " + numElements);
		mainLayout.addComponent(paginationLabel);
		
		if (currentFirstElement < numElements - elementsPerPage) {
			// Next page button
			Button nextButton = new Button(">");
			nextButton.addClickListener(new ClickListener() {
				
				private static final long serialVersionUID = -4580233303041354651L;

				@Override
				public void buttonClick(ClickEvent event) {
					int nextElement = (int) Math.min(numElements - 1, currentFirstElement + elementsPerPage);
					paginationListener.paginate(nextElement);
				}
			});
			mainLayout.addComponent(nextButton);
			
			// Last page button
			Button lastButton = new Button(">>");
			lastButton.addClickListener(new ClickListener() {

				private static final long serialVersionUID = 6381387201810157695L;

				@Override
				public void buttonClick(ClickEvent event) {
					int newFirstElement = (int) (numElements - (int) (numElements % elementsPerPage));
					paginationListener.paginate(newFirstElement);
				}
			});
			mainLayout.addComponent(lastButton);
		}
	}

	@Override
	protected Component initContent() {
		return mainLayout;
	}

	@Override
	public Class<? extends Long> getType() {
		return Long.class;
	}
}