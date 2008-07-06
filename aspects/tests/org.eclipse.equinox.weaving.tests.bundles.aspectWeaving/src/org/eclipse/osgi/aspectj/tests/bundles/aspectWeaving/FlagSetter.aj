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

package org.eclipse.osgi.aspectj.tests.bundles.aspectWeaving;

import org.eclipse.osgi.aspectj.tests.bundles.spareBundle.Advisable;

/**
 * This abstract aspect will specify the method to set
 * the flag in the testing class.
 * This aspect will need to be woven at compile time in 
 * order for the tests to pass.
 * 
 * This demonstrates the opt-in aspect model
 * 
 * Note that the load time weaving tests will attempt to re-weave this class
 * this results in an error which can be safly ignored
 */
public abstract aspect FlagSetter {
	
	public abstract pointcut scope();
	
	pointcut access ( Advisable fh ) :
		execution (public void access()) && scope() && this(fh);
	
	/**
	 * After the access pointcut (above) 
	 * set the flag in the target class
	 */
	after ( Advisable fh ) : access ( fh ) {
		System.out.println("FlagSetter aspect - setting flag");
		fh.setFlag();
	}

}
