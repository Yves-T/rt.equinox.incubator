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

package org.eclipse.osgi.aspectj.tests.aspectWeaving;

import org.eclipse.osgi.aspectj.tests.bundles.spareBundle.Advisable;

/**
 * This class holds a flag, which can be set by an aspect.
 * This aspect could be woven at compile time or load time.
 */
public class FlagHolder implements Advisable {
	
	private boolean flag;
	private String text;
	
	public FlagHolder(){
		flag = false;
	}

	/**
	 * @return Returns the flag.
	 */
	public boolean isFlag() {
		return flag;
	}

	/**
	 * @param flag Specify whether the flag should be set to true or false.
	 */
	public void setFlag() {
		flag = true;
	}
	
	/**
	 * This would be advised by an aspect to be compile at load time
	 */
	public void access() {
		System.out.println("FlagHolder - access called");
	}
	
	/**
	 * This would be advised by an aspect to be woven at load time
	 */
	public void remoteAccess() {
		System.out.println("FlagHolder - remoteAccess called");
	}

	public void setText(String s) {
		text=s;		
	}
	

}
