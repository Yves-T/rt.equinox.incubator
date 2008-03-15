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
package org.eclipse.equinox.security.sample.ssl;

import java.io.IOException;
import java.security.cert.*;
import javax.net.ssl.X509TrustManager;
import org.eclipse.equinox.security.sample.AuthAppPlugin;
import org.eclipse.osgi.service.security.TrustEngine;

public class MyX509TrustManager implements X509TrustManager {

	public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
		throw new UnsupportedOperationException();
	}

	public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
		// use the trust engines certs
		TrustEngine[] engines = AuthAppPlugin.getTrustEngines();
		Certificate foundCert = null;
		for (int i = 0; i < chain.length; i++) {
			try {
				foundCert = engines[i].findTrustAnchor(chain);
				if (null != foundCert)
					break;
			} catch (IOException e) {
				CertificateException ce = new CertificateException();
				ce.initCause(e);
				throw ce;
			}
		}
		if (null == foundCert)
			throw new CertificateException("Not a trust certificate found!");
	}

	public X509Certificate[] getAcceptedIssuers() {
		throw new UnsupportedOperationException();
	}

}
