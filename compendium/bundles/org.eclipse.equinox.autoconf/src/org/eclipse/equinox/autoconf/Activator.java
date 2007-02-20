/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.equinox.autoconf;

import java.util.Dictionary;

import org.osgi.framework.*;
import org.osgi.service.deploymentadmin.spi.ResourceProcessor;
import org.osgi.service.log.LogService;

public class Activator implements BundleActivator {

	ResourceProcessor processor;

	BundleContext context;

	ServiceRegistration registration;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		Log.init(context);
		Log.log(LogService.LOG_INFO, this, "start(context)", "BEGIN");
		this.context = context;
		AutoconfResourceProcessor processor = new AutoconfResourceProcessor(context);

		Dictionary properties = Utils.createHashtable(3);
		properties.put(Constants.SERVICE_PID, "org.osgi.deployment.rp.autoconf");
		properties.put(Constants.SERVICE_VENDOR, "Equinox Project, Eclipse Foundation");
		properties.put(Constants.SERVICE_DESCRIPTION, "Implementation of Auto Configuration Specification 1.0");

		registration = context.registerService(ResourceProcessor.class.getName(), processor, properties);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		Log.log(LogService.LOG_INFO, this, "stop(context)", "BEGIN");
		registration.unregister();
		context = null;
	}

}
