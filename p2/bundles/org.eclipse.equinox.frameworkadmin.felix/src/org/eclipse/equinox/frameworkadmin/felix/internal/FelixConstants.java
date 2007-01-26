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

public class FelixConstants {
	public final static String PROP_FELIX_DEPENDENT_PREFIX = "felix.";
	//	public final static String PROP_FELIX_BUNDLEBASE_DIR = "org.knopflerfish.gosg.jars";
	public final static String PROP_FELIX_FWPRIVATE_DIR = "felix.cache.profiledir";

	public final static String PROP_BUNDLES_STARTLEVEL = "felix.startlevel.bundle";
	public final static String PROP_INITIAL_STARTLEVEL = "felix.startlevel.framework";
	public final static String PROP_BUNDLE_INSTALL_PREFIX = "felix.auto.install.";
	public final static String PROP_BUNDLE_START_PREFIX = "felix.auto.start.";
	public final static String JVMPROP_FWCONFIGFILE_URL = "felix.config.properties";
	public final static String JVMPROP_SYSTEMPROPS_CONFIGFILE_URL = "felix.system.properties";

	//	private final static String DEFAULT_BUNDLE_SEARCH_DIR = "jars";
	public static final String FW_VERSION = "0.8.0.SNAPSHOT";
	public static final String FW_NAME = "Felix";
	public static final String LAUNCHER_NAME = "java -jar";
	public static final String LAUNCHER_VERSION = FW_VERSION;

	public static final String VMARGS_COMMAND = "-vmargs";
	public static final String VM_COMMAND = "-vm";
	public static final String XARGS_INIT = "-init";
	public static final String XARGS_OPTION = "-xargs";
	public static final String DEFAULT_FW_PRIVATE_AREA = ".felix";
	public static final String DEFAULT_FW_CONFIGLOCATION = "conf";
	public static final String DEFAULT_FW_CONFIGFILE = "config.properties";
	public static final String DEFAULT_SYSTEMPROP_CONFIGFILE = "system.properties";
	public static final int DEFAULT_INITIAL_BUNDLE_SL = 1;
	public static final String FELIX_BASIC_PROPERTIES_FILE = "felixBasic.properties";

}
