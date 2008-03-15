/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.equinox.security.sample.ssl;

import java.net.URL;
import java.net.URLConnection;
import javax.net.ssl.*;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

public class ConnectSSLAction implements IWorkbenchWindowActionDelegate {

	private IWorkbenchWindow workbenchWindow;
	static {
		TrustManager[] trustAllCerts = new TrustManager[] {new MyX509TrustManager()};

		// Install the all-trusting trust manager
		try {
			SSLContext sc = SSLContext.getInstance("SSL");
			sc.init(null, trustAllCerts, new java.security.SecureRandom());
			HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void init(IWorkbenchWindow window) {
		workbenchWindow = window;
	}

	public void run(IAction action) {
		// bring up the ui to get server urls
		ServerURLDialog dialog = new ServerURLDialog(workbenchWindow.getShell());
		dialog.open();

		String serverURL = dialog.getSeverURL();

		try {
			URL url = new URL(serverURL);
			URLConnection urlCo = url.openConnection();
			urlCo.connect();
			MessageDialog.openInformation(workbenchWindow.getShell(), "Equinox Security Sample", "Connected successfully");
		} catch (Exception e) {
			MessageDialog.openInformation(workbenchWindow.getShell(), "Equinox Security Sample", "Failed to connect");
		}
	}

	public void selectionChanged(IAction action, ISelection selection) {
	}

	public void dispose() {
	}

}
