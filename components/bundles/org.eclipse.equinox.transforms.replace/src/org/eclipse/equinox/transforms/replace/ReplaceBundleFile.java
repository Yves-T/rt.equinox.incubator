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

package org.eclipse.equinox.transforms.replace;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.eclipse.equinox.transforms.CSVTransformingBundleFile;
import org.eclipse.equinox.transforms.LazyBundleInputStream;
import org.eclipse.equinox.transforms.LazyBundleInputStream.InputStreamProvider;
import org.eclipse.osgi.baseadaptor.BaseData;
import org.eclipse.osgi.baseadaptor.bundlefile.BundleFile;
import org.osgi.framework.BundleContext;

public class ReplaceBundleFile extends CSVTransformingBundleFile {

	public ReplaceBundleFile(BundleContext context, BaseData data,
			BundleFile delegate, TransformList transformList)
			throws IOException {
		super(context, data, delegate, transformList);
	}

	protected InputStream getInputStream(InputStream inputStream,
			final URL transformerUrl) throws IOException {

		return new LazyBundleInputStream(new InputStreamProvider() {

			public InputStream getInputStream() throws IOException {
				return transformerUrl.openStream();
			}
		});
	}
}