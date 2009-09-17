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
import engine.*;
import org.eclipse.core.runtime.IExtensionRegistry;
import unclassified.EventBus;

/**
 * 
 */
public class EngineComponent implements IAgentServiceFactory {

	private IExtensionRegistry registry;

	public void setRegistry(IExtensionRegistry aRegistry) {
		this.registry = aRegistry;
	}

	public void unsetRegistry(IExtensionRegistry aRegistry) {
		if (this.registry == aRegistry)
			this.registry = null;
	}

	public Object createService(IProvisioningAgent agent) {
		EventBus bus = (EventBus) agent.getService(EventBus.class.getName());
		ProfileRegistry profileRegistry = (ProfileRegistry) agent.getService(ProfileRegistry.class.getName());
		TouchpointManager tpMgr = new TouchpointManager(registry);
		ActionManager actMgr = new ActionManager(tpMgr, registry);
		return new Engine(profileRegistry, actMgr, tpMgr, bus);
	}
}
