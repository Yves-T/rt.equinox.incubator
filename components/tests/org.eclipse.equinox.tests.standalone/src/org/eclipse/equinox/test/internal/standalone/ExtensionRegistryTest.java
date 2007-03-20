/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.equinox.test.internal.standalone;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import junit.framework.*;
import org.eclipse.core.runtime.*;
import org.eclipse.core.runtime.spi.RegistryStrategy;

/**
 * Tests if the extenesion registry can be run without OSGi. 
 */
public class ExtensionRegistryTest extends TestCase {

	final private static String xmlContribution = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + //$NON-NLS-1$ 
			"<plugin id=\"RegistryTest\" version=\"1.0.0\">" + //$NON-NLS-1$
			"<extension-point id=\"point1\" name=\"Test extension point\" schema=\"schema/schema.exsd\"/>" + //$NON-NLS-1$ 
			"<extension point=\"point1\" id=\"extension1\">" + //$NON-NLS-1$
			"</extension></plugin>"; //$NON-NLS-1$

	final private static String contributorID = "RegistryTest"; //$NON-NLS-1$
	final private static String extPointID = contributorID + ".point1"; //$NON-NLS-1$
	final private static String extensionID = contributorID + ".extension1"; //$NON-NLS-1$
	final private static String extPointLable = "Test extension point"; //$NON-NLS-1$

	private IContributor contributor = null;
	private IExtensionRegistry registry = null;

	protected void setUp() throws Exception {
		super.setUp();
		contributor = ContributorFactorySimple.createContributor(contributorID);
		// to store contributions in the registry cache pass file location to the RegistryStrategy
		registry = RegistryFactory.createRegistry(new RegistryStrategy(null, null), null, null);
	}

	public void testStandAlone() {
		// fill registry with an extension point and an extension
		InputStream is = new ByteArrayInputStream(xmlContribution.getBytes());
		assertTrue(registry.addContribution(is, contributor, false, null, null, null));

		// check the extension point
		IExtensionPoint extensionPoint = registry.getExtensionPoint(extPointID);
		assertNotNull(extensionPoint);
		String extPointLabel = extensionPoint.getLabel();
		assertTrue(extPointLable.equals(extPointLabel));

		// check the extension
		IExtension[] extensions = extensionPoint.getExtensions();
		assertNotNull(extensions);
		assertTrue(extensions.length == 1);
		String extensionId = extensions[0].getUniqueIdentifier();
		assertTrue(extensionID.equals(extensionId));
	}

	public static Test suite() {
		return new TestSuite(ExtensionRegistryTest.class);
	}

}
