package es.vegamultimedia.standardform.test;

import java.io.Serializable;

import com.vaadin.navigator.View;

public class MenuItemDoplan<B, V extends View> implements Serializable {
	private static final long serialVersionUID = -7124725317154698242L;
	
	private String caption;
	private Class<B> beanClass;
	private Class<V> viewClass;
	
	public MenuItemDoplan(String caption, Class<B> beanClass, Class<V> viewClass) {
		setCaption(caption);
		setBeanClass(beanClass);
		setViewClass(viewClass);
	}
	
	public String getCaption() {
		return caption;
	}
	public void setCaption(String caption) {
		this.caption = caption;
	}
	public Class<B> getBeanClass() {
		return beanClass;
	}
	public void setBeanClass(Class<B> beanClass) {
		this.beanClass = beanClass;
	}
	public Class<V> getViewClass() {
		return viewClass;
	}
	public void setViewClass(Class<V> viewClass) {
		this.viewClass = viewClass;
	}
}