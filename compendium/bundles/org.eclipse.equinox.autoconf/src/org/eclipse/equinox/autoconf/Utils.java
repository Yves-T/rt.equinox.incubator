/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.equinox.autoconf;

import java.io.File;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.*;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.service.condpermadmin.*;
import org.osgi.service.deploymentadmin.DeploymentPackage;
import org.osgi.service.deploymentadmin.spi.ResourceProcessorException;
import org.osgi.service.log.LogService;
import org.osgi.service.permissionadmin.PermissionInfo;
import org.osgi.util.tracker.ServiceTracker;

public class Utils {

	//private static boolean DOPRIVILEGED = false;

	public static void printoutConditionalPermissions(BundleContext context) {
		ServiceTracker cpTracker = new ServiceTracker(context, ConditionalPermissionAdmin.class.getName(), null);
		cpTracker.open();
		ConditionalPermissionAdmin cpAdmin = (ConditionalPermissionAdmin) cpTracker.getService();
		System.out.println("\n$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$\n");
		if (cpAdmin == null) {
			System.out.println("cpAdmin==null. It might be caused by lack of required Permission");
			return;
		}
		System.out.println("BEGIN printoutConditionalPermissions()");
		try {
			for (Enumeration enumeration = cpAdmin.getConditionalPermissionInfos(); enumeration.hasMoreElements();) {
				ConditionalPermissionInfo cpInfo = (ConditionalPermissionInfo) enumeration.nextElement();
				System.out.println("condtionpermissionInfo:\n\tname=" + cpInfo.getName());
				ConditionInfo[] cInfos = cpInfo.getConditionInfos();
				for (int i = 0; i < cInfos.length; i++)
					System.out.println("\tcondtionInfo[" + i + "]=" + cInfos[i]);
				PermissionInfo[] pInfos = cpInfo.getPermissionInfos();
				for (int i = 0; i < pInfos.length; i++)
					System.out.println("\tpermInfo[" + i + "]=" + pInfos[i]);
			}
		} catch (SecurityException se) {
			se.printStackTrace();
		}
		System.out.println("END printoutConditionalPermissions()\n" + "$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$\n");

	}

	public static Bundle getBundleFromDpDoPrivileged(DeploymentPackage deploymentPackage, String bundleSymbolicName) {
		final DeploymentPackage dp = deploymentPackage;
		final String bsn = bundleSymbolicName;
		return (Bundle) AccessController.doPrivileged(new PrivilegedAction() {
			public java.lang.Object run() {
				return dp.getBundle(bsn);
			}
		});
	}

	public static String getBundleLocationDoPrivileged(Bundle bundle) {
		final Bundle b = bundle;
		return (String) AccessController.doPrivileged(new PrivilegedAction() {
			public java.lang.Object run() {
				return b.getLocation();
			}
		});
	}

	public static File getDataFileDoPrivileged(BundleContext context, String filePath) {
		final BundleContext bc = context;
		final String path = filePath;
		return (File) AccessController.doPrivileged(new PrivilegedAction() {
			public java.lang.Object run() {
				return bc.getDataFile(path);
			}
		});
	}

	public static String getMsgStOfConfiguration(Dictionary props) {
		StringBuffer sb = new StringBuffer(10);
		for (Enumeration enumeration = props.keys(); enumeration.hasMoreElements();) {
			String key = (String) enumeration.nextElement();
			Object value = props.get(key);
			if (value instanceof Object[]) {
				Object[] array = (Object[]) value;
				sb.append("\t{" + key + ",<");
				for (int j = 0; j < array.length; j++) {
					if (j != 0)
						sb.append(",");
					sb.append(array[j]);
				}
				sb.append(">:" + value.getClass().getName() + "}\n");
			} else
				sb.append("\t{" + key + "," + value + ":" + value.getClass().getName() + "}\n");
		}
		return sb.toString();
	}

	public static void errorHandler(boolean optional, String msg) throws ResourceProcessorException {
		errorHandler(optional, msg, null);
	}

	public static void errorHandler(boolean optional, Exception ex) throws ResourceProcessorException {
		errorHandler(optional, null, ex);
	}

	public static void errorHandler(boolean optional, String msg, Exception ex) throws ResourceProcessorException {
		if (optional) {
			Log.log(LogService.LOG_ERROR, msg, ex);
			return;
		}
		// Log.log(LogService.LOG_WARNING, msg, ex);
		throw new ResourceProcessorException(ResourceProcessorException.CODE_OTHER_ERROR, msg, ex);
	}

	public static Hashtable createHashtable(int size) {
		return new Hashtable((int) Math.ceil((double) size / 0.75));
	}
}
