/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.equinox.internal.transforms.sed.provisional;

import java.io.*;
import java.net.*;
import org.eclipse.equinox.transforms.ProcessPipeInputStream;
import org.eclipse.osgi.framework.log.FrameworkLog;
import org.eclipse.osgi.framework.log.FrameworkLogEntry;
import org.eclipse.osgi.service.urlconversion.URLConverter;
import org.osgi.framework.FrameworkEvent;
import org.osgi.util.tracker.ServiceTracker;

public class SEDTransformer {

	private ServiceTracker urlService;
	private ServiceTracker logTracker;

	public SEDTransformer(ServiceTracker urlConverterServiceTracker, ServiceTracker logTracker) {
		this.urlService = urlConverterServiceTracker;
		this.logTracker = logTracker;
	}

	public InputStream getInputStream(InputStream inputStream, URL transformerUrl) throws IOException {

		URLConverter converter = (URLConverter) urlService.getService();
		if (converter == null)
			return null;

		try {
			URL convertedURL = converter.toFileURL(transformerUrl);
			URI convertedURI = new URI(convertedURL.getProtocol(), convertedURL.getUserInfo(), convertedURL.getHost(), convertedURL.getPort(), convertedURL.getPath(), convertedURL.getQuery(), convertedURL.getRef());
			File commandFile = new File(convertedURI);

			return new ProcessPipeInputStream(inputStream, "sed -f " + commandFile.getName(), null, commandFile.getParentFile()); //$NON-NLS-1$
		} catch (URISyntaxException e) {

			FrameworkLog log = (FrameworkLog) logTracker.getService();
			if (log == null) {
				if (e != null)
					e.printStackTrace();
			} else {
				FrameworkLogEntry entry = new FrameworkLogEntry("org.eclipse.equinox.transforms.sed", FrameworkEvent.ERROR, 0, "Could not convert URL", 0, e, null); //$NON-NLS-1$ //$NON-NLS-2$
				log.log(entry);
			}

		}

		return null;
	}

	public static boolean isSedAvailable() {
		try {
			Process process = Runtime.getRuntime().exec("sed", null, null); //$NON-NLS-1$
			process.destroy();
			return true;
		} catch (IOException e) {
			return false;
		}
	}
}