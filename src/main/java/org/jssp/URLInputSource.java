/**
 * URLInputSource.java
 *
 * @author Created by Volodymyr M. Lisivka
 */

package org.jssp;

import java.util.*;
import java.io.*;
import java.net.*;

/**
 * URL input source
 */
public class URLInputSource implements InputSource {
    public URLInputSource(URL url) {
	if (url != null)
	    this.url = url;
	else
	    throw new NullPointerException("Can't create URLInputSource from NULL url");
    }

    private URL url;

    public String getResourceName() {
	return url.toExternalForm();
    }

    public java.io.InputStream getInputStream() throws IOException {
	return url.openStream();
    }

    /**
     * This method always return <b>false<b>.
     */
    public boolean isChangedAfterLastReading() {
	return false;
    }
}
