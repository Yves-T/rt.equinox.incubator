/*******************************************************************************
 * Copyright (c) 2005, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.equinox.internal.security.ssl;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.X509TrustManager;

public class NullTrustManager implements X509TrustManager {

	public void checkClientTrusted( X509Certificate[] chain, String authType)
		throws CertificateException {
		
		throw new CertificateException( );
	}

	public void checkServerTrusted(X509Certificate[] chain, String authType)
		throws CertificateException {
		
		throw new CertificateException( );
	}

	public X509Certificate[] getAcceptedIssuers( ) {
		return new X509Certificate[] { };
	}
}
