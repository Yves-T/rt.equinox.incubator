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


public class EditableException extends Exception{
	public String text =null ; 

	public EditableException(String message) {
		super(message);
	}
	
	
	/*
	 *  This method can be called by an aspect
	 *  it will set the field, which can later be returned by the overridden toString();
	 */
	public int hashCode(){ 
		text = "Exception has been affected";
		return 0;
	}
	

	public String toString(){
		return text;
	}
}