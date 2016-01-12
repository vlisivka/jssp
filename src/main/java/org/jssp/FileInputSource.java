package org.jssp;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * File input source
 */
public class FileInputSource implements InputSource {
    private String fileName;
    private File file = null;
    private long lastModified = -1;

    public FileInputSource(String fileName) {
	this.fileName = fileName;
	this.file = new File(fileName);
    }

    public String getResourceName() {
	return "file://~/" + fileName;
    }

    public java.io.InputStream getInputStream() throws IOException {
	lastModified = file.lastModified();
	return new FileInputStream(file);
    }

    public boolean isChangedAfterLastReading() {
	return (file.lastModified() > lastModified);
    }
}
