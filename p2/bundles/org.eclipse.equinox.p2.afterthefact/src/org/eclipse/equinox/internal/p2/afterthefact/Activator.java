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
package org.eclipse.equinox.internal.p2.afterthefact;

import org.eclipse.equinox.internal.p2.afterthefact.command.RunExampleCommand;
import org.eclipse.equinox.internal.p2.core.ProvisioningEventBus;
import org.eclipse.equinox.internal.p2.director.SimpleDirector;
import org.eclipse.equinox.internal.p2.director.SimplePlanner;
import org.eclipse.equinox.internal.p2.engine.SimpleProfileRegistry;
import org.eclipse.equinox.internal.p2.metadata.repository.MetadataRepositoryManager;
import org.eclipse.equinox.internal.provisional.p2.core.eventbus.IProvisioningEventBus;
import org.eclipse.equinox.internal.provisional.p2.director.IDirector;
import org.eclipse.equinox.internal.provisional.p2.director.IPlanner;
import org.eclipse.equinox.internal.provisional.p2.engine.IProfileRegistry;
import org.eclipse.equinox.internal.provisional.p2.metadata.repository.IMetadataRepositoryManager;
import org.eclipse.osgi.framework.console.CommandProvider;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;

public class Activator implements BundleActivator {

	private ServiceRegistration registration;
	public static BundleContext context;

	private IProvisioningEventBus bus;
	private ServiceRegistration registrationBus;

	private ServiceRegistration registrationDefaultManager;

	//	private ArtifactRepositoryManager artifactRepoManager;
	//	private ServiceRegistration registrationArtifactRepoManager;

	private IProfileRegistry profileRegistry;
	private ServiceRegistration registrationProfileRegistry;

	private IDirector director;
	private ServiceRegistration registrationDirector;

	private IPlanner planner;
	private ServiceRegistration registrationPlanner;

	private ServiceReference metadataRepositoryReference;
	
	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		Activator.context = context;
		registration = context.registerService(CommandProvider.class.getName(), new RunExampleCommand(context), null);
		registerEventBus();
		//create the profile registry
		registerProfileRegistry();
		registerMetadataRepositoryManager();

		//create the director and planner.  The planner must be
		//registered first because the director finds it in its constructor.
		registerPlanner();
		registerDirector();
	}

	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		Activator.context = null;
		registration.unregister();
		unregisterDirector();
		unregisterPlanner();
		unregisterDefaultMetadataRepoManager();
		unregisterProfileRegistry();
		unregisterEventBus();
		Activator.context = null;

	}
	
	public static BundleContext getContext() {
		return context;
	}




	private void registerDirector() {
		director = new SimpleDirector();
		registrationDirector = context.registerService(IDirector.class.getName(), director, null);
	}

	private void unregisterDirector() {
		registrationDirector.unregister();
		registrationDirector = null;
		director = null;
	}

	private void registerPlanner() {
		planner = new SimplePlanner();
		registrationPlanner = context.registerService(IPlanner.class.getName(), planner, null);
	}

	private void unregisterPlanner() {
		registrationPlanner.unregister();
		registrationPlanner = null;
		planner = null;
	}

	private void registerProfileRegistry() {
		profileRegistry = new SimpleProfileRegistry();
		registrationProfileRegistry = context.registerService(IProfileRegistry.class.getName(), profileRegistry, null);
	}

	private void unregisterProfileRegistry() {
		registrationProfileRegistry.unregister();
		registrationProfileRegistry = null;
		profileRegistry = null;
	}

	/**
	 * Returns a metadata repository manager, registering a service if there isn't
	 * one registered already.
	 */
	private void registerMetadataRepositoryManager() {
		//register a metadata repository manager if there isn't one already registered
		metadataRepositoryReference = context.getServiceReference(IMetadataRepositoryManager.SERVICE_NAME);
		if (metadataRepositoryReference == null) {
			registrationDefaultManager = context.registerService(IMetadataRepositoryManager.SERVICE_NAME, new MetadataRepositoryManager(), null);
			metadataRepositoryReference = registrationDefaultManager.getReference();
		}
	}

	private void unregisterDefaultMetadataRepoManager() {
		//unget the service obtained for the metadata cache
		if (metadataRepositoryReference != null) {
			context.ungetService(metadataRepositoryReference);
			metadataRepositoryReference = null;
		}

		//unregister the service if we registered it
		if (registrationDefaultManager != null) {
			registrationDefaultManager.unregister();
			registrationDefaultManager = null;
		}
	}

	private void registerEventBus() {
		bus = new ProvisioningEventBus();
		registrationBus = context.registerService(IProvisioningEventBus.SERVICE_NAME, bus, null);
	}

	private void unregisterEventBus() {
		registrationBus.unregister();
		registrationBus = null;
		bus.close();
	}

}
