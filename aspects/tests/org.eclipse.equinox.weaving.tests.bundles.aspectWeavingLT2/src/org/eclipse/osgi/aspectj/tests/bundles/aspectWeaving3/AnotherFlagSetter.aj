/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   David Knibb               initial implementation      
 *******************************************************************************/

package org.eclipse.osgi.aspectj.tests.bundles.aspectWeaving3;

import org.eclipse.osgi.aspectj.tests.bundles.spareBundle.Advisable;

public abstract aspect AnotherFlagSetter {

	public abstract pointcut scope();
	
	pointcut access ( Advisable fh ) :
		execution (public void access()) && scope() && this(fh);
	
	/**
	 * After the access pointcut (above) 
	 * set the flag in the target class 
	 */
	after ( Advisable fh ) : access ( fh ) {
		System.out.println("AnotherFlagSetter aspect - setting flag");
		fh.setFlag();
	}
}
