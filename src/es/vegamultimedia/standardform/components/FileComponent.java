package es.vegamultimedia.standardform.components;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.imageio.ImageIO;

import com.vaadin.server.FileDownloader;
import com.vaadin.server.StreamResource;
import com.vaadin.server.StreamResource.StreamSource;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomField;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Image;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Notification.Type;
import com.vaadin.ui.Upload;
import com.vaadin.ui.Upload.ChangeEvent;
import com.vaadin.ui.Upload.ChangeListener;
import com.vaadin.ui.Upload.Receiver;
import com.vaadin.ui.Upload.SucceededEvent;
import com.vaadin.ui.Upload.SucceededListener;

import es.vegamultimedia.standardform.model.File;

@SuppressWarnings("serial")
public class FileComponent extends CustomField<File> {
	
	private GridLayout mainLayout;
	private Upload upload;
	private Button loadButton;
	private Label filenameLabel;
	private Button downloadButton;
	private FileUploader fileUploader;
	private final int alturaMiniatura = 100;
	
	/**
	 * Creates a new FileComponent to upload a file in "insert mode" without a current file
	 * @param caption
	 */
	public FileComponent(String caption) {
		this(caption, new File(), "", false);
		downloadButton.setVisible(false);
	}
	
	/**
	 * Creates a new FileComponent to upload or download an existing file 
	 * @param caption
	 * @param file
	 * @param id
	 * @param insertMode
	 */
	public FileComponent(String caption, final File file,
			String id, boolean insertMode) {
		setCaption(caption);
		
		mainLayout = new GridLayout();
		mainLayout.setRows(3);
		
		// Añadimos el Upload sin botón
		upload = new Upload();
		fileUploader = new FileUploader();
		upload.setReceiver(fileUploader);
		upload.setButtonCaption(null);
		upload.addChangeListener(fileUploader);
		upload.addSucceededListener(fileUploader);
		mainLayout.addComponent(upload, 0, 0);
		
		// Añadimos un botón para subir archivo
		loadButton = new Button("Subir");
		loadButton.setId(id + ".upload");
		loadButton.addClickListener(new ClickListener(){
			@Override
			public void buttonClick(ClickEvent event) {
				upload.submitUpload();
			}
		});
		loadButton.setEnabled(false);
		mainLayout.addComponent(loadButton, 0, 1);
		
		HorizontalLayout buttonsLayout = new HorizontalLayout();
		mainLayout.addComponent(buttonsLayout, 0, 2);
		
		// Añadimos la etiqueta con el nombre del archivo
		filenameLabel = new Label();
		filenameLabel.setCaption("Ningún archivo subido");
		
		// Si estamos en modo modificación y existe el archivo
		if (!insertMode && file != null) {
			filenameLabel.setCaption("Archivo actual: " + file.getFilename());
			
			// Añadimos un botón para descargar archivo
			downloadButton = new Button("Descargar");
			downloadButton.setId(id + ".download");
			StreamSource streamSource = new StreamSource(){
				@Override
				public InputStream getStream() {
					byte[] bytes = file.getBytes();
					return new ByteArrayInputStream(bytes);
				}
			};
			StreamResource streamResource =
					new StreamResource(streamSource, file.getFilename());
			FileDownloader fileDownloader = new FileDownloader(streamResource);
	        fileDownloader.extend(downloadButton);
	        buttonsLayout. addComponent(downloadButton);
	        // Si es una imagen
	        if (file instanceof es.vegamultimedia.standardform.model.Image) {
	        	// Mostramos una miniatura de la imagen
	        	try {
					Image image = new Image("", streamResource);
	        		BufferedImage bimg = ImageIO.read(streamSource.getStream());
	        		if (bimg != null) {
	        			float height = bimg.getHeight();
			        	float width = bimg.getWidth();
			        	image.setHeight(alturaMiniatura + "px");
			        	int newWidth = (int)(width*alturaMiniatura/height);
			        	image.setWidth(newWidth + "px");
	        		}
		        	mainLayout.setColumns(2);
		        	mainLayout.addComponent(image, 1, 0, 1, 2);
				} catch (IOException ignorada) {
					ignorada.printStackTrace();
				}
	        }
		}
		buttonsLayout.addComponent(filenameLabel);
	}

	@Override
	public Class<File> getType() {
		return File.class;
	}

	@Override
	protected Component initContent() {
		return mainLayout;
	}
	
	public class FileUploader implements Receiver, ChangeListener, SucceededListener {
		
		private ByteArrayOutputStream byteArrayOutputStream;
		private String filename;
		private String mimeType;

		@Override
		public OutputStream receiveUpload(String filename, String mimeType) {
			this.filename = filename;
			this.mimeType = mimeType;
			byteArrayOutputStream = null;
			if (filename == null || filename.isEmpty()) {
				Notification.show("Debe seleccionar una archivo", Type.ERROR_MESSAGE);
				return null;
			}
			byteArrayOutputStream = new ByteArrayOutputStream();
			return byteArrayOutputStream;
		}
		
		@Override
		public void filenameChanged(ChangeEvent event) {
			loadButton.setEnabled(true);
		}
		
		@Override
		public void uploadSucceeded(SucceededEvent event) {
			filenameLabel.setCaption("Archivo subido: " + event.getFilename());
			if (downloadButton != null) {
				downloadButton.setEnabled(false);
			}
		}

		public ByteArrayOutputStream getByteArrayOutputStream() {
			return byteArrayOutputStream;
		}

		public String getFilename() {
			return filename;
		}

		public String getMimeType() {
			return mimeType;
		}
	}

	public Upload getUpload() {
		return upload;
	}

	public Button getLoadButton() {
		return loadButton;
	}

	public Label getFilenameLabel() {
		return filenameLabel;
	}

	public Button getDownloadButton() {
		return downloadButton;
	}

	public FileUploader getFileUploader() {
		return fileUploader;
	}
}
