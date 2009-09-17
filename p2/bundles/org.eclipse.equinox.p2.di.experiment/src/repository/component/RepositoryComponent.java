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
package repository.component;

import agent.IAgentServiceFactory;
import agent.IProvisioningAgent;
import engine.Profile;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.preferences.IPreferencesService;
import repository.RepositoryManager;
import unclassified.EventBus;

/**
 * 
 */
public class RepositoryComponent implements IAgentServiceFactory {

	private IPreferencesService preferences;
	private IExtensionRegistry registry;

	public void setPreferences(IPreferencesService prefs) {
		this.preferences = prefs;
	}

	public void setRegistry(IExtensionRegistry registry) {
		this.registry = registry;
	}

	public Object createService(IProvisioningAgent agent) {
		EventBus bus = (EventBus) agent.getService(EventBus.class.getName());
		Profile profile = (Profile) agent.getService(Profile.class.getName());
		return new RepositoryManager(bus, preferences, profile, registry);
	}
}
