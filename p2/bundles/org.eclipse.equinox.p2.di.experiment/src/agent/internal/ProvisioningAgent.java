/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package agent.internal;

import agent.IAgentServiceFactory;
import agent.IProvisioningAgent;
import environment.Location;
import java.util.*;
import org.osgi.framework.*;

/**
 * Represents a p2 agent instance.
 */
public class ProvisioningAgent implements IProvisioningAgent {

	private final Map agentServices = Collections.synchronizedMap(new HashMap());

	private BundleContext context;

	/* (non-Javadoc)
	 * @see agent.IProvisioningAgent#getService(java.lang.String)
	 */
	public Object getService(String serviceName) {
		Object service = agentServices.get(serviceName);
		if (service != null)
			return service;
		//attempt to get service from registry
		ServiceReference[] refs;
		try {
			refs = context.getServiceReferences(IAgentServiceFactory.AGENT_SERVICE, "(" + IAgentServiceFactory.PROP_SERVICE_NAME + '=' + serviceName + ')'); //$NON-NLS-1$
		} catch (InvalidSyntaxException e) {
			e.printStackTrace();
			return null;
		}
		if (refs == null || refs.length == 0)
			return null;
		IAgentServiceFactory factory = (IAgentServiceFactory) context.getService(refs[0]);
		if (factory == null)
			return null;
		service = factory.createService(this);
		if (service != null)
			agentServices.put(serviceName, service);
		return service;
	}

	public void registerService(String serviceName, Object service) {
		agentServices.put(serviceName, service);
	}

	public void setBundleContext(BundleContext context) {
		this.context = context;
	}

	public void setLocation(Location location) {
		agentServices.put(Location.class.getName(), location);
	}

	public void unregisterService(String serviceName, Object service) {
		synchronized (agentServices) {
			if (agentServices.get(serviceName) == service)
				agentServices.remove(serviceName);
		}
	}
}
