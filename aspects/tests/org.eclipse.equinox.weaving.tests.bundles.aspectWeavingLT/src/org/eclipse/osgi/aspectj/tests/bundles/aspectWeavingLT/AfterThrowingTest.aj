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


public aspect AfterThrowingTest {
	
	public pointcut scope() :
		within (org.eclipse.osgi.aspectj.tests.aspectWeaving.FlagHolderForLT);
	
	pointcut afterThrow() :
		execution (public void chuckExcexptionBack(..)) && scope();
	
	after() throwing(Exception ex) :afterThrow(){
		System.out.println("Called afterThrowing advice");
		ex.hashCode(); //will set the text field in EditableException
	}

}
