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

package org.eclipse.equinox.examples.servlet.apps;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import org.osgi.framework.*;
import org.osgi.service.application.ApplicationDescriptor;
import org.osgi.service.application.ApplicationHandle;
import org.osgi.service.packageadmin.PackageAdmin;
import org.osgi.util.tracker.ServiceTracker;

public class Activator implements BundleActivator {
	private volatile static ServiceTracker appDescriptors;
	private volatile static ServiceTracker appHandles;
	private volatile static BundleContext context;

	public void start(BundleContext context) throws Exception {
		appDescriptors = new ServiceTracker(context, context.createFilter("(&(objectClass=" + ApplicationDescriptor.class.getName() + ")(eclipse.application.type=any.thread))"), null);
		appDescriptors.open();
		appHandles = new ServiceTracker(context, context.createFilter("(&(objectClass=" + ApplicationHandle.class.getName() + ")(eclipse.application.type=any.thread))"), null);
		appHandles.open();
		Activator.context = context;
	}

	public void stop(BundleContext context) throws Exception {
		if (appDescriptors != null)
			appDescriptors.close();
		if (appHandles != null)
			appHandles.close();
		appDescriptors = null;
		appHandles = null;
		Activator.context = null;
	}

	static ApplicationDescriptor[] getApplications() {
		ServiceTracker apps = appDescriptors;
		if (apps == null)
			return new ApplicationDescriptor[0];
		Object[] objs = apps.getServices();
		if (objs == null)
			return new ApplicationDescriptor[0];
		ApplicationDescriptor[] results = new ApplicationDescriptor[objs.length];
		System.arraycopy(objs, 0, results, 0, objs.length);
		Arrays.sort(results, new Comparator() {
			public int compare(Object o1, Object o2) {
				ApplicationDescriptor app1 = (ApplicationDescriptor) o1;
				ApplicationDescriptor app2 = (ApplicationDescriptor) o2;
				return app1.getApplicationId().compareTo(app2.getApplicationId());
			}
		});
		return results;
	}

	static ApplicationHandle[] getHandles() {
		ServiceTracker handles = appHandles;
		if (handles == null)
			return new ApplicationHandle[0];
		Object[] objs = handles.getServices();
		if (objs == null)
			return new ApplicationHandle[0];
		ApplicationHandle[] results = new ApplicationHandle[objs.length];
		System.arraycopy(objs, 0, results, 0, objs.length);
		return results;
	}

	public static void installBundles(String path) throws IOException, BundleException {
		if (path == null || path.length() == 0)
			return;
		try {
			File dir = new File(path);
			if (dir.isFile()) {
				installBundle(dir);
				return;
			}
			File[] files = dir.listFiles();
			for (int i = 0; i < files.length; i++)
				if (files[i].isFile())
					installBundle(files[i]);
		} finally {
			ServiceReference ref = context.getServiceReference(PackageAdmin.class.getName());
			if (ref == null)
				return;
			PackageAdmin pa = (PackageAdmin) context.getService(ref);
			if (pa == null)
				return;
			try {
				pa.resolveBundles(null);
				Thread.sleep(500);
			} catch (InterruptedException e) {
				// nothing
			} finally {
				context.ungetService(ref);
			}
		}
	}

	public static void installBundle(File file) throws BundleException, IOException {
		BundleContext bc = Activator.context;
		if (bc == null)
			return;
		String location = "reference:file:" + file.getCanonicalPath();
		bc.installBundle(location);
	}
}
