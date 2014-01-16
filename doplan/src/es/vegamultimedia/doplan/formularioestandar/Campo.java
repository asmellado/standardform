package es.vegamultimedia.doplan.formularioestandar;

public class Campo {
	
	private String caption;
	private String name;
	
	public Campo(String caption, String name) {
		setCaption(caption);
		setName(name);
	}
	
	public String getCaption() {
		return caption;
	}
	public void setCaption(String caption) {
		this.caption = caption;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}

}
