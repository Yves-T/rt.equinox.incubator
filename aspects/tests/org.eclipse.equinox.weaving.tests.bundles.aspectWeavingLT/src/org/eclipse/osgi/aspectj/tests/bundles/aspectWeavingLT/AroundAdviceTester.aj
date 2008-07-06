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

//import org.eclipse.osgi.aspectj.tests.bundles.spareBundle.Advisable;

public aspect AroundAdviceTester {
	
	public pointcut scope() :
		within (org.eclipse.osgi.aspectj.tests.aspectWeaving.FlagHolderForLT);
	
	pointcut aroundRemote(Object fh, String s) :
		execution (public void setText(..)) && scope() && this(fh) && args( s );
	
	void around(Object a, String s) : aroundRemote(a, s){
		System.out.println("Called around advice");
		
		proceed (a, s+"Around advice affect me!");
	}

}
