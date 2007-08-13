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
package org.eclipse.equinox.internal.security.provider.ssl;

import java.net.Socket;
import java.security.Principal;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;

import javax.net.ssl.X509KeyManager;

public class NullKeyManager implements X509KeyManager {

	public String[] getClientAliases( String arg0, Principal[] arg1) {

		return new String[] {};
	}

	public String chooseClientAlias( String[] arg0, Principal[] arg1, Socket arg2) {

		return null;
	}

	public String[] getServerAliases( String arg0, Principal[] arg1) {

		return new String[] {};
	}

	public String chooseServerAlias( String arg0, Principal[] arg1, Socket arg2) {

		return null;
	}

	public X509Certificate[] getCertificateChain( String arg0) {

		return null;
	}

	public PrivateKey getPrivateKey( String arg0) {

		return null;
	}
}
