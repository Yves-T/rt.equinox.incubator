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
import java.security.SecureRandom;
import javax.net.ssl.*;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

public class ConnectSSLAction implements IWorkbenchWindowActionDelegate {

	private IWorkbenchWindow workbenchWindow;

	public void init(IWorkbenchWindow window) {
		workbenchWindow = window;
	}

	public void run(IAction action) {

		ServerURLDialog dialog = new ServerURLDialog(workbenchWindow.getShell());
		dialog.open();

		String serverName = dialog.getServerURL();

		try {
			SSLContext context = SSLContext.getInstance("SSL"); //$NON-NLS-1$
			context.init(new KeyManager[] {}, new TrustManager[] {new MyX509TrustManager()}, new SecureRandom());
			SSLSocketFactory factory = context.getSocketFactory();

			HttpsURLConnection.setDefaultSSLSocketFactory(factory);
			URLConnection conn = new URL("https://" + serverName).openConnection();
			conn.connect();

			MessageDialog.openInformation(workbenchWindow.getShell(), "Equinox Security Sample", "Connection established");

		} catch (Exception e) {
			MessageDialog.openInformation(workbenchWindow.getShell(), "Equinox Security Sample", "Failed to connect");
			e.printStackTrace();
		}
	}

	public void selectionChanged(IAction action, ISelection selection) {
	}

	public void dispose() {
	}

}
