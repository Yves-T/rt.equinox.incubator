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

import java.security.InvalidAlgorithmParameterException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactorySpi;
import javax.net.ssl.ManagerFactoryParameters;

public class NullKeyManagerFactory extends KeyManagerFactorySpi {

	protected void engineInit( KeyStore keyStore, char[] password)
		throws KeyStoreException, NoSuchAlgorithmException, UnrecoverableKeyException {
		
	}

	protected void engineInit( ManagerFactoryParameters parameters)
		throws InvalidAlgorithmParameterException {

	}

	protected KeyManager[] engineGetKeyManagers( ) {
		return new KeyManager[] {};
	}
}
