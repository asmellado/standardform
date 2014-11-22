package es.vegamultimedia.standardform.components;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import com.vaadin.server.FileDownloader;
import com.vaadin.server.StreamResource;
import com.vaadin.server.StreamResource.StreamSource;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomField;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Notification.Type;
import com.vaadin.ui.Upload;
import com.vaadin.ui.Upload.ChangeEvent;
import com.vaadin.ui.Upload.ChangeListener;
import com.vaadin.ui.Upload.Receiver;
import com.vaadin.ui.Upload.SucceededEvent;
import com.vaadin.ui.Upload.SucceededListener;
import com.vaadin.ui.VerticalLayout;

import es.vegamultimedia.standardform.model.File;

@SuppressWarnings("serial")
public class FileComponent extends CustomField<File> {
	
	VerticalLayout mainLayout;
	Upload upload;
	Button loadButton;
	Label filenameLabel;
	Button downloadButton;
	FileUploader fileUploader;
	
	public FileComponent(String caption, final File file,
			String id, boolean insertMode) {
		setCaption(caption);
		
		mainLayout = new VerticalLayout();
		
		// Añadimos el Upload sin botón
		upload = new Upload();
		fileUploader = new FileUploader();
		upload.setReceiver(fileUploader);
		upload.setButtonCaption(null);
		upload.addChangeListener(fileUploader);
		upload.addSucceededListener(fileUploader);
		mainLayout.addComponent(upload);
		
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
		mainLayout.addComponent(loadButton);
		
		HorizontalLayout buttonsLayout = new HorizontalLayout();
		mainLayout.addComponent(buttonsLayout);
		
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
