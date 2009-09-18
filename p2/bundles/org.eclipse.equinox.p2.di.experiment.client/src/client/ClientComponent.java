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
package client;

import agent.IProvisioningAgent;
import agent.IProvisioningAgentProvider;
import engine.Engine;
import environment.Location;
import java.net.URI;

/**
 * A sample client of p2.
 */
public class ClientComponent {
	private IProvisioningAgentProvider provider;

	public void activate() throws Exception{
		Location location = new Location(new URI("file:/tmp/testagent")); //$NON-NLS-1$
		IProvisioningAgent agent = provider.createAgent(location);
		Engine engine = (Engine) agent.getService(Engine.class.getName());
		System.out.println(engine);
	}
	public void setAgentProvider(IProvisioningAgentProvider provider) {
		this.provider = provider;
	}
	
	public void unsetAgentProvider(IProvisioningAgentProvider aProvider) {
		if (provider == aProvider)
			provider = null;
	}
}
