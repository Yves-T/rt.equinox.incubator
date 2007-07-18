/*******************************************************************************
 * Copyright (c) 2003, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.examples.rcp.browser;

import java.util.Hashtable;

import org.eclipse.core.runtime.Platform;
import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.eclipse.equinox.http.jetty.JettyConfigurator;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.osgi.framework.Bundle;

/**
 * The application class for the RCP Browser Example.
 * Creates and runs the Workbench, passing a <code>BrowserAdvisor</code>
 * as the workbench advisor.
 * 
 * @issue Couldn't run without initial perspective -- it failed with NPE 
 *   on WorkbenchWindow.openPage (called from Workbench.openFirstTimeWindow).  Advisor is currently required to override 
 *   getInitialWindowPerspectiveId.
 * 
 * @issue If shortcut bar is hidden, and last view in perspective is closed, there's no way to get it open again.
 * 
 * @since 3.0
 */
public class BrowserApp implements IApplication {
	private static String httpPort = "http.port";
	private static String otherInfo = "other.info";
	private static String equinoxWebinar = "equinox.webinar";
	private static String httpRegistry = "org.eclipse.equinox.http.registry";

	/**
	 * Constructs a new <code>BrowserApp</code>.
	 */
	public BrowserApp() {
		// do nothing
	}

	/* (non-Javadoc)
	 * @see org.eclipse.equinox.app.IApplication#start(org.eclipse.equinox.app.IApplicationContext)
	 */
	public Object start(IApplicationContext context) throws Exception {
		// start jetty
		Hashtable jettyProps = new Hashtable();
		jettyProps.put(httpPort, new Integer(80));
		jettyProps.put(otherInfo, equinoxWebinar);
		JettyConfigurator.startServer(equinoxWebinar, jettyProps);
		Bundle httpRegistryBundle = Platform.getBundle(httpRegistry);
		if (httpRegistryBundle != null)
			httpRegistryBundle.start(Bundle.START_TRANSIENT);
		Display display = PlatformUI.createDisplay();
		try {
			int code = PlatformUI.createAndRunWorkbench(display,
					new BrowserAdvisor());
			// exit the application with an appropriate return code
			return code == PlatformUI.RETURN_RESTART
					? EXIT_RESTART
					: EXIT_OK;
		} finally {
			if (display != null)
				display.dispose();
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.equinox.app.IApplication#stop()
	 */
	public void stop() {
		final IWorkbench workbench = PlatformUI.getWorkbench();
		if (workbench == null)
			return;
		final Display display = workbench.getDisplay();
		display.syncExec(new Runnable() {
			public void run() {
				if (!display.isDisposed())
					workbench.close();
			}
		});
		try {
			JettyConfigurator.stopServer(equinoxWebinar);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
