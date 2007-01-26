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

public class KfConstants {
	public final static String PROP_KF_DEPENDENT_PREFIX = "org.knopflerfish.";
	public final static String PROP_KF_BUNDLEBASE_DIR = "org.knopflerfish.gosg.jars";
	public final static String PROP_KF_FW_PERSISTENT_DATA_LOC = "org.osgi.framework.dir";
	//	private final static String DEFAULT_BUNDLE_SEARCH_DIR = "jars";

	public final static String FW_NAME = "Knopflerfish";
	public final static String FW_VERSION = "2.0";
	public static final String LAUNCHER_NAME = "java -jar";
	public static final String LAUNCHER_VERSION = FW_VERSION;

	public static final String VMARGS_COMMAND = "-vmargs";
	public static final String VM_COMMAND = "-vm";
	public static final String XARGS_INIT = "-init";
	public static final String XARGS_OPTION = "-xargs";
	public static final String DEFAULT_FW_PRIVATE_AREA = "fwdir";
	public static final int DEFAULT_INITIAL_BUNDLE_SL = 1;
}
