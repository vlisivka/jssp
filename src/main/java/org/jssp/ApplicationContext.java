/**
 * ApplicationContext.java
 *
 * @author Created by Omnicore CodeGuide
 */

package org.jssp;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.Writer;
import java.util.Properties;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.NativeObject;
import org.mozilla.javascript.Scriptable;

public class ApplicationContext {

    // Global scope for all Java Scripts, we can modify it only in
    // initialization script
    private Scriptable globalScope = null;

    private Scriptable actions = null;
    private Scriptable pages = null;

    private Properties configuration = new Properties();

    private String name = null;
    private String rootDir = null;

    public ApplicationContext(String name, File dataDir, Scriptable globalScope) throws IOException {
	rootDir = dataDir.getPath() + File.separator + name;
	// Read configuration file
	configuration.load(new FileInputStream(rootDir + File.separatorChar + "application.properties"));
	configuration.put("rootDir", rootDir);

	this.name = configuration.getProperty("applicationName", name);
	this.globalScope = globalScope;

	init();
    }

    private File initJsspFile = null, actionsJsspFile = null, pagesJsspFile = null;
    private JSSP initJssp = null, actionsJssp = null, pagesJssp = null;
    private long initJsspTimestamp = -1, actionsJsspTimestamp = -1, pagesJsspTimestamp = -1;

    private void reinit() {// Reload configuration if one of configuration files
			   // is changed

	if (initJsspFile.lastModified() != initJsspTimestamp) {
	    try {
		initJsspTimestamp = initJsspFile.lastModified();
		actionsJsspTimestamp = actionsJsspFile.lastModified();
		pagesJsspTimestamp = pagesJsspFile.lastModified();

		System.out.println("Reinitialize aplication \"" + this.name + "\"\nOutput from \"" + initJssp
			+ "\"\n-0-0-0-0-0-0-0-0-0-0-0-0-0-0-0-0-0-0-0-0-0-0-0-0-");
		initJssp.printToStream(System.out, globalScope, new NativeObject());
		System.out.println("\n-0-0-0-0-0-0-0-0-0-0-0-0-0-0-0-0-0-0-0-0-0-0-0-0-");

		System.out.println("Reinitialize aplication \"" + this.name + "\"\nOutput from \"" + actionsJssp
			+ "\"\n-0-0-0-0-0-0-0-0-0-0-0-0-0-0-0-0-0-0-0-0-0-0-0-0-");
		actionsJssp.printToStream(System.out, globalScope, new NativeObject());
		System.out.println("\n-0-0-0-0-0-0-0-0-0-0-0-0-0-0-0-0-0-0-0-0-0-0-0-0-");

		System.out.println("Reinitialize aplication \"" + this.name + "\"\nOutput from \"" + pagesJssp
			+ "\"\n-0-0-0-0-0-0-0-0-0-0-0-0-0-0-0-0-0-0-0-0-0-0-0-0-");
		pagesJssp.printToStream(System.out, globalScope, new NativeObject());
		System.out.println("\n-0-0-0-0-0-0-0-0-0-0-0-0-0-0-0-0-0-0-0-0-0-0-0-0-");

		actions = (Scriptable) globalScope.get("actions", globalScope);
		pages = (Scriptable) globalScope.get("pages", globalScope);
	    } catch (Exception e) {
		e.printStackTrace(System.err);
	    }
	}

	if (actionsJsspFile.lastModified() != actionsJsspTimestamp) {
	    try {
		actionsJsspTimestamp = actionsJsspFile.lastModified();

		System.out.println("Reinitialize aplication \"" + this.name + "\"\nOutput from \"" + actionsJssp
			+ "\"\n-0-0-0-0-0-0-0-0-0-0-0-0-0-0-0-0-0-0-0-0-0-0-0-0-");
		actionsJssp.printToStream(System.out, globalScope, new NativeObject());
		System.out.println("\n-0-0-0-0-0-0-0-0-0-0-0-0-0-0-0-0-0-0-0-0-0-0-0-0-");

		actions = (Scriptable) globalScope.get("actions", globalScope);
	    } catch (Exception e) {
		e.printStackTrace(System.err);
	    }
	}

	if (pagesJsspFile.lastModified() != pagesJsspTimestamp) {
	    try {
		pagesJsspTimestamp = pagesJsspFile.lastModified();

		System.out.println("Reinitialize aplication \"" + this.name + "\"\nOutput from \"" + pagesJssp
			+ "\"\n-0-0-0-0-0-0-0-0-0-0-0-0-0-0-0-0-0-0-0-0-0-0-0-0-");
		pagesJssp.printToStream(System.out, globalScope, new NativeObject());
		System.out.println("\n-0-0-0-0-0-0-0-0-0-0-0-0-0-0-0-0-0-0-0-0-0-0-0-0-");

		pages = (Scriptable) globalScope.get("pages", globalScope);
	    } catch (Exception e) {
		e.printStackTrace(System.err);
	    }
	}
    }

    private void init() throws IOException {
	// initialize configuration object
	globalScope.put("configuration", globalScope, configuration);

	// initialize application object
	globalScope.put("application", globalScope, this);

	// Execute application init script
	String initJsspFileName = rootDir + File.separatorChar + "init.jssp";
	initJsspFile = new File(initJsspFileName);
	initJsspTimestamp = initJsspFile.lastModified();

	String actionsJsspFileName = rootDir + File.separatorChar + "actions.jssp";
	actionsJsspFile = new File(actionsJsspFileName);
	actionsJsspTimestamp = actionsJsspFile.lastModified();

	String pagesJsspFileName = rootDir + File.separatorChar + "pages.jssp";
	pagesJsspFile = new File(pagesJsspFileName);
	pagesJsspTimestamp = pagesJsspFile.lastModified();

	initJssp = new JSSP(initJsspFileName);
	System.out.println("Initialize aplication \"" + this.name + "\"\nOutput from \"" + initJssp
		+ "\"\n-0-0-0-0-0-0-0-0-0-0-0-0-0-0-0-0-0-0-0-0-0-0-0-0-");
	initJssp.printToStream(System.out, globalScope, new NativeObject());
	System.out.println("\n-0-0-0-0-0-0-0-0-0-0-0-0-0-0-0-0-0-0-0-0-0-0-0-0-");

	actionsJssp = new JSSP(actionsJsspFileName);
	System.out.println("Initialize aplication \"" + this.name + "\"\nOutput from \"" + actionsJssp
		+ "\"\n-0-0-0-0-0-0-0-0-0-0-0-0-0-0-0-0-0-0-0-0-0-0-0-0-");
	actionsJssp.printToStream(System.out, globalScope, new NativeObject());
	System.out.println("\n-0-0-0-0-0-0-0-0-0-0-0-0-0-0-0-0-0-0-0-0-0-0-0-0-");

	pagesJssp = new JSSP(pagesJsspFileName);
	System.out.println("Initialize aplication \"" + this.name + "\"\nOutput from \"" + pagesJssp
		+ "\"\n-0-0-0-0-0-0-0-0-0-0-0-0-0-0-0-0-0-0-0-0-0-0-0-0-");
	pagesJssp.printToStream(System.out, globalScope, new NativeObject());
	System.out.println("\n-0-0-0-0-0-0-0-0-0-0-0-0-0-0-0-0-0-0-0-0-0-0-0-0-");

	actions = (Scriptable) globalScope.get("actions", globalScope);
	pages = (Scriptable) globalScope.get("pages", globalScope);
    }

    public String getName() {
	return name;
    }

    public String getRootDir() {
	return rootDir;
    }

    public void showJsspPage(String pageName, Scriptable rs, Writer out) throws Exception {

	Scriptable scope = new ReadOnlyScriptableObject(getGlobalScope(), new String[] { "scope" });

	// Put some variables into script scope ( session, request, response,
	// etc. ) so we can call it directly
	scope.put("scope", scope, scope);

	// Get page description by name
	Object tmp = pages.get(pageName, pages);
	if (tmp == Scriptable.NOT_FOUND)
	    throw new RuntimeException("Page \"" + pageName + "\" not found!");

	Scriptable page = (Scriptable) tmp;

	String pageType = null;
	tmp = page.get("pageType", page);
	if (tmp != Scriptable.NOT_FOUND)
	    pageType = Context.toString(tmp);

	String contentType = null;
	tmp = page.get("contentType", page);
	if (tmp != Scriptable.NOT_FOUND)
	    contentType = Context.toString(tmp);

	String file = null;
	tmp = page.get("file", page);
	if (tmp != Scriptable.NOT_FOUND)
	    file = Context.toString(tmp);
	else
	    throw new RuntimeException(
		    "Property \"file\" is required for page description! (page \"" + pageName + "\"");

	// Set content type to text/html
	//response.setContentType(contentType);

	// TODO:pageType

	CommonFuncs.executeJssp(this, scope, file, rs, out);
    }

    public Scriptable getGlobalScope() {
	return globalScope;
    }

    public Properties getConfiguration() {
	return configuration;
    }

}
