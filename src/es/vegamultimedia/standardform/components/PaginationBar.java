package es.vegamultimedia.standardform.components;

import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomField;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;

public class PaginationBar extends CustomField<Long> {
	
	private static final long serialVersionUID = 4629535328213002591L;
	
    /**
	 * Interface for pagination
	 */
	public interface PaginationListener {
		/**
		 * Called to refresh the table with a new pagination
		 */
		public abstract void paginate(int page);
	}
	
	private HorizontalLayout mainLayout;
	
	public PaginationBar(final long numElements, final int page, final int elementsPerPage,
			final PaginationListener paginationListener) {
		mainLayout = new HorizontalLayout();
		final int lastPage = (int) ((numElements-1)/elementsPerPage);
		if (page > 0) {
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
					paginationListener.paginate(page - 1);
				}
			});
			mainLayout.addComponent(previousButton);
		}
		long firstElement = page * elementsPerPage + 1;
		long lastElement = (page + 1) * elementsPerPage;
		if (lastElement > numElements) {
			lastElement = numElements;
		}
		Label paginationLabel = new Label(firstElement + 
				" a " + lastElement + " de " + numElements);
		mainLayout.addComponent(paginationLabel);
		if (page < lastPage) {
			// Next page button
			Button nextButton = new Button(">");
			nextButton.addClickListener(new ClickListener() {
				
				private static final long serialVersionUID = -4580233303041354651L;

				@Override
				public void buttonClick(ClickEvent event) {
					paginationListener.paginate(page + 1);
				}
			});
			mainLayout.addComponent(nextButton);
			
			// Last page button
			Button lastButton = new Button(">>");
			lastButton.addClickListener(new ClickListener() {

				private static final long serialVersionUID = 6381387201810157695L;

				@Override
				public void buttonClick(ClickEvent event) {
					paginationListener.paginate(lastPage);
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