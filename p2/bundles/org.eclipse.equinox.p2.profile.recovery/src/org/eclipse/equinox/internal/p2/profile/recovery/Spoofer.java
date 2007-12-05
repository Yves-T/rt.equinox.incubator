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
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Set;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.equinox.frameworkadmin.*;
import org.eclipse.equinox.p2.artifact.repository.IArtifactRepository;
import org.eclipse.equinox.p2.artifact.repository.IArtifactRepositoryManager;
import org.eclipse.equinox.p2.director.IDirector;
import org.eclipse.equinox.p2.engine.Profile;
import org.eclipse.equinox.p2.engine.SelfProfileProducer;
import org.eclipse.equinox.p2.metadata.generator.IGeneratorInfo;
import org.eclipse.equinox.p2.metadata.repository.IMetadataRepository;
import org.eclipse.equinox.p2.metadata.repository.IMetadataRepositoryManager;
import org.osgi.framework.Bundle;

public class Spoofer implements SelfProfileProducer, IGeneratorInfo {
	private Manipulator runningInstance;
	private String artifactLocation;
	private String metadataLocation;
	private IArtifactRepository artifactRepo;
	private IMetadataRepository metadataRepo;

	public Profile produce(Profile p) {
		FrameworkAdmin fwkAdmin = (FrameworkAdmin) ServiceHelper.getService(Activator.ctx, FrameworkAdmin.class.getName());
		runningInstance = fwkAdmin.getRunningManipulator();
		initializeArtifactRepository();
		initializeMetadataRepository();
		new InPlaceGenerator(this).generate();
		if (p == null) {
			p = ProfileFactory.makeProfile(); //TODO Need to handle the case where the prop is not set
			p.setValue("eclipse.p2.cache", artifactLocation.substring(5));
			//Mark it roaming if possible
		}
		return p;
	}

	public void fill(Profile p) {
		IDirector dir = (IDirector) ServiceHelper.getService(Activator.ctx, IDirector.class.getName());
		dir.install(metadataRepo.getInstallableUnits(null), p, null);
	}

	public boolean addDefaultIUs() {
		return false;
	}

	public boolean append() {
		return true;
	}

	public File[] getBundleLocations() {
		Bundle[] installed = Activator.ctx.getBundles();
		File[] locations = new File[installed.length];
		for (int i = 0; i < installed.length; i++) {
			try {
				locations[i] = FileLocator.getBundleFile(installed[i]);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return locations;
	}

	public File getConfigurationLocation() {
		return null;
	}

	public ArrayList getDefaultIUs(Set ius) {
		return null;
	}

	public File getExecutableLocation() {
		return null;
	}

	public File getFeaturesLocation() {
		return null;
	}

	public String getFlavor() {
		return "tooling";
	}

	public File getJRELocation() {
		return null;
	}

	public String[][] getMappingRules() {
		return null;
	}

	public String getRootId() {
		return null;
	}

	public String getRootVersion() {
		return null;
	}

	public URL getSiteLocation() {
		return null;
	}

	public boolean publishArtifactRepository() {
		return true;
	}

	public boolean publishArtifacts() {
		return false;
	}

	public void setFlavor(String value) {
	}

	public void setPublishArtifacts(boolean value) {
	}

	public void setRootId(String value) {
	}

	public void setArtifactRepository(IArtifactRepository value) {
	}

	public void setMetadataRepository(IMetadataRepository value) {
	}

	public IArtifactRepository getArtifactRepository() {
		return artifactRepo;
	}

	public IMetadataRepository getMetadataRepository() {
		return metadataRepo;
	}

	public ConfigData getConfigData() {
		return runningInstance.getConfigData();
	}

	public LauncherData getLauncherData() {
		return runningInstance.getLauncherData();
	}

	public String getLauncherConfig() {
		return null;
	}

	private void initializeArtifactRepository() {
		File artifactXML = new File(ProfileFactory.getDefaultLocation() + "/artifacts.xml");
		artifactXML.getParentFile().mkdirs();
		try {
			artifactLocation = artifactXML.toURL().toExternalForm();
		} catch (MalformedURLException e1) {
			//Ignore
		}

		IArtifactRepositoryManager manager = (IArtifactRepositoryManager) ServiceHelper.getService(Activator.ctx, IArtifactRepositoryManager.class.getName());
		URL location;
		try {
			location = new URL(artifactLocation);
		} catch (MalformedURLException e) {
			throw new IllegalArgumentException("Artifact repository location not a valid URL:" + artifactLocation); //$NON-NLS-1$
		}
		artifactRepo = manager.loadRepository(location, null);
		if (artifactRepo != null) {
			if (!artifactRepo.isModifiable())
				throw new IllegalArgumentException("Artifact repository not writeable: " + location); //$NON-NLS-1$
			if (!append())
				artifactRepo.removeAll();
			return;
		}

		// 	the given repo location is not an existing repo so we have to create something
		// TODO for now create a Simple repo by default.
		String repositoryName = artifactLocation + " - artifacts"; //$NON-NLS-1$
		artifactRepo = manager.createRepository(location, repositoryName, "org.eclipse.equinox.p2.artifact.repository.simpleRepository"); //$NON-NLS-1$
	}

	private void initializeMetadataRepository() {
		File contentXML = new File(ProfileFactory.getDefaultLocation() + "/p2/content.xml");
		contentXML.getParentFile().mkdirs();
		try {
			metadataLocation = contentXML.toURL().toExternalForm();
		} catch (MalformedURLException e1) {
			//Ignore
		}

		URL location;
		try {
			location = new URL(metadataLocation);
		} catch (MalformedURLException e) {
			throw new IllegalArgumentException("Metadata repository location not a valid URL:" + artifactLocation); //$NON-NLS-1$
		}
		IMetadataRepositoryManager manager = (IMetadataRepositoryManager) ServiceHelper.getService(Activator.ctx, IMetadataRepositoryManager.class.getName());
		metadataRepo = manager.loadRepository(location, null);
		if (metadataRepo != null) {
			if (!metadataRepo.isModifiable())
				throw new IllegalArgumentException("Metadata repository not writeable: " + location); //$NON-NLS-1$
			if (!append())
				metadataRepo.removeAll();
			return;
		}

		// 	the given repo location is not an existing repo so we have to create something
		// TODO for now create a random repo by default.
		String repositoryName = metadataLocation + " - metadata"; //$NON-NLS-1$
		metadataRepo = manager.createRepository(location, repositoryName, "org.eclipse.equinox.p2.metadata.repository.simpleRepository"); //$NON-NLS-1$
	}
}
