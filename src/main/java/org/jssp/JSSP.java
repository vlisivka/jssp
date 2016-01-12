package org.jssp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.net.URL;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.EcmaError;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;

/**
 * Parser of templates with embedded JS.
 *
 * You can use JS variables like $varName; or embeded JS like
 * ##out.println("Hello,world!");##
 */
public class JSSP {

    /**
     * Input source with template.
     */
    private InputSource inputSource = null;
    /**
     * Compiled function
     */
    private Function function = null;

    public JSSP(InputSource inputSource) {
	this.inputSource = inputSource;
    }

    public JSSP(String fileName) {
	this.inputSource = new FileInputSource(fileName);
    }

    public JSSP(URL url) {
	this.inputSource = new URLInputSource(url);
    }

    /**
     * Print data to stream.
     */
    public void printToStream(Writer out, Scriptable scope, Scriptable params) throws IOException {
	PrintWriter os = new PrintWriter(out);
	try {
	    try {
		if (inputSource.isChangedAfterLastReading() || function == null) {
		    InputStream in = inputSource.getInputStream();
		    try {
			compileStream(in, scope);
		    } finally {
			in.close();
		    }
		}
		evaluateJS(os, scope, params);
	    } catch (EcmaError e1) {
		os.println("<b>" + e1 + "</b>");
		System.err.println(e1.getLocalizedMessage() + " in line " + e1.getLineNumber() + " in "
			+ inputSource.getResourceName());
		throw e1;
	    } catch (Throwable e) {
		os.println("<b>" + e + "</b>");
		e.printStackTrace(System.err);

		throw new IOException("" + e);
	    }
	} finally {
	    os.flush();
	}
    }

    public void printToStream(OutputStream out, Scriptable scope, Scriptable params) throws IOException {
	printToStream(new OutputStreamWriter(out), scope, params);
    }

    private void compileStream(InputStream is, Scriptable scope) {
	Context cx = Context.enter();
	try {
	    BufferedReader in = new BufferedReader(new InputStreamReader(is));
	    StringBuffer js = new StringBuffer("function anonymous(out,params){with(params){out.print(\"");
	    boolean flag[] = { false };
	    for (; in.ready();) {
		String line = in.readLine();
		if (line != null)
		    js.append(parseLine(line + "\n", flag));
	    }
	    js.append("\");}}");

	    cx.setLanguageVersion(Context.VERSION_1_8);
	    function = cx.compileFunction(scope, js.toString(), inputSource.getResourceName(), 1, null);
	} catch (EcmaError e) {
	    System.err.println(
		    e.getLocalizedMessage() + " in line " + e.getLineNumber() + " in " + inputSource.getResourceName());

	    throw new RuntimeException(
		    e.getLocalizedMessage() + " in line " + e.getLineNumber() + " in " + inputSource.getResourceName());
	} catch (Exception e) {
	    e.printStackTrace(System.err);
	    throw new RuntimeException("" + e);
	} finally {
	    Context.exit();
	}
    }

    private String parseLine(String line, boolean flag[]) {
	StringBuffer result = new StringBuffer(line.length());
	for (int i = 0; i < line.length(); i++) {
	    char ch = line.charAt(i);
	    if (!flag[0])
		switch (ch) {
		case '"':
		    result.append("\\\"");
		    break;
		case '\n':
		    result.append("\\n\");\nout.print(\"");
		    break;
		case '«':
		    if (i == line.length() - 1 || line.charAt(i + 1) == '»') {
			result.append('«');
			i++;
		    } else {
			result.append("\"+(");
			i++;
			for (; i < line.length(); i++) {
			    ch = line.charAt(i);
			    if (ch == '»')
				break;
			    result.append(ch);
			}
			result.append(")+\"");
		    }
		    break;
		case '„':
		    if (line.charAt(i + 1) == '„') {
			result.append("\");");
			i++;
			flag[0] = true;// Switch to JS mode
		    } else
			result.append(ch);
		    break;
		default:
		    result.append(ch);
		}
	    else {
		switch (ch) {
		case '“':
		    if (line.charAt(i + 1) == '“') {
			result.append(";out.print(\"");
			i++;
			flag[0] = false;// Switch to template mode
		    } else
			result.append(ch);
		    break;
		default:
		    result.append(ch);
		}
	    }
	}
	return result.toString();
    }

    private void evaluateJS(PrintWriter out, Scriptable scope, Scriptable params) throws Exception {
	if (function != null) {
	    Context cx = Context.enter();
	    try {
		cx.setLanguageVersion(Context.VERSION_1_8);
		function.call(cx, scope, scope, new Object[] { out, params });
	    } finally {
		Context.exit();
	    }
	}
    }
}
