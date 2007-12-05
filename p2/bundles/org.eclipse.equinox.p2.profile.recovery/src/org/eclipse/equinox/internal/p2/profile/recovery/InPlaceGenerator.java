/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.equinox.internal.p2.profile.recovery;

import java.io.File;
import java.io.IOException;
import org.eclipse.equinox.p2.artifact.repository.*;
import org.eclipse.equinox.p2.metadata.generator.Generator;
import org.eclipse.equinox.p2.metadata.generator.IGeneratorInfo;

public class InPlaceGenerator extends Generator {

	public InPlaceGenerator(IGeneratorInfo infoProvider) {
		super(infoProvider);
	}

	protected void publishArtifact(IArtifactDescriptor descriptor, File[] files, IArtifactRepository destination, boolean asIs) {
		if (descriptor == null)
			return;
		if (!getGeneratorInfo().publishArtifacts()) {
			ArtifactDescriptor pathDescriptor = new ArtifactDescriptor(descriptor);
			try {
				if (files.length == 1)
					pathDescriptor.setProperty("artifact.reference", files[0].getAbsoluteFile().toURL().toExternalForm()); //$NON-NLS-1$
				else {
					pathDescriptor.setProperty("artifact.reference", files[0].getParentFile().getAbsoluteFile().toURL().toExternalForm()); //$NON-NLS-1$
					pathDescriptor.setProperty("artifact.folder", "true"); //$NON-NLS-1$//$NON-NLS-2$
				}
			} catch (IOException e) {
				//toURL will always succeed
			}
			destination.addDescriptor(pathDescriptor);
		}
	}
}
