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

import org.eclipse.equinox.configuratormanipulator.ConfiguratorManipulator;
import org.eclipse.equinox.frameworkadmin.*;
import org.eclipse.equinox.frameworkadmin.knopflerfish.KfConfigData;
import org.eclipse.equinox.internal.frameworkadmin.utils.SimpleBundlesState;
import org.eclipse.equinox.internal.frameworkadmin.utils.Utils;
import org.osgi.framework.*;
import org.osgi.service.log.LogService;
import org.osgi.util.tracker.ServiceTracker;

public class KfManipulatorImpl implements Manipulator {
	private static final String SYSTEMBUNDLE_VENDOR = "Knopflerfish";
	private static final String SYSTEMBUNDLE_NAME = "frameworkbundle";
	KfConfigData configData = new KfConfigData(KfConstants.FW_NAME, KfConstants.FW_VERSION, null, null);
	LauncherData launcherData = new LauncherData(KfConstants.FW_NAME, KfConstants.FW_VERSION, null, null);

	BundleContext context = null;
	BundlesState bundleState = null;

	ServiceTracker cmTracker;
	int trackingCount = -1;

	ConfiguratorManipulator configuratorManipulator;

	KfFwAdminImpl fwAdmin = null;

	KfManipulatorImpl(BundleContext context, KfFwAdminImpl ManipulatorAdmin) {
		this.context = context;
		this.fwAdmin = ManipulatorAdmin;
		if (context != null) {
			cmTracker = new ServiceTracker(context, ConfiguratorManipulator.class.getName(), null);
			cmTracker.open();
		}
	}

	public BundlesState getBundlesState() throws FrameworkAdminRuntimeException {
		return new SimpleBundlesState(fwAdmin, this, SYSTEMBUNDLE_NAME, SYSTEMBUNDLE_VENDOR);
		//		return new KfBundlesState(context, ManipulatorAdmin, this);
	}

	public ConfigData getConfigData() throws FrameworkAdminRuntimeException {
		return configData;
	}

	public BundleInfo[] getExpectedState() throws IllegalArgumentException, IOException, FrameworkAdminRuntimeException {
		Log.log(LogService.LOG_DEBUG, this, "getExpectedState()", "BEGIN");
		SimpleBundlesState.checkAvailability(fwAdmin);

		BundlesState bundleState = this.getBundlesState();
		bundleState.resolve(true);
		return bundleState.getExpectedState();
	}

	public LauncherData getLauncherData() throws FrameworkAdminRuntimeException {
		return launcherData;
	}

	public void initialize() {
		Log.log(LogService.LOG_DEBUG, this, "initialize()", "BEGIN");
		configData.initialize();
		launcherData.initialize();
	}

	// Load parameters from LauncherConfigFile, FwConfigFile, and ConfiguratorConfigFile if required.
	// The parameter has been set will be updated.
	public void load() throws IOException, FrameworkAdminRuntimeException {
		Log.log(LogService.LOG_DEBUG, this, "load()", "BEGIN");
		SimpleBundlesState.checkAvailability(fwAdmin);

		// current implementation for KF doesn't support launcher.

		File fwConfigFile = launcherData.getFwConfigLocation();
		if (fwConfigFile == null) {
			//TODO refine this algorithm according to the KF behaivior.
			// Default fwConfigFile for Eclipse.exe
			File home = launcherData.getHome();
			fwConfigFile = new File(home, "init.xargs");
		}
		KfFwConfigFileParser parser = new KfFwConfigFileParser();
		parser.readFwConfigFile(configData, fwConfigFile);

		// TODO current impl doesn't support it.
		//		BundlesState bundleState = this.getBundlesState();
		//		BundleInfo[] newBundleInfos = bundleState.getExpectedState();
		//		configData.setBundles(newBundleInfos);
		setConfiguratorManipulator();
		if (this.configuratorManipulator == null)
			return;
		configuratorManipulator.updateBundles(this);
		return;
	}

	// Save all parameter in memory into proper config files.
	public void save(boolean backup) throws IOException, FrameworkAdminRuntimeException {
		Log.log(LogService.LOG_DEBUG, this, "save()", "BEGIN");
		SimpleBundlesState.checkAvailability(fwAdmin);

		// current implementation for KF doesn't support launcher.

		File fwConfigFile = launcherData.getFwConfigLocation();
		if (fwConfigFile == null)
			throw new IllegalStateException("fwConfigFile is not set.");

		setConfiguratorManipulator();

		BundleInfo[] newBInfo = null;
		if (configuratorManipulator != null) // Optimize BundleInfo[] 
			newBInfo = configuratorManipulator.save(this, backup);
		else
			newBInfo = configData.getBundles();
		// Save FwConfigFile
		KfFwConfigFileParser parser = new KfFwConfigFileParser();
		parser.saveConfigs(newBInfo, configData, fwConfigFile, backup);
	}

	public void setConfigData(ConfigData configData) {
		this.configData.initialize();
		this.configData.setInitialBundleStartLevel(configData.getInitialBundleStartLevel());
		this.configData.setBeginningFwStartLevel(configData.getBeginingFwStartLevel());
		BundleInfo[] bInfos = configData.getBundles();
		for (int i = 0; i < bInfos.length; i++)
			this.configData.addBundle(bInfos[i]);
		this.configData.setFwIndependentProps(configData.getFwIndependentProps());
		if (this.configData.getFwName().equals(configData.getFwName()))
			if (this.configData.getFwVersion().equals(configData.getFwVersion())) {
				// TODO refine the algorithm to copying fw dependent props.
				//  configData.getFwName()/getFwVersion()/
				//	getLauncherName()/getLauncherVersion() might be taken into consideration. 
				this.configData.setFwDependentProps(configData.getFwDependentProps());
			}
	}

	/**
	 * 1. get all ServiceReferences of ConfiguratorManipulator.   
	 * 2. Check if there any ConfiguratorBundle in the Bundles list that can be manipulated by 
	 * 	the available ConfiguratorManipulators.
	 * 3. Choose the one that will be firstly started among them.
	 * 4. set the object that corresponds to the chosen ConfiguratorBundle.  
	 * 
	 */
	private void setConfiguratorManipulator() {
		if (context == null) {
			this.configuratorManipulator = this.fwAdmin.getConfiguratorManipulator();
			return;
		}

		ServiceReference[] references = cmTracker.getServiceReferences();
		int count = cmTracker.getTrackingCount();
		if (count == this.trackingCount)
			return;
		this.trackingCount = count;

		BundleInfo[] bInfos = configData.getBundles();
		int initialBSL = configData.getInitialBundleStartLevel();
		bInfos = Utils.sortBundleInfos(bInfos, initialBSL);
		//int index = -1;	
		configuratorManipulator = null;
		for (int i = 0; i < bInfos.length; i++) {
			//String location = bInfos[i].getLocation();
			if (!bInfos[i].isMarkedAsStarted())
				continue;
			for (int j = 0; j < references.length; j++)
				if (references[j].getProperty(ConfiguratorManipulator.SERVICE_PROP_KEY_CONFIGURATOR_BUNDLESYMBOLICNAME).equals(Utils.getManifestMainAttributes(bInfos[i].getLocation(), Constants.BUNDLE_SYMBOLICNAME))) {
					configuratorManipulator = (ConfiguratorManipulator) cmTracker.getService(references[j]);
					break;
				}
			if (configuratorManipulator != null)
				break;
		}
	}

	public void setLauncherData(LauncherData launcherData) {
		this.launcherData.initialize();
		this.launcherData.setFwConfigLocation(launcherData.getFwConfigLocation());
		this.launcherData.setFwPersistentDataLocation(launcherData.getFwPersistentDataLocation(), launcherData.isClean());
		this.launcherData.setJvm(launcherData.getJvm());
		this.launcherData.setJvmArgs(launcherData.getJvmArgs());
		if (this.launcherData.getFwName().equals(launcherData.getFwName()))
			if (this.launcherData.getFwVersion().equals(launcherData.getFwVersion())) {
				// TODO launcherData.getFwName()/getFwVersion()/
				//	getLauncherName()/getLauncherVersion() might be taken into consideration
				//  for copying . 
				this.launcherData.setFwJar(launcherData.getFwJar());
			}
	}
}
