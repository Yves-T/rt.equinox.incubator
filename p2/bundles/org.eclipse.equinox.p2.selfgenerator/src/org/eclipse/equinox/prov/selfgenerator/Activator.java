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
package org.eclipse.equinox.prov.selfgenerator;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.equinox.prov.artifact.repository.IArtifactRepositoryManager;
import org.eclipse.equinox.prov.core.helpers.MultiStatus;
import org.eclipse.equinox.prov.engine.IProfileRegistry;
import org.eclipse.equinox.prov.engine.Profile;
import org.eclipse.equinox.prov.metadata.InstallableUnit;
import org.eclipse.equinox.prov.metadata.repository.IMetadataRepository;
import org.eclipse.equinox.prov.metadata.repository.IMetadataRepositoryManager;
import org.eclipse.equinox.prov.metadata.repository.RepositoryCreationException;
import org.eclipse.osgi.service.datalocation.Location;
import org.eclipse.osgi.service.resolver.PlatformAdmin;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.Filter;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.service.startlevel.StartLevel;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

public class Activator implements BundleActivator, ServiceTrackerCustomizer {

	private static final String FILTER = "(|"
		+ "(" + Constants.OBJECTCLASS + "=" + IProfileRegistry.class.getName() + ")" 
		+ "(" + Constants.OBJECTCLASS + "=" + IMetadataRepositoryManager.class.getName() + ")"
		+ "(" + Constants.OBJECTCLASS + "=" + StartLevel.class.getName() + ")"
		+ "(" + Constants.OBJECTCLASS + "=" + IArtifactRepositoryManager.class.getName() + ")"
		+ "(" + Constants.OBJECTCLASS + "=" + PlatformAdmin.class.getName() + ")"
		+ ")";
	private BundleContext context;
	private IProfileRegistry profileregistry;
	private IMetadataRepositoryManager metadataRepositoryManager;
	private StartLevel startLevel;
	private IArtifactRepositoryManager artifactRepositoryManager;
	private PlatformAdmin platformAdmin;
	private ServiceTracker tracker;

	public void start(BundleContext context) throws Exception {
		
		this.context = context;
		Filter filter = context.createFilter(FILTER);
		tracker = new ServiceTracker(context, filter, this);
		tracker.open();
	}

	public void stop(BundleContext context) throws Exception {
		tracker.close();
		this.context = null;
	}
	
	public Object addingService(ServiceReference reference) {
		Object service = context.getService(reference);
		
		if (service instanceof IProfileRegistry)
			profileregistry = (IProfileRegistry) service;
		else if (service instanceof IMetadataRepositoryManager)
			metadataRepositoryManager = (IMetadataRepositoryManager) service;
		else if (service instanceof StartLevel)
			startLevel = (StartLevel) service;
		else if (service instanceof IArtifactRepositoryManager)
			artifactRepositoryManager = (IArtifactRepositoryManager) service;
		else if (service instanceof PlatformAdmin)
			platformAdmin = (PlatformAdmin) service;
 
		// elegant ++
		if (profileregistry != null && metadataRepositoryManager != null && startLevel != null && artifactRepositoryManager != null && platformAdmin != null)
			activate();

		return service;
	}

	public void modifiedService(ServiceReference reference, Object service) {
		// TODO Auto-generated method stub
	}

	public void removedService(ServiceReference reference, Object service) {
		if (service instanceof IProfileRegistry)
			profileregistry = null;
		else if (service instanceof IMetadataRepositoryManager)
			metadataRepositoryManager = null;
		else if (service instanceof StartLevel)
			startLevel = null;
		else if (service instanceof IArtifactRepositoryManager)
			artifactRepositoryManager = null;
		else if (service instanceof PlatformAdmin)
			platformAdmin = null;
		
		// elegant ++
		if (profileregistry == null || metadataRepositoryManager == null || startLevel == null || artifactRepositoryManager == null || platformAdmin == null)
			deactivate();	
	}
	
	private synchronized void activate() {
		File bundlesFolder = getPluginsFolder();
//		File repositoriesFolder = context.getDataFile("plugins-repo");
		File metadataRepo = new File(bundlesFolder, "content.xml");
		File artifactRepo = new File(bundlesFolder, "artifacts.xml");
		
		Bundle[] bundles = context.getBundles();

		BundlesFolderRepositoryGenerator repositoryGenerator = new BundlesFolderRepositoryGenerator(platformAdmin, startLevel);
		List currentIUs = repositoryGenerator.generate(bundlesFolder, metadataRepo, artifactRepo, bundles);
		registerMetadataRepository(metadataRepo);
		registerArtifactRepository(artifactRepo);
		
		Profile current = registerCurrentProfile();
		current.setValue("eclipse.prov.cache", artifactRepo.getAbsolutePath());
		current.setValue(Profile.PROP_FLAVOR, "self");
		
		ProfileFiller filler = new ProfileFiller();
		filler.setProfile(current);
		for (Iterator it = currentIUs.iterator(); it.hasNext();) {
			InstallableUnit iu = (InstallableUnit) it.next();
			filler.add(iu);
		}
		MultiStatus result = filler.generate(new NullProgressMonitor());
		
		System.out.println(result);
	}
	
	private synchronized void deactivate() {
		// nothing for the moment
	}

	private void registerArtifactRepository(File artifactRepo) {
		IArtifactRepositoryManager artifactManager = (IArtifactRepositoryManager) context.getService(context.getServiceReference(IArtifactRepositoryManager.class.getName()));
		try {
			artifactManager.getRepository(artifactRepo.toURL(), null);
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void registerMetadataRepository(File metadataRepo) {
		IMetadataRepositoryManager metadataManager = (IMetadataRepositoryManager) context.getService(context.getServiceReference(IMetadataRepositoryManager.class.getName()));
		try {
			 IMetadataRepository oldRepo = metadataManager.getRepository(metadataRepo.getParentFile().toURL());
			if (oldRepo != null)
				metadataManager.removeRepository(oldRepo);
			
			metadataManager.addRepository(metadataRepo.getParentFile().toURL(), new NullProgressMonitor());
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (RepositoryCreationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

//	private void synchronizeBundleState(Profile current, Bundle[] bundles) {
//		ProfileFiller filler = new ProfileFiller();
//		filler.setProfile(current);
//		for (int i = 0; i < bundles.length; i++) {
//			Bundle bundle = bundles[i];
//			String symbolicName = bundle.getSymbolicName();
//			String version = (String) bundle.getHeaders().get(Constants.BUNDLE_VERSION);
//			InstallableUnit iu = findInstallableUnit(symbolicName, version);
//			if (iu != null) {
//				filler.add(iu);
//				InstallableUnitFragment[] fragments = iu.getFragments();
//				iu.setFragments(null);
//				for (int j = 0; j < fragments.length; j++) {
//					filler.add(fragments[j]);
//				}
//			} else
//				System.out.println("Couldn't find matching IU for " + symbolicName + "_" + version);
//		}
//
//		MultiStatus result = filler.generate(new NullProgressMonitor());
//		
//		System.out.println(result);
//	}
//
//	private InstallableUnit findInstallableUnit(String symbolicName, String version) {
//		IMetadataRepositoriesManager metadataManager = (IMetadataRepositoriesManager) context.getService(context.getServiceReference(IMetadataRepositoriesManager.class.getName()));
//		IMetadataRepository[] repositories = metadataManager.getKnownRepositories();
//		for (int i=0; i < repositories.length; ++i) {
//			InstallableUnit[] ius = repositories[i].getInstallableUnits();
//			for (int j=0; j < ius.length; ++j) {
//				InstallableUnit iu = ius[j];
//				if (symbolicName.equals(iu.getId()) && version.equals(iu.getVersion().toString())) {
//					return iu;
//				}
//			}
//		}
//		return null;
//	}

	private Profile registerCurrentProfile() {
		String profileName = context.getProperty("eclipse.prov.profile");
		if (profileName == null)
			profileName = "eclipse.prov.profile.current";

		IProfileRegistry profileRegistry = (IProfileRegistry) context.getService(context.getServiceReference(IProfileRegistry.class.getName()));

		Profile current = profileRegistry.getProfile(profileName);
		if (current != null)
			profileRegistry.removeProfile(current);
		
		current = new Profile(profileName);
		try {
			String installProperty = context.getProperty("osgi.install.area");
			URL installURL = new URL(installProperty);
			File installFolder = new File(installURL.getPath());
			current.setValue(Profile.PROP_INSTALL_FOLDER, installFolder.getAbsolutePath());
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		try {
			String configurationProperty = context.getProperty("osgi.configuration.area");
			URL configurationURL = new URL(configurationProperty);
			File configurationFolder = new File(configurationURL.getPath());
			current.setValue("eclipse.configurationFolder", configurationFolder.getAbsolutePath());
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		profileRegistry.addProfile(current);
		return current;
	}
	
	private File getPluginsFolder() {
		URL installURL = null;
		try {
			installURL = new URL(context.getProperty("osgi.install.area"));
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		File pluginFolder = new File(installURL.getFile(), "plugins");
		return pluginFolder;
	}
	

	
	
	// TODO: switch to this instead of context data file
	public static URL getBaseStoreURL(BundleContext context) {
		Filter filter = null;
		try {
			filter = context.createFilter(Location.CONFIGURATION_FILTER);
		} catch (InvalidSyntaxException e) {
			// should not happen
		}
		ServiceTracker configLocationTracker = new ServiceTracker(context, filter, null);
		configLocationTracker.open();
		try {
			Location configLocation = (Location) configLocationTracker.getService();
			if (configLocation == null)
				return null;

			URL baseURL = configLocation.getURL();
			if (baseURL == null)
				return null;

			try {
				URL configURL = new URL(baseURL, context.getBundle().getSymbolicName());
				File configFile = new File(configURL.getFile());
				if (configFile.exists())
					return configURL;
			} catch (MalformedURLException e) {
				e.printStackTrace();
			}
			return null;
		} finally {
			configLocationTracker.close();
		}
	}


}