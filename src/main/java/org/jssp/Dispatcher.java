//$Id: Dispatcher.java,v 1.1 2002/07/25 18:23:40 lvm Exp $
//$Log: Dispatcher.java,v $
//Revision 1.1  2002/07/25 18:23:40  lvm
//Initial addition.
//
//Revision 1.5  2002/04/04 19:42:45  lvm
//Small fixes.
//
//Revision 1.4  2002/03/06 20:25:36  lvm
//Many fixes in login process. Add action for executing SQL.
//
//Revision 1.3  2002/03/05 18:36:34  lvm
//Many changes and improvements.
//
//Revision 1.2  2002/02/28 20:06:13  lvm
//Small fixes.
//
//Revision 1.1  2002/02/28 19:41:07  lvm
//Initial addition.
//

package org.jssp;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.NativeObject;
import org.mozilla.javascript.Scriptable;

public class Dispatcher {
    private HashMap<String,ApplicationContext> applications = new HashMap<>();

    // Initialize servlet
    public void reinit(String dataDir) {
	try {
	    // Get configuration of application
	    if (dataDir == null || dataDir.length() == 0) {
		dataDir = "./data";
		System.out.println(
			"Can't find parameter dataDir in configuration. Use default value \"" + dataDir + "\".");
	    }

	    // Get list of subdirs in (dataDir)/application/
	    File appsDirRoot = new File(dataDir, "application");
	    if (!appsDirRoot.isDirectory())
		throw new IOException("File " + appsDirRoot + " is not a directory");

	    String[] applicationNames = appsDirRoot.list();

	    for (int i = 0; i < applicationNames.length; i++) {
		if ("CVS".equals(applicationNames[i]))
		    continue;
		File appDir = new File(appsDirRoot, applicationNames[i]);
		if (appDir.isDirectory()) {
		    // initialize global scope for JS
		    Context context = Context.enter();
		    Scriptable globalScope = context.initStandardObjects(new NativeObject());
		    Context.exit();

		    // Add recursive link to global scope so we can modify scope
		    // from initializer script
		    globalScope.put("globalScope", globalScope, globalScope);
		    globalScope.put("scope", globalScope, globalScope);

		    // Execute system init script
		    String javaSystemScriptInitializerScript = dataDir + File.separatorChar + "etc" + File.separatorChar
			    + "init.jssp";
		    JSSP jssp = new JSSP(javaSystemScriptInitializerScript);
		    System.out.println("System init script for aplication \"" + applicationNames[i] + "\"");
		    System.out.println("Output from \"" + javaSystemScriptInitializerScript + "\"");
		    System.out.println("-0-0-0-0-0-0-0-0-0-0-0-0-0-0-0-0-0-0-0-0-0-0-0-0-");
		    jssp.printToStream(System.out, globalScope, new NativeObject());
		    System.out.println("\n-0-0-0-0-0-0-0-0-0-0-0-0-0-0-0-0-0-0-0-0-0-0-0-0-");

		    try {
			ApplicationContext application = new ApplicationContext(applicationNames[i], appsDirRoot,
				globalScope);

			// Store application in cache
			applications.put(application.getName(), application);
		    } catch (Exception e) {
			System.err.println(
				"Can't initialize application \"" + applicationNames[i] + "\":" + e.getMessage());
			e.printStackTrace(System.err);
		    }
		}
	    }
	} catch (Exception e) {
	    e.printStackTrace(System.err);
	    System.out.println("\nCan't initialize servlet.");
	    System.exit(1);
	}
    }

    public void service(String applicationName, String pageName, Writer out) {
	// Use this object to transfer parameters from action to page
	Scriptable rs = new NativeObject();

	ApplicationContext application = (ApplicationContext) applications.get(applicationName);

	try {
	    // Show page
	    application.showJsspPage(pageName, rs, out);

	} catch (Exception e) {
	    e.printStackTrace(System.err);
	}
    }

}
