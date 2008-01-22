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
package org.eclipse.equinox.security.sample;

import java.net.URL;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.osgi.framework.Bundle;

public class InstallSignedAction implements IWorkbenchWindowActionDelegate {
	private IWorkbenchWindow window;

	private static final String BUNDLE_URL = "data/org.eclipse.test.signedcontent.signed_1.0.0.jar";

	/**
	 * The constructor.
	 */
	public InstallSignedAction() {
	}

	/**
	 * The action has been activated. The argument of the
	 * method represents the 'real' action sitting
	 * in the workbench UI.
	 * @see IWorkbenchWindowActionDelegate#run
	 */
	public void run(IAction action) {
		URL bundleURL = AuthAppPlugin.getBundleContext().getBundle().getEntry(BUNDLE_URL);
		try {
			Bundle bundle = AuthAppPlugin.getBundleContext().installBundle(BUNDLE_URL, bundleURL.openStream());
			bundle.start();

			MessageDialog.openInformation(window.getShell(), "Equinox Security Sample", "Installed and started bundle '" + bundle.getSymbolicName() + "'");
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/**
	 * Selection in the workbench has been changed. We 
	 * can change the state of the 'real' action here
	 * if we want, but this can only happen after 
	 * the delegate has been created.
	 * @see IWorkbenchWindowActionDelegate#selectionChanged
	 */
	public void selectionChanged(IAction action, ISelection selection) {
	}

	/**
	 * We can use this method to dispose of any system
	 * resources we previously allocated.
	 * @see IWorkbenchWindowActionDelegate#dispose
	 */
	public void dispose() {
	}

	/**
	 * We will cache window object in order to
	 * be able to provide parent shell for the message dialog.
	 * @see IWorkbenchWindowActionDelegate#init
	 */
	public void init(IWorkbenchWindow window) {
		this.window = window;
	}
}
