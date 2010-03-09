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
import org.eclipse.equinox.p2.core.*;
import org.osgi.framework.*;

public class WebListenerActivator implements BundleActivator {

	private static final String SERVER_ID = "p2 web listener"; //$NON-NLS-1$
	private static BundleContext ctx;
	private IProvisioningAgent agent;
	private static WebListenerActivator activator = null;

	public void start(BundleContext context) throws Exception {
		activator = this;
		ctx = context;
		Dictionary serverParams = new Hashtable();
		serverParams.put(JettyConstants.HTTP_PORT, new Integer(8787));
		serverParams.put(JettyConstants.HTTP_HOST, "127.0.0.1"); //$NON-NLS-1$
		JettyConfigurator.startServer(SERVER_ID, serverParams);
		Platform.getBundle("org.eclipse.equinox.http.registry").start(Bundle.START_TRANSIENT);
		setupAgent();
	}

	public void stop(BundleContext context) throws Exception {
		JettyConfigurator.stopServer(SERVER_ID);
		if (agent != null)
			agent.stop();
		agent = null;
		ctx = null;
		activator = null;
	}

	public static WebListenerActivator getDefault() {
		return activator;
	}

	public static BundleContext getContext() {
		return ctx;
	}

	public IProvisioningAgent getAgent() {
		return agent;
	}

	private void setupAgent() throws ProvisionException {
		ServiceReference agentRef = getContext().getServiceReference(IProvisioningAgent.SERVICE_NAME);
		if (agentRef != null) {
			agent = (IProvisioningAgent) getContext().getService(agentRef);
			if (agent != null)
				return;
		}
		ServiceReference providerRef = getContext().getServiceReference(IProvisioningAgentProvider.SERVICE_NAME);
		if (providerRef == null)
			throw new RuntimeException("No provisioning agent provider is available"); //$NON-NLS-1$
		IProvisioningAgentProvider provider = (IProvisioningAgentProvider) getContext().getService(providerRef);
		if (provider == null)
			throw new RuntimeException("No provisioning agent provider is available"); //$NON-NLS-1$
		//obtain agent for currently running system
		agent = provider.createAgent(null);
		getContext().ungetService(providerRef);
	}
}
