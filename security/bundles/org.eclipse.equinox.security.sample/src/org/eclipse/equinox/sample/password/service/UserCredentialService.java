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
package org.eclipse.equinox.sample.password.service;

import org.eclipse.equinox.sample.password.model.UserCredential;
import org.eclipse.equinox.security.sample.AuthAppPlugin;
import org.eclipse.equinox.security.storage.StorageException;

public class UserCredentialService implements IUserCredentialService {

	public boolean addCredential(UserCredential userCredential) {
		try {
			AuthAppPlugin.getSecurePreferences().put(userCredential.getUsername() + "::" + userCredential.getServerURL(), userCredential.getPassword(), true);
			return true;
		} catch (StorageException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}

	public boolean removeCredential(UserCredential userCredential) {
		AuthAppPlugin.getSecurePreferences().remove(userCredential.getUsername() + "::" + userCredential.getServerURL());
		return true;
	}

	public void updateCredential(UserCredential oldCredential, UserCredential newCredential) {

	}

}
