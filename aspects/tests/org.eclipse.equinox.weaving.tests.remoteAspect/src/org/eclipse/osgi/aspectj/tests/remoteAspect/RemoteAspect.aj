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

package org.eclipse.osgi.aspectj.tests.remoteAspect;

/**
 * This project contains a concrete aspect. It is required by
 * org.eclipse.osgi.aspectj.tests.remoteAspectFragment, which
 * is a fragment of the tests bundle. The fragment contains no 
 * implementation, only a manifest is present. This demonstrates 
 * the opt-in model for ltw.
 */
public aspect RemoteAspect {
	static{
		System.out.println("RemoteAspect.<cinit>");
	}
	
	public pointcut sayHello() :
		execution (public StringBuffer sayHello()) &&
		within(org.eclipse.osgi.aspectj.tests.aspectWeaving.RemoteAspectTarget);
	
	
	after() returning (StringBuffer sb) : sayHello(){
		sb.append("hello");
	}

}
