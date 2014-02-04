package es.vegamultimedia.standardform;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Persistence;
import javax.persistence.Query;

import com.vaadin.data.fieldgroup.DefaultFieldGroupFieldFactory;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.ui.AbstractSelect.ItemCaptionMode;
import com.vaadin.ui.AbstractTextField;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Field;

import es.vegamultimedia.doplan.model.Localidad;

public class StandardFormFieldFactory extends DefaultFieldGroupFieldFactory {

	private static final long serialVersionUID = -8249212885106867525L;

	@Override
	public <T extends Field> T createField(Class<?> type, Class<T> fieldType) {
//		if (type.getSimpleName().equals("Localidad")) {
//			ComboBox combo = new ComboBox("Localidades");
//			combo.setItemCaptionMode(ItemCaptionMode.PROPERTY);
//			combo.setItemCaptionPropertyId("nombre");
//			combo.setConverter(ComboBoxConverter.class);
//			combo.setImmediate(true);
//			return fieldType.cast(combo);
//		}
		T campo = super.createField(type, fieldType);
		// Si es un campo de texto se establece el NullRepresentation
		if (campo instanceof AbstractTextField) {
			((AbstractTextField)campo).setNullRepresentation("");
		}
		return campo;
	}
}