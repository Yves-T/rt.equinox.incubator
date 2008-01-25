/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.equinox.security.sample;

import org.eclipse.swt.graphics.Point;
import org.eclipse.ui.application.*;

public class AuthWorkbenchWindowAdvisor extends WorkbenchWindowAdvisor {

	private static final String AUTHENTICATED_APPLICATION = "Authenticated Application"; //$NON-NLS-1$

	public AuthWorkbenchWindowAdvisor(IWorkbenchWindowConfigurer configurer) {
		super(configurer);
		// TODO Auto-generated constructor stub
	}

	public ActionBarAdvisor createActionBarAdvisor(IActionBarConfigurer configurer) {
		return new AuthActionBarAdvisor(configurer);
	}

	public void preWindowOpen() {
		//super.preWindowOpen( configurer);
		this.getWindowConfigurer().setShowMenuBar(true);
		this.getWindowConfigurer().setInitialSize(new Point(640, 480));
		this.getWindowConfigurer().setShowCoolBar(false);
		this.getWindowConfigurer().setShowStatusLine(true);
		this.getWindowConfigurer().setTitle(AUTHENTICATED_APPLICATION);
	}
}
