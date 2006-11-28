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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import org.eclipse.equinox.transforms.CSVTransformingBundleFile;
import org.eclipse.equinox.transforms.ProcessPipeInputStream;
import org.eclipse.osgi.baseadaptor.BaseData;
import org.eclipse.osgi.baseadaptor.bundlefile.BundleFile;
import org.eclipse.osgi.service.urlconversion.URLConverter;
import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;

public class SedBundleFile extends CSVTransformingBundleFile {

	private ServiceTracker urlServiceTracker;

	public SedBundleFile(BundleContext context, BaseData data,
			BundleFile delegate, TransformList transformList,
			ServiceTracker urlServiceTracker) throws IOException {
		super(context, data, delegate, transformList);
		this.urlServiceTracker = urlServiceTracker;
	}

	protected InputStream getInputStream(InputStream inputStream,
			URL transformerUrl) throws IOException {

		URLConverter converter = (URLConverter) urlServiceTracker.getService();
		if (converter == null)
			return null;

		try {
			URL convertedURL = converter.toFileURL(transformerUrl);
			URI convertedURI = new URI(convertedURL.toExternalForm());
			File commandFile = new File(convertedURI);

			return new SedInputStream(inputStream, commandFile);
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}

		return null;
	}
}

class SedInputStream extends ProcessPipeInputStream {

	private File commandFile;

	public SedInputStream(InputStream original, File commandFile) {
		super(original);
		this.commandFile = commandFile;
	}

	protected String getCommandString() {
		return "sed -f " + commandFile.getName();
	}

	protected File getWorkingDirectory() {
		return commandFile.getParentFile();
	}

}
