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

package org.eclipse.osgi.aspectj.tests;

import java.io.IOException;
import java.util.Enumeration;

import junit.framework.TestCase;

import org.eclipse.osgi.aspectj.tests.aspectWeaving.EditableException;
import org.eclipse.osgi.aspectj.tests.aspectWeaving.FlagHolder;
import org.eclipse.osgi.aspectj.tests.aspectWeaving.FlagHolderForLT;
import org.eclipse.osgi.aspectj.tests.aspectWeaving.RemoteAspectTarget;
import org.eclipse.osgi.aspectj.tests.bundles.spareBundle.Advisable;

/**
 * Full suite of tests for loadtime weaving.
 * 
 * @author David Knibb
 */
public class WeavingTest extends TestCase {

	protected void setUp() throws Exception {
		super.setUp();
	}
	

	/*
	 * Test method for 'org.eclipse.osgi.aspectj.tests.aspectWeaving.FlagHolder.access()'
	 * Tests compile time weave has taken place
	 */
	public void testFirstWeave() {
		FlagHolder holder = new FlagHolder();
		holder.access();
		assertEquals(true, holder.isFlag());
	}
	
	/*
	 * Test method for 'org.eclipse.osgi.aspectj.tests.aspectWeaving.FlagHolder.remoteAccess()'
	 * Tests a load time weave of the same class
	 *  
	 */
	public void testReweaveOfWovenClass() {
		FlagHolder holder = new FlagHolder();
		holder.remoteAccess();
		assertEquals(true, holder.isFlag());
	}
	

	/*
	 * Test method for 'org.eclipse.osgi.aspectj.tests.aspectWeaving.FlagHolderForLT.remoteAccess()'
	 * Tests load time weave using a separate class as a flag holder
	 */
	public void testLoadTimeWeave() {
		FlagHolderForLT holder = new FlagHolderForLT();
		holder.access();
		assertEquals(true, holder.isFlag());
	}


	/*
	 * Used to test load time weaving with two aspects, 
	 * both to be woven at load time.
	 * uses the aspectWeavingLT2 project
	 */
	public void testLoadTimeWeave2() {
		FlagHolderForLT holder = new FlagHolderForLT();
		holder.remoteAccess();
		assertEquals(true, holder.isFlag());
	}
	
	/*
	 * Test that around advice is woven correctly. 
	 * Use with -Xnoinline to ensure a closure is generated. This is important for shared classes.
	 */
	public void testAroundAdvice(){
		FlagHolderForLT holder = new FlagHolderForLT();
		holder.setText("Hello World");
		assertTrue(holder.getText().endsWith("Around advice affect me!"));
	}
	
	/*
	 * Test the behaviour of methods with throw exceptions
	 */
	public void testAfterThrowing(){
		FlagHolderForLT holder = new FlagHolderForLT();
		try {
			holder.chuckExcexptionBack();
		} catch (EditableException e) {
//			e.hashCode();
//			System.out.println("Caught: "+e.getMessage());
			assertEquals("Exception has been affected", e.toString());
		}
	}
	
	/*
	 * Test the opt in model using configuration through the fragment manifest only.
	 * The fragment requires the aspect bundle, but contains no aspects
	 */
	public void testOptinWithRemoteAspect(){
		RemoteAspectTarget target = new RemoteAspectTarget();
		StringBuffer s = target.sayHello();
		assertEquals("hello", s.toString());
	}
	
	/*
	 * Test the ClassLoader.getResources(String) method of the AspectJClassloader
	 * Load a resource from a package which we import.
	 */
	public void testClassLoaderGetResources(){
		try {
			String resource = "resource/pkg/file.file";
			ClassLoader loader = getClass().getClassLoader();
			Enumeration e = loader.getResources(resource);
			if(!e.hasMoreElements()){
				fail("resource  '"+resource+"'  not found");
			}
		} catch (IOException e) {
			fail(e.toString());
//			e.printStackTrace();
		}
		
	}

	public void testWeaveFragment_pr166776 () {
		try {
			Class clazz = Class.forName("tests.bundles.fragment.AdvisableFragmentImpl");
			Advisable a = (Advisable)clazz.newInstance();
			a.remoteAccess();
			assertTrue(a.isFlag());
		}
		catch (Exception ex) {
			fail(ex.toString());
		}
	}
}
