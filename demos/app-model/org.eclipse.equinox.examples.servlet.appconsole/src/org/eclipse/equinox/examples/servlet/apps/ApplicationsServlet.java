/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.equinox.examples.servlet.apps;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.util.*;
import javax.servlet.ServletException;
import javax.servlet.http.*;
import org.osgi.framework.BundleException;
import org.osgi.service.application.*;

public class ApplicationsServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final String APP = "app_";
	private static final String HANDLE = "handle_";
	private static final String POSTFIX = ".x";
	private static final String START_APP = "start_app";
	private static final String STOP_APP = "stop_app";
	private static final String INSTALL_BUNDLE = "install_bundles";
	private static final String CMD_START_APP = START_APP + POSTFIX; 
	private static final String CMD_STOP_APP = STOP_APP + POSTFIX;
	private static final String CMD_INSTALL_BUNDLE = INSTALL_BUNDLE + POSTFIX;
	private static final String INSTALL_BUNDLE_DIR = "install_bundles_dir";
	

	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		doPost(req, resp);
	}

	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		PrintWriter pw = resp.getWriter();
		try {
			runCommands(req);
		} catch (Throwable e) {
			e.printStackTrace(pw);
		}
		writeHeader(pw);
		writeBody(pw);
	}

	private void runCommands(HttpServletRequest req) throws ApplicationException, IOException, BundleException {
		ArrayList apps = new ArrayList();
		ArrayList handles = new ArrayList();
		boolean startApps = false;
		boolean stopHandles = false;
		for (Enumeration params = req.getParameterNames(); params.hasMoreElements();) {
			String key = (String) params.nextElement();
			if (key.startsWith(APP))
				apps.add(key.substring(APP.length()));
			else if (key.startsWith(HANDLE))
				handles.add(key.substring(HANDLE.length()));
			else if (key.equals(CMD_START_APP))
				startApps = true;
			else if (key.equals(CMD_STOP_APP))
				stopHandles = true;
			else if (key.equals(CMD_INSTALL_BUNDLE))
				Activator.installBundles(req.getParameter(INSTALL_BUNDLE_DIR));
		}
		if (startApps)
			for (Iterator eApps = apps.iterator(); eApps.hasNext();)
				startApp((String) eApps.next());
		if (stopHandles)
			for (Iterator eHandles = handles.iterator(); eHandles.hasNext();)
				stopHandle((String) eHandles.next());
	}

	private void stopHandle(String instanceID) {
		ApplicationHandle[] handles = Activator.getHandles();
		for (int i = 0; i < handles.length; i++)
			if (instanceID.equals(handles[i].getInstanceId())) {
				handles[i].destroy();
				break;
			}
	}

	private void startApp(String appID) throws ApplicationException {
		ApplicationDescriptor[] apps = Activator.getApplications();
		for (int i = 0; i < apps.length; i++)
			if (appID.equals(apps[i].getApplicationId())) {
				apps[i].launch(null);
				break;
			}
	}

	private void writeHeader(PrintWriter pw) {
		pw.println("<html><head><title>Equinox Applications Console</title>");
		pw.println("<LINK href=\"resources/console.css\" rel=\"stylesheet\" type=\"text/css\">");
		pw.println("</head>");
	}

	private void writeBody(PrintWriter pw) {
		pw.println("<body><form action=\"\" method=\"POST\">");
		writeToolbarAndApplications(pw);
		pw.println("</form></body></html>");
	}

	private void writeToolbarAndApplications(PrintWriter pw) {
		pw.println("<table width = \"100%\" class=\"maintable\">");
		writeToolbar(pw);
		pw.println("<tr><td class=\"mainview\">");
		writeApplications(pw);
		pw.println("</td><td class=\"mainview\">");
		writeHandles(pw);
		pw.println("</td></tr>");
		writeInstallbar(pw);
		pw.println("</table>");
	}

	private void writeToolbar(PrintWriter pw) {
		pw.println("<tr class=\"toolview\"><td colspan=\"2\" class=\"toolview\">");
		pw.println("<input alt=\"Start selected applications\" type=\"image\" class=\"iconcmd\" name=\"" + START_APP + "\" src=\"resources/resume_co.gif\"/>");
		pw.println("<input alt=\"Stop selected applications\" type=\"image\" class=\"iconcmd\" name=\"" + STOP_APP + "\" src=\"resources/terminate_co.gif\"/>");
		pw.println("<input alt=\"Stop selected applications\" type=\"image\" class=\"iconcmd\" src=\"resources/refresh.gif\"/>");
		pw.println("</td></tr>");
	}

	private void writeInstallbar(PrintWriter pw) {
		pw.println("<tr class=\"toolview\"><td colspan=\"2\" class=\"toolview\">");
		pw.println("<input alt=\"Install Bundles\" type=\"image\" class=\"iconcmd\" name=\"" + INSTALL_BUNDLE + "\" src=\"resources/repo_rep.gif\"/>");
		pw.println("<input alt=\"Directory\" type=\"text\" name=\"" + INSTALL_BUNDLE_DIR + "\">");
		pw.println("</td></tr>");
	}

	private void writeApplications(PrintWriter pw) {
		pw.println("<div class=\"shadow\">Installed Applications</div><table width=\"100%\" class=\"apptable\">");
		ApplicationDescriptor[] descriptions = Activator.getApplications();
		if (descriptions.length == 0)
			pw.println("<tr><td><div class=\"appname\">No applications</div></td></tr>");
		for (int i = 0; i < descriptions.length; i++) {
			Map appProps = descriptions[i].getProperties(null);
			URL url = (URL) appProps.get(ApplicationDescriptor.APPLICATION_ICON);
			String name = (String) appProps.get(ApplicationDescriptor.APPLICATION_NAME);
			if (name == null)
				name = descriptions[i].getApplicationId();
			pw.println("<tr><td><input type=\"checkbox\" name=\"" + APP + descriptions[i].getApplicationId() + "\"/></td><td>" + getIcon(url) + "</td><td><div class=\"appname\">" + name + "</div></td></tr>");
		}
		pw.println("</table>");
	}

	private String getIcon(URL url) {
		if (url != null)
			return "<img border=\"0\" src=\"icons?url=" + url.toExternalForm() + "\"/>";
		return "<img border=\"0\" src=\"resources/alt16.gif\"/>";
	}

	private void writeHandles(PrintWriter pw) {
		pw.println("<div class=\"shadow\">Application Instances</div><table>");
		ApplicationHandle[] handles = Activator.getHandles();
		if (handles.length == 0)
			pw.println("<tr><td><div class=\"appname\">No instances</div></td></tr>");
		for (int i = 0; i < handles.length; i++) {
			ApplicationDescriptor description = handles[i].getApplicationDescriptor();
			Map appProps = description.getProperties(null);
			String name = (String) appProps.get(ApplicationDescriptor.APPLICATION_NAME);
			if (name == null)
				name = description.getApplicationId();
			pw.println("<tr><td><input type=\"checkbox\" name=\"" + HANDLE + handles[i].getInstanceId() + "\"/></td><td><div class=\"appname\">" + name + "</div></td><td><div class=\"appdescription\">" + handles[i].getInstanceId() + "</div></td></tr>");
		}
		pw.println("</table>");
	}
}
