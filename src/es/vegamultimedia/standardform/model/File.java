package es.vegamultimedia.standardform.model;

/**
 * An object of this class represents a generic file
 */
public class File {
	
	private byte[] bytes;
	
	private String filename;
	
	private String mimeType;
	
	public byte[] getBytes() {
		return bytes;
	}
	public void setBytes(byte[] bytes) {
		this.bytes = bytes;
	}
	public String getFilename() {
		return filename;
	}
	public void setFilename(String filename) {
		this.filename = filename;
	}
	public String getMimeType() {
		return mimeType;
	}
	public void setMimeType(String mimeType) {
		this.mimeType = mimeType;
	}
}
