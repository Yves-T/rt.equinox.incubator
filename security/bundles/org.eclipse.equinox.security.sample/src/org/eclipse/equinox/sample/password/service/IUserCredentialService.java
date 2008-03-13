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

public interface IUserCredentialService {

	public static final IUserCredentialService userCredentialService = new UserCredentialService();

	boolean addCredential(UserCredential userCredential);

	boolean removeCredential(UserCredential userCredential);

	void updateCredential(UserCredential oldCredential, UserCredential newCredential);

}
