/*******************************************************************************
 * Copyright (c) 2007, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.equinox.frameworkadmin.felix.internal;

public class FelixConstants {
	public final static String PROP_FELIX_DEPENDENT_PREFIX = "felix."; //$NON-NLS-1$
	public final static String PROP_FELIX_FWPRIVATE_DIR = "felix.cache.profiledir"; //$NON-NLS-1$

	public final static String PROP_BUNDLES_STARTLEVEL = "felix.startlevel.bundle"; //$NON-NLS-1$
	public final static String PROP_INITIAL_STARTLEVEL = "felix.startlevel.framework"; //$NON-NLS-1$
	public final static String PROP_BUNDLE_INSTALL_PREFIX = "felix.auto.install."; //$NON-NLS-1$
	public final static String PROP_BUNDLE_START_PREFIX = "felix.auto.start."; //$NON-NLS-1$
	public final static String JVMPROP_FWCONFIGFILE_URL = "felix.config.properties"; //$NON-NLS-1$
	public final static String JVMPROP_SYSTEMPROPS_CONFIGFILE_URL = "felix.system.properties"; //$NON-NLS-1$

	public static final String FW_VERSION = "2.0.0"; //$NON-NLS-1$
	public static final String FW_NAME = "Felix"; //$NON-NLS-1$
	public static final String LAUNCHER_NAME = "java -jar"; //$NON-NLS-1$
	public static final String LAUNCHER_VERSION = FW_VERSION;

	public static final String VMARGS_COMMAND = "-vmargs"; //$NON-NLS-1$
	public static final String VM_COMMAND = "-vm"; //$NON-NLS-1$
	public static final String XARGS_INIT = "-init"; //$NON-NLS-1$
	public static final String XARGS_OPTION = "-xargs"; //$NON-NLS-1$
	public static final String DEFAULT_FW_PRIVATE_AREA = ".felix"; //$NON-NLS-1$
	public static final String DEFAULT_FW_CONFIGLOCATION = "conf"; //$NON-NLS-1$
	public static final String DEFAULT_FW_CONFIGFILE = "config.properties"; //$NON-NLS-1$
	public static final String DEFAULT_SYSTEMPROP_CONFIGFILE = "system.properties"; //$NON-NLS-1$
	public static final int DEFAULT_INITIAL_BUNDLE_SL = 1;
	public static final String FELIX_BASIC_PROPERTIES_FILE = "felixBasic.properties"; //$NON-NLS-1$

}
