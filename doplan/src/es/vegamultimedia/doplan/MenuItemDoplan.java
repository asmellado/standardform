package es.vegamultimedia.doplan;

import com.vaadin.navigator.View;

public class MenuItemDoplan<T extends View> {
	private String caption;
	private Class<T> viewClass;
	private String name;
	
	public MenuItemDoplan(String caption, Class<T> viewClass, String name) {
		setCaption(caption);
		setViewClass(viewClass);
		setName(name);
	}
	
	public String getCaption() {
		return caption;
	}
	public void setCaption(String caption) {
		this.caption = caption;
	}
	public Class<T> getViewClass() {
		return viewClass;
	}
	public void setViewClass(Class<T> viewClass) {
		this.viewClass = viewClass;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}