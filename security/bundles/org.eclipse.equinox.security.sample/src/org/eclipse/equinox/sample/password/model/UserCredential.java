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
package org.eclipse.equinox.sample.password.model;

/*
 * A class that represents the user credential model for this app
 */
public class UserCredential {

	private String serverURL;
	private String username;
	private String password;

	public UserCredential(String serverURL, String username, String password) {
		this.serverURL = serverURL;
		this.username = username;
		this.password = password;
	}

	public String getServerURL() {
		return serverURL;
	}

	public String getUsername() {
		return username;
	}

	public String getPassword() {
		return password;
	}
}
