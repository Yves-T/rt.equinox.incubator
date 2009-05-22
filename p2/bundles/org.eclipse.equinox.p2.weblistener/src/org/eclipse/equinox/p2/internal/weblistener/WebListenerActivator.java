/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.equinox.p2.internal.weblistener;

import java.util.Dictionary;
import java.util.Hashtable;
import org.eclipse.core.runtime.Platform;
import org.eclipse.equinox.http.jetty.JettyConfigurator;
import org.eclipse.equinox.http.jetty.JettyConstants;
import org.osgi.framework.*;

public class WebListenerActivator implements BundleActivator {

	private static final String SERVER_ID = "p2 web listener"; //$NON-NLS-1$
	private static BundleContext ctx;

	public void start(BundleContext context) throws Exception {
		ctx = context;
		Dictionary serverParams = new Hashtable();
		serverParams.put(JettyConstants.HTTP_PORT, new Integer(8787));
		serverParams.put(JettyConstants.HTTP_HOST, "127.0.0.1"); //$NON-NLS-1$
		JettyConfigurator.startServer(SERVER_ID, serverParams);
		Platform.getBundle("org.eclipse.equinox.http.registry").start(Bundle.START_TRANSIENT);

	}

	public void stop(BundleContext context) throws Exception {
		JettyConfigurator.stopServer(SERVER_ID);
		ctx = null;
	}

	public static BundleContext getContext() {
		return ctx;
	}
}
