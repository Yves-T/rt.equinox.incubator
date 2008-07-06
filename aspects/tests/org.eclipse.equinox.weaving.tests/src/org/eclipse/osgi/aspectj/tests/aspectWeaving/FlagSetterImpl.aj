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

import org.eclipse.osgi.aspectj.tests.bundles.aspectWeaving.FlagSetter;

/**
 * Provides a concrete implementation of the (abstract) FlagSetter aspect
 * 
 *  This is compile time woven, demonstrating the opt-in aspect model
 */
public aspect FlagSetterImpl extends FlagSetter {
	
	public pointcut scope() :
		within (org.eclipse.osgi.aspectj.tests.aspectWeaving.FlagHolder);

}
