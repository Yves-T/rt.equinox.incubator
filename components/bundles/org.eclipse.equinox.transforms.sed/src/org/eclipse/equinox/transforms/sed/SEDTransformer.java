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

package org.eclipse.equinox.transforms.sed;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import org.eclipse.equinox.transforms.ProcessPipeInputStream;
import org.eclipse.osgi.framework.log.FrameworkLog;
import org.eclipse.osgi.framework.log.FrameworkLogEntry;
import org.eclipse.osgi.service.urlconversion.URLConverter;
import org.osgi.framework.FrameworkEvent;
import org.osgi.util.tracker.ServiceTracker;

public class SEDTransformer {

	private ServiceTracker urlService;
	private ServiceTracker logTracker;

	public SEDTransformer(ServiceTracker urlConverterServiceTracker,
			ServiceTracker logTracker) {
		this.urlService = urlConverterServiceTracker;
		this.logTracker = logTracker;
	}

	public InputStream getInputStream(InputStream inputStream,
			URL transformerUrl) throws IOException {

		URLConverter converter = (URLConverter) urlService.getService();
		if (converter == null)
			return null;

		try {
			URL convertedURL = converter.toFileURL(transformerUrl);
			URI convertedURI = new URI(convertedURL.getProtocol(), convertedURL
					.getUserInfo(), convertedURL.getHost(), convertedURL
					.getPort(), convertedURL.getPath(),
					convertedURL.getQuery(), convertedURL.getRef());
			File commandFile = new File(convertedURI);

			return new SedInputStream(inputStream, commandFile);
		} catch (URISyntaxException e) {

			FrameworkLog log = (FrameworkLog) logTracker.getService();
			if (log == null) {
				if (e != null)
					e.printStackTrace();
			} else {
				FrameworkLogEntry entry = new FrameworkLogEntry(
						"org.eclipse.equinox.transforms.xslt",
						FrameworkEvent.ERROR, 0, "Could not convert URL", 0, e,
						null);
				log.log(entry);
			}

		}

		return null;
	}

	public static boolean isSedAvailable() {
		try {
			Process process = Runtime.getRuntime().exec("sed", null, null);
			process.destroy();
			return true;
		} catch (IOException e) {
			return false;
		}
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