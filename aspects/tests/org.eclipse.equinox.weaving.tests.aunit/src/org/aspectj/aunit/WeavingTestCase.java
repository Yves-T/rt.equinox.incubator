/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Matthew Webster           initial implementation      
 *******************************************************************************/

package org.aspectj.aunit;

import junit.framework.TestCase;

public abstract class WeavingTestCase extends TestCase {

	protected void setUp() throws Exception {
		super.setUp();
		TestMessageHandler.clear();
	}

	protected void assertWoven(Class type, String aspectName) {
		String typeName = type.getName();
		assertTrue("Expected: Type '" + typeName + "' woven by '" + aspectName,TestMessageHandler.containsMessage(typeName,aspectName));
	}

	protected void assertWoven(String className, String aspectName) {
		assertTrue("Expected: Type '" + className + "' woven by '" + aspectName,TestMessageHandler.containsMessage(className,aspectName));
	}

	protected void assertNotWoven(Class type, String aspectName) {
		String typeName = type.getName();
		String message = TestMessageHandler.getMessage(typeName,aspectName);
		assertTrue("Unexpected: " + message,message == null);
	}

	protected void assertNotWoven(String className, String aspectName) {
		String message = TestMessageHandler.getMessage(className,aspectName);
		assertTrue("Unexpected: " + message,message == null);
	}

	protected void assertPrecedes(Class azpect1, Class azpect2) {
		String aspectName1 = azpect1.getName(); 
		String aspectName2 = azpect2.getName(); 
		assertTrue("Expected: '" + aspectName1 + "' precedes '" + aspectName2,TestMessageHandler.precedes(aspectName1,aspectName2));
	}

}
