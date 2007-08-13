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

import java.security.InvalidAlgorithmParameterException;
import java.security.KeyStore;
import java.security.KeyStoreException;

import javax.net.ssl.ManagerFactoryParameters;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactorySpi;

public class NullTrustManagerFactory extends TrustManagerFactorySpi {

	protected void engineInit( KeyStore keyStore)
		throws KeyStoreException {
		
		throw new KeyStoreException( );
	}

	protected void engineInit( ManagerFactoryParameters parameters)
		throws InvalidAlgorithmParameterException {

		throw new InvalidAlgorithmParameterException( );
	}

	protected TrustManager[] engineGetTrustManagers( ) {

		return new TrustManager[] {new NullTrustManager( )};
	}
}
