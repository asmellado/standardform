package es.vegamultimedia.standardform;

import java.io.Serializable;

import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Notification.Type;
import com.vaadin.ui.Table;
import com.vaadin.ui.Table.ColumnGenerator;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.BaseTheme;

import es.vegamultimedia.standardform.DAO.BeanDAOException;
import es.vegamultimedia.standardform.model.Bean;

public class SearchWindow<T extends Bean, K> extends Window {
	
	/**
	 * Interface for listening for a selection in a searchWindow
	 */
	public interface SelectionListener<T> extends Serializable {
		/**
		 * Called when an element has been selected in a searchWindow
		 * @param element selected
		 */
		public abstract void select(T element);
	}

	private static final long serialVersionUID = -7484559342264755709L;
	
	protected ListForm<T, K> listForm;
	protected SelectionListener<T> selectListener;

	public SearchWindow(BeanUI<T, K> beanUI, SelectionListener<T> selectListener) {
		super("Selección");
		this.selectListener = selectListener;
		
		setModal(true);
		center();
		setHeight("90%");
		setWidth("90%");
		
		try {
			listForm = new ListForm<T, K>(beanUI);
			listForm.disableForm(false);
			listForm.addGeneratedColumn("Seleccionar", new SeleccionarColumn());
			setContent(listForm);
		} catch (BeanDAOException e) {
			Notification.show("Error",
					"No se puede mostrar la ventana de selección.\n" + e.getMessage(),
					Type.ERROR_MESSAGE);
			e.printStackTrace();
		}
	}
	
	class SeleccionarColumn implements ColumnGenerator {

		private static final long serialVersionUID = 7160224583866154579L;

		@Override
		public Object generateCell(Table source, final Object itemId, Object columnId) {
			// Creamos botón para seleccionar elemento
			Button seleccionarButton = new Button("Seleccionar");
			seleccionarButton.setStyleName(BaseTheme.BUTTON_LINK);
			seleccionarButton.addClickListener(new ClickListener() {

				private static final long serialVersionUID = 5383485352885816534L;

				@SuppressWarnings("unchecked")
				@Override
				public void buttonClick(ClickEvent event) {
					selectListener.select((T) itemId);
					close();
				}
				
			});
			return seleccionarButton;
		}
		
	}

}
