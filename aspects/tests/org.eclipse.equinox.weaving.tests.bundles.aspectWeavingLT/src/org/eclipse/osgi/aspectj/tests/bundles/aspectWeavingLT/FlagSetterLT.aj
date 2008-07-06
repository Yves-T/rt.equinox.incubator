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

package org.eclipse.osgi.aspectj.tests.bundles.aspectWeavingLT;

import org.eclipse.osgi.aspectj.tests.bundles.spareBundle.Advisable;

/**
 * This abstract aspect will specify the method to set
 * the flag in the testing class.
 * This aspect will need to be woven at load time in 
 * order for the tests to pass.
 */
public aspect FlagSetterLT {

	public pointcut scope() :
		within (org.eclipse.osgi.aspectj.tests.aspectWeaving.FlagHolder) ||
		within (org.eclipse.osgi.aspectj.tests.aspectWeaving.FlagHolderForLT)
		|| within (tests.bundles.fragment.AdvisableFragmentImpl)
		;
	
	pointcut remoteAccess ( Advisable fh ) :
		execution (public void remoteAccess()) && scope() && this(fh);
	
	/**
	 * After the remoteAccess pointcut (above) 
	 * set the flag in the target class 
	 */
	after ( Advisable fh ) : remoteAccess ( fh ) {
		System.out.println("FlagSetterLT aspect - setting flag on "+fh);
		fh.setFlag();
	}

}
