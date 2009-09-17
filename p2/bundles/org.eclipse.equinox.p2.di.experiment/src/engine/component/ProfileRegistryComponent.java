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
package engine.component;

import agent.IAgentServiceFactory;
import agent.IProvisioningAgent;
import engine.ProfileRegistry;
import environment.Location;
import unclassified.EventBus;

/**
 * 
 */
public class ProfileRegistryComponent implements IAgentServiceFactory {

	public Object createService(IProvisioningAgent agent) {
		EventBus bus = (EventBus) agent.getService(EventBus.class.getName());
		Location location = (Location) agent.getService(Location.class.getName());
		return new ProfileRegistry(location, bus);
	}
}
