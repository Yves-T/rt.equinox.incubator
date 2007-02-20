/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.equinox.autoconf.parser;

import org.eclipse.equinox.metatype.MetaTypeMsg;
import org.eclipse.osgi.util.NLS;

public class AutoconfMetaTypeMsg extends MetaTypeMsg {
	private static final String BUNDLE_NAME = "org.eclipse.equinox.dp.rp.autoconf.parser.ExternalMessages"; //$NON-NLS-1$

	public static String INVALID_SYNTAX;
	public static String FAILED_CONFIGADMIN;

	static {
		// initialize resource bundles
		NLS.initializeMessages(BUNDLE_NAME, AutoconfMetaTypeMsg.class);
	}
}