/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.equinox.internal.p2.repositoryoptimizer;

import java.io.IOException;
import java.io.OutputStream;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.equinox.p2.artifact.repository.*;
import org.eclipse.equinox.p2.artifact.repository.processing.*;
import org.eclipse.equinox.p2.metadata.IArtifactKey;

public class Optimizer {
	private static final String PACKED_FORMAT = "packed"; //$NON-NLS-1$
	private IArtifactRepository repository;

	public Optimizer(IArtifactRepository repository) {
		this.repository = repository;
	}

	public void run() {
		IArtifactKey[] keys = repository.getArtifactKeys();
		for (int i = 0; i < keys.length; i++) {
			IArtifactKey key = keys[i];
			if (!key.getClassifier().equals("plugin"))
				continue;
			IArtifactDescriptor[] descriptors = repository.getArtifactDescriptors(key);
			IArtifactDescriptor complete = null;
			boolean optimized = false;
			for (int j = 0; j < descriptors.length; j++) {
				IArtifactDescriptor descriptor = descriptors[j];
				if (descriptor.getProcessingSteps().length == 0)
					complete = descriptor;
				optimized |= isOptimized(descriptor);
			}
			if (!optimized)
				optimize(complete);
		}
	}

	private void optimize(IArtifactDescriptor descriptor) {
		ArtifactDescriptor newDescriptor = new ArtifactDescriptor(descriptor);
		ProcessingStepDescriptor[] steps = new ProcessingStepDescriptor[] {new ProcessingStepDescriptor("org.eclipse.equinox.p2.processing.Pack200Unpacker", null, true)};
		newDescriptor.setProcessingSteps(steps);
		newDescriptor.setProperty(IArtifactDescriptor.FORMAT, PACKED_FORMAT);
		OutputStream repositoryStream = null;
		try {
			repositoryStream = repository.getOutputStream(newDescriptor);

			// Add in all the processing steps needed to optimize (e.g., pack200, ...)
			ProcessingStepHandler handler = new ProcessingStepHandler();
			OutputStream destination = handler.link(new ProcessingStep[] {new Pack200Step()}, repositoryStream, null);

			// Do the actual work by asking the repo to get the artifact and put it in the destination.
			repository.getArtifact(descriptor, destination, new NullProgressMonitor());
		} finally {
			if (repositoryStream != null)
				try {
					repositoryStream.close();
					// TODO need to figure out how to get our processing steps linked into the repositoryStream
					// so that the close() picks up any status issues.
					IStatus status = ProcessingStepHandler.validateSteps(repositoryStream);
					if (!status.isOK()) {
						System.out.println("Skipping optimization of: " + descriptor.getArtifactKey());
						System.out.println(status.toString());
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}
	}

	private boolean isOptimized(IArtifactDescriptor descriptor) {
		return PACKED_FORMAT.equals(descriptor.getProperty(IArtifactDescriptor.FORMAT));
	}

}
