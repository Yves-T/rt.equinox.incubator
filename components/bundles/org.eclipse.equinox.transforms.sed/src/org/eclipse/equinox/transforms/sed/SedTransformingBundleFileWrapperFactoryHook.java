/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.equinox.transforms.sed;

import java.io.IOException;

import org.eclipse.equinox.transforms.TransformingBundleFileWrapperFactoryHook;
import org.eclipse.equinox.transforms.CSVTransformingBundleFile.TransformList;
import org.eclipse.osgi.baseadaptor.BaseData;
import org.eclipse.osgi.baseadaptor.bundlefile.BundleFile;
import org.eclipse.osgi.service.urlconversion.URLConverter;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.Filter;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.util.tracker.ServiceTracker;

public class SedTransformingBundleFileWrapperFactoryHook extends
		TransformingBundleFileWrapperFactoryHook {

	private TransformList transformList;
	private ServiceTracker urlConverterServiceTracker;

	public BundleFile wrapBundleFile(BundleFile bundleFile, Object content,
			BaseData data, boolean base) throws IOException {
		if (urlConverterServiceTracker != null && transformList != null)
			return new SedBundleFile(getContext(), data, bundleFile,
					transformList, urlConverterServiceTracker);
		return null;
	}

	public void frameworkStart(BundleContext context) throws BundleException {
		super.frameworkStart(context);
		Filter filter;
		try {
			filter = context.createFilter("(objectClass="
					+ URLConverter.class.getName() + ")");
			urlConverterServiceTracker = new ServiceTracker(context, filter,
					null);
			urlConverterServiceTracker.open();
		} catch (InvalidSyntaxException e1) {
			e1.printStackTrace();
		}

		try {
			transformList = new TransformList(context,
					SedTransformingBundleFileWrapperFactoryHook.class.getName());
			transformList.open();
		} catch (InvalidSyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void frameworkStop(BundleContext context) throws BundleException {
		super.frameworkStop(context);
		urlConverterServiceTracker.close();
		transformList.close();
	}
}
