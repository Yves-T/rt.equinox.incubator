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

import agent.IProvisioningAgent;
import agent.IProvisioningAgentProvider;
import environment.Location;
import org.osgi.framework.BundleContext;

/**
 * Default implementation of {@link IProvisioningAgentProvider}.
 */
public class DefaultAgentProvider implements IProvisioningAgentProvider {
	private BundleContext context;

	public void activate(BundleContext context) {
		this.context = context;
	}

	public IProvisioningAgent createAgent(Location location) throws Exception {
		ProvisioningAgent result = new ProvisioningAgent();
		result.setLocation(location);
		result.setBundleContext(context);
		return result;
	}

}
