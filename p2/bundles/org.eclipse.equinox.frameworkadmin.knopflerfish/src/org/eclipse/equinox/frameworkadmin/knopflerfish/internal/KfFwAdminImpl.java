/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.equinox.frameworkadmin.knopflerfish.internal;

import java.io.File;
import java.io.IOException;

import org.eclipse.equinox.configurator.ConfiguratorManipulator;
import org.eclipse.equinox.configurator.ConfiguratorManipulatorFactory;
import org.eclipse.equinox.frameworkadmin.*;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;

public class KfFwAdminImpl implements FrameworkAdmin {

	/**
	 * If the currently running fw launch is the one that the FrameworkAdmin object can handle,
	 * return true.
	 *  
	 * @return flag true if the ManipulatorAdmin object can handle currently running fw launch. 
	 */
	static boolean isRunningFw(BundleContext context) {
		//TODO implementation for Eclipse.exe and for Equinox
		if (!context.getProperty(Constants.FRAMEWORK_VENDOR).equals("Eclipse.org"))
			return false;
		//TODO decide if this version can be supported by this bundle.
		//Dictionary header = context.getBundle(0).getHeaders();
		//String versionSt = (String) header.get("Bundle-Version");

		// TODO need to identify the version of eclipse.exe used for this launch, if used. 
		//		String eclipseCommandsSt = context.getProperty(EquinoxConstants.PROP_ECLIPSE_COMMANDS);
		//	StringTokenizer tokenizer = new StringTokenizer(eclipseCommandsSt,"\n");

		return false;
	}

	BundleContext context = null;

	boolean active = false;

	private boolean runningFw = false;
	private ConfiguratorManipulator configuratorManipulator = null;

	KfFwAdminImpl() {
		this(null, false);
	}

	KfFwAdminImpl(BundleContext context) {
		this(context, false);

	}

	KfFwAdminImpl(BundleContext context, boolean runningFw) {
		this.context = context;
		this.active = true;
		this.runningFw = runningFw;
	}

	KfFwAdminImpl(String configuratorManipulatorFactoryName) throws InstantiationException, IllegalAccessException, ClassNotFoundException {
		this.context = null;
		this.active = true;
		this.runningFw = false;
		//		this.configuratorManipulatorFactoryName = configuratorManipulatorFactoryName;
		loadConfiguratorManipulator(configuratorManipulatorFactoryName);
	}

	private void loadConfiguratorManipulator(String configuratorManipulatorFactoryName) throws InstantiationException, IllegalAccessException, ClassNotFoundException {
		if (configuratorManipulatorFactoryName == null)
			this.configuratorManipulator = null;
		else
			this.configuratorManipulator = ConfiguratorManipulatorFactory.getInstance(configuratorManipulatorFactoryName);
		return;
	}

	void deactivate() {
		active = false;
	}

	public Manipulator getManipulator() {
		return new KfManipulatorImpl(context, this);
	}

	public Manipulator getRunningManipulator() {
		if (this.runningFw) {
			//KfManipulatorImpl Manipulator = new KfManipulatorImpl(context, this);
			//TODO using some MAGIC dependent on Kf implementation, set parameters according to the current running fw.
			return null;
		}
		return null;
	}

	public boolean isActive() {
		return active;
	}

	public Process launch(Manipulator Manipulator, File cwd) throws IllegalArgumentException, FrameworkAdminRuntimeException, IOException {
		return new KfLauncherImpl(context, this).launch(Manipulator, cwd);
	}

	public ConfiguratorManipulator getConfiguratorManipulator() {
		return configuratorManipulator;
	}

}
