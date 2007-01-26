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
package org.eclipse.equinox.frameworkadmin.felix.internal;

import java.io.File;
import java.io.IOException;

import org.eclipse.equinox.frameworkadmin.*;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;

public class FelixFwAdminImpl implements FrameworkAdmin {

	/**
	 * If the currently running fw launch is the one that the FrameworkAdmin object can handle,
	 * return true.
	 *  
	 * @return flag true if the fwAdmin object can handle currently running fw launch. 
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

	FelixFwAdminImpl(BundleContext context) {
		this(context, false);

	}

	FelixFwAdminImpl(BundleContext context, boolean runningFw) {
		this.context = context;
		this.active = true;
		this.runningFw = runningFw;
	}

	void deactivate() {
		active = false;
	}

	public Manipulator getManipulator() {
		return new FelixManipulatorImpl(context, this);
	}

	public Manipulator getRunningManipulator() {
		if (this.runningFw) {
			//KfManipulatorImpl manipulator = new KfManipulatorImpl(context, this);
			//TODO using some MAGIC dependent on Kf implementation, set parameters according to the current running fw.
			return null;
		}
		return null;
	}

	public boolean isActive() {
		return active;
	}

	public Process launch(Manipulator manipulator, File cwd) throws IllegalArgumentException, FrameworkAdminRuntimeException, IOException {
		return new FelixLauncherImpl(context, this).launch(manipulator, cwd);
	}

}
