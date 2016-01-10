package org.jssp;

import java.io.IOException;
import java.io.InputStream;
/**
 *	Abstract source
 */
public interface InputSource
{
  public String getResourceName();
  public InputStream getInputStream() throws IOException;
  public boolean isChangedAfterLastReading();
}
