//$Id: CommonFuncs.java,v 1.1 2002/07/25 18:23:40 lvm Exp $
//$Log: CommonFuncs.java,v $
//Revision 1.1  2002/07/25 18:23:40  lvm
//Initial addition.
//
//Revision 1.1  2002/02/28 19:41:07  lvm
//Initial addition.
//
package org.jssp;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.NativeObject;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.Undefined;


public class CommonFuncs
{
  public static String toHtml(String value,String defaultValue)
  {
    if(value==null)
      return defaultValue;

    StringBuffer result=new StringBuffer(value.length());
    char ch=' ';
    for(int i=0;i<value.length();i++)
    {
      switch(ch=value.charAt(i))
      {
        case '<':
          result.append("&lt;");
          break;
        case '>':
          result.append("&gt;");
          break;
        case '&':
          result.append("&amp;");
          break;
        case '\'':
          result.append("&#39;");
          break;
        case '"':
          result.append("&quot;");
          break;
        default:
          result.append(ch);
      }
    }
    return result.toString();
  }

  public static String toHtml(String value)
  {
    return toHtml(value,"");
  }

  public static String toUrl(String value,String defaultValue)
  {
    if(value==null)
      return defaultValue;
    else
      return java.net.URLEncoder.encode(value);
  }
  public static String toUrl(String value)
  {
    return toUrl(value,"");
  }

  public static void copyPropertiesToScriptable(Hashtable properties,Scriptable scriptable)
  {
    if(properties==null || scriptable==null)
      return;

    Context cx=Context.enter();
    try
    {
      for(Enumeration e=properties.keys();e.hasMoreElements();)
      {
        String key=(String)e.nextElement();
        Object value=properties.get(key);
        scriptable.put(key,scriptable,value);
      }
    }finally
    {
      Context.exit();
    }
  }
  public static void copyScriptableToProperties(Scriptable scriptable,Map<String,Object> properties)
  {
    if(properties==null || scriptable==null)
      return;

    Context.enter();
    try
    {
      Object ids[]=scriptable.getIds();
      for(int i=0;i<ids.length;i++)
      {
        String key=ids[i].toString();
        Object value=scriptable.get(ids[i].toString(),scriptable);
        if(value==null || value==Undefined.instance)
          continue;
        properties.put(key,value);
      }
    }finally
    {
      Context.exit();
    }
  }

  //Include JSSP page into current page (execute JSSP page without arguments)
  public static String includeJssp(ApplicationContext application,Scriptable scope,String jsspName)
  {
    return executeJssp(application,scope,jsspName,null);
  }
  public static void includeJssp(ApplicationContext application,Scriptable scope,String jsspName,OutputStream outputStream) throws IOException
  {
    executeJssp(application,scope,jsspName,null,outputStream);
  }
  public static void includeJssp(ApplicationContext application,Scriptable scope,String jsspName,Writer outputWriter) throws IOException
  {
    executeJssp(application,scope,jsspName,null,outputWriter);
  }

  //Execute JSSP page with arguments
  public static String executeJssp(ApplicationContext application,Scriptable scope,String jsspName,Scriptable parameters)
  {
    try
    {
      StringWriter stringWriter=new StringWriter();
      executeJssp(application,scope,jsspName,parameters,stringWriter);
      return stringWriter.toString();
    }catch(IOException e)
    {
      e.printStackTrace(System.err);
      throw new RuntimeException(""+e);
    }
  }
  public static void executeJssp(ApplicationContext application,Scriptable scope,String jsspName,Scriptable parameters,OutputStream outputStream) throws IOException
  {
    executeJssp(application,scope,jsspName,parameters,new OutputStreamWriter(outputStream));
  }
  public static void executeJssp(ApplicationContext application,Scriptable scope,String jsspName,Scriptable parameters,Writer outputWriter) throws IOException
  {

    //full path to file with JSSP page
    String dataDir=(String)application.getRootDir();
    String pathToJsspPage=dataDir+File.separatorChar+"jssp"+File.separatorChar+jsspName;

    //WARNING: synchronize(jsspCache) removed, but this code safe (jssp page may be compiled twice)
    JSSP jssp=(JSSP)jsspCache.get(pathToJsspPage);
    if(jssp==null)
    {
      jssp=new JSSP(pathToJsspPage);
      jsspCache.put(pathToJsspPage,jssp);
    }
    //WARNING: synchronize(jsspCache) removed, but this code safe (jssp page may be compiled twice)

    if(parameters==null)
      parameters=new NativeObject();

    jssp.printToStream(outputWriter,scope,parameters);
  }
  public static String executeJsspWithDefaultScope(ApplicationContext application,String jsspName,Scriptable parameters)
  {
    try
    {
      StringWriter stringWriter=new StringWriter();
      executeJsspWithDefaultScope(application,jsspName,parameters,stringWriter);
      return stringWriter.toString();
    }catch(IOException e)
    {
      e.printStackTrace(System.err);
      throw new RuntimeException(""+e);
    }
  }
  public static void executeJsspWithDefaultScope(ApplicationContext application,String jsspName,Scriptable parameters,OutputStream outputStream) throws IOException
  {
    executeJsspWithDefaultScope(application,jsspName,parameters,new OutputStreamWriter(outputStream));
  }

  public static void executeJsspWithDefaultScope(ApplicationContext application,String jsspName,Scriptable parameters,Writer outputWriter) throws IOException
  {
    Scriptable scope=new ReadOnlyScriptableObject(application.getGlobalScope(),new String[]{"scope"});
    scope.put("scope",scope,scope);


    //full path to file with JSSP page
    String dataDir=(String)application.getRootDir();
    String pathToJsspPage=dataDir+File.separatorChar+"jssp"+File.separatorChar+jsspName;

    //WARNING: synchronize(jsspCache) removed, but this code safe (jssp page may be compiled twice)
    JSSP jssp=(JSSP)jsspCache.get(pathToJsspPage);
    if(jssp==null)
    {
      jssp=new JSSP(pathToJsspPage);
      jsspCache.put(pathToJsspPage,jssp);
    }
    //WARNING: synchronize(jsspCache) removed, but this code safe (jssp page may be compiled twice)

    if(parameters==null)
      parameters=new NativeObject();

    jssp.printToStream(outputWriter,scope,parameters);
  }

  public static void clearJsspCache()
  {
    jsspCache=new HashMap<String,Object>();
  }


  //Global cache for JSSP pages
  private static Map<String,Object> jsspCache=new HashMap<String,Object>();
}
