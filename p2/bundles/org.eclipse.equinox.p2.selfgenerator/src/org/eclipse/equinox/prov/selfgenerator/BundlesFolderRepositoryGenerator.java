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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.eclipse.equinox.prov.artifact.repository.ArtifactDescriptor;
import org.eclipse.equinox.prov.metadata.IArtifactKey;
import org.eclipse.equinox.prov.metadata.InstallableUnit;
import org.eclipse.equinox.prov.metadata.InstallableUnitFragment;
import org.eclipse.equinox.prov.metadata.ProvidedCapability;
import org.eclipse.equinox.prov.metadata.RequiredCapability;
import org.eclipse.equinox.prov.metadata.TouchpointData;
import org.eclipse.equinox.prov.metadata.TouchpointType;
import org.eclipse.equinox.prov.metadata.generator.BundleDescriptionFactory;
import org.eclipse.equinox.prov.metadata.generator.MetadataGeneratorHelper;
import org.eclipse.equinox.prov.metadata.generator.PersistenceMetadata;
import org.eclipse.osgi.service.resolver.BundleDescription;
import org.eclipse.osgi.service.resolver.PlatformAdmin;
import org.eclipse.osgi.service.resolver.StateObjectFactory;
import org.eclipse.osgi.service.resolver.VersionRange;
import org.osgi.framework.Bundle;
import org.osgi.framework.Constants;
import org.osgi.framework.Version;
import org.osgi.service.startlevel.StartLevel;

public class BundlesFolderRepositoryGenerator {

	private BundleDescriptionFactory factory;
	private StartLevel startLevel;

	private static final String ECLIPSE_TOUCHPOINT = "eclipse";
	private static final Version ECLIPSE_TOUCHPOINT_VERSION = new Version(1, 0, 0);

	public BundlesFolderRepositoryGenerator(PlatformAdmin platformAdmin, StartLevel startLevel) {
		Object b = new BundleDescriptionFactory(null, null);
		StateObjectFactory stateFactory = platformAdmin.getFactory();
		factory = new BundleDescriptionFactory(stateFactory, null);
		
		this.startLevel = startLevel;
	}

	public List generate(File bundlesFolder, File metadataRepo, File artifactRepo, Bundle[] bundles) {
		PersistenceMetadata persistence = new PersistenceMetadata(metadataRepo, artifactRepo, false);
		Set artifactDescriptors = persistence.getArtifactDescriptors();
		Set ius = new HashSet();

		List currentIUs = new ArrayList();

		// add JRE metadata
		InstallableUnit jreIU = MetadataGeneratorHelper.createJREIU();
		ius.add(jreIU);
		currentIUs.add(jreIU);

		// add default IUFragemnt
		InstallableUnit defaultIUFragment = createEclipseDefaultConfigurationUnit("self", 4, false);
		ius.add(defaultIUFragment);
		currentIUs.add(defaultIUFragment);

		Map bundlesMap = createBundlesMap(bundles);

		List mappingRules = new ArrayList();

		File[] plugins = bundlesFolder.listFiles();
		for (int i = 0; i < plugins.length; i++) {
			BundleDescription bd = factory.getBundleDescription(plugins[i]);
			if (bd != null) {
				IArtifactKey key = MetadataGeneratorHelper.createEclipseArtifactKey(bd.getSymbolicName(), bd.getVersion().toString());
				ArtifactDescriptor ad = MetadataGeneratorHelper.createArtifactDescriptor(key, plugins[i], true, false);
				artifactDescriptors.add(ad);
				InstallableUnit iu = MetadataGeneratorHelper.createEclipseIU(bd, (Map) bd.getUserObject(), plugins[i].isDirectory(), key);
				ius.add(iu);

				if (plugins[i].isDirectory())
					mappingRules.add(new String[] {"(& (namespace=eclipse) (classifier=plugin) (id=" + key.getId() + "))", bundlesFolder.toURI() + "${id}_${version}"});

				String bundleKey = iu.getId() + "|" + iu.getVersion().toString();
				Bundle bundle = (Bundle) bundlesMap.remove(bundleKey);
				if (bundle != null) {
					currentIUs.add(iu);
					if (bd.getHost() == null) {
						InstallableUnitFragment iuFragment = createInstallableUnitFragment(iu, "self", startLevel.getBundleStartLevel(bundle), startLevel.isBundlePersistentlyStarted(bundle));
						ius.add(iuFragment);
						currentIUs.add(iuFragment);
					}
				}
			}
		}

		mappingRules.add(new String[] {"(& (namespace=eclipse) (classifier=plugin))", bundlesFolder.toURI() + "${id}_${version}.jar"});

		persistence.setMappingRules((String[][]) mappingRules.toArray(new String[][] {}));
		persistence.getMetadataRepository().addInstallableUnits((InstallableUnit[]) ius.toArray(new InstallableUnit[ius.size()]));
		persistence.saveArtifactRepository();

		return currentIUs;
	}

	private Map createBundlesMap(Bundle[] bundles) {
		Map bundlesMap = new HashMap();
		for (int i = 0; i < bundles.length; i++) {
			Bundle bundle = bundles[i];
			String symbolicName = bundle.getSymbolicName();
			String version = (String) bundle.getHeaders().get(Constants.BUNDLE_VERSION);
			bundlesMap.put(symbolicName + "|" + version, bundle);
		}
		return bundlesMap;
	}

	static InstallableUnitFragment createInstallableUnitFragment(InstallableUnit iu, String flavor, int startLevel, boolean markedAsStarted) {
		InstallableUnitFragment iuFragment = new InstallableUnitFragment();
		iuFragment.setId(flavor + iu.getId());
		iuFragment.setVersion(iu.getVersion());
		iuFragment.setHost(iu.getId(), new VersionRange(iu.getVersion(), true, iu.getVersion(), true));

		iuFragment.setCapabilities(new ProvidedCapability[] {new ProvidedCapability(InstallableUnit.FLAVOR_NAMESPACE, flavor, Version.emptyVersion)});

		iuFragment.setTouchpointType(new TouchpointType(ECLIPSE_TOUCHPOINT, ECLIPSE_TOUCHPOINT_VERSION));
		String configScript = "manipulator.getConfigData().addBundle(bundleToInstall);";

		// TODO - figure out a better way to figure this out
		boolean isFragment = Boolean.valueOf(iu.getProperty("fragment")).booleanValue();
		if (!isFragment) {
			configScript += "bundleToInstall.setStartLevel(" + startLevel + ");";
			configScript += "bundleToInstall.setMarkedAsStarted(" + Boolean.toString(markedAsStarted) + ");";
		}
		Map touchpointData = new HashMap();

		touchpointData.put("configurationData", configScript);
		touchpointData.put("unconfigurationData", "manipulator.getConfigData().removeBundle(bundleToRemove);");

		iuFragment.setImmutableTouchpointData(new TouchpointData(touchpointData));
		return iuFragment;
	}

	static InstallableUnit createEclipseDefaultConfigurationUnit(String flavor, int startLevel, boolean markedAsStarted) {
		InstallableUnitFragment iuFragment = new InstallableUnitFragment();
		iuFragment.setId(flavor + "default");
		iuFragment.setVersion(new Version(1, 0, 0));

		//Add a capability describing the flavor supported
		iuFragment.setCapabilities(new ProvidedCapability[] {new ProvidedCapability(InstallableUnit.FLAVOR_NAMESPACE, flavor, Version.emptyVersion)});

		//Create a capability on bundles
		RequiredCapability[] reqs = new RequiredCapability[] {new RequiredCapability(InstallableUnit.CAPABILITY_ECLIPSE_TYPES, InstallableUnit.CAPABILITY_ECLIPSE_BUNDLE, VersionRange.emptyRange, null, false, true)};
		iuFragment.setRequiredCapabilities(reqs);
		iuFragment.setTouchpointType(new TouchpointType(ECLIPSE_TOUCHPOINT, ECLIPSE_TOUCHPOINT_VERSION)); //TODO Is this necessary? I think we get that from the IU
		Map touchpointData = new HashMap();

		String configScript = "manipulator.getConfigData().addBundle(bundleToInstall);";
		configScript += "bundleToInstall.setStartLevel(" + startLevel + ");";
		configScript += "bundleToInstall.setMarkedAsStarted(" + Boolean.toString(markedAsStarted) + ");";

		touchpointData.put("configurationData", configScript);
		touchpointData.put("unconfigurationData", "manipulator.getConfigData().removeBundle(bundleToRemove);");

		iuFragment.setImmutableTouchpointData(new TouchpointData(touchpointData));
		return iuFragment;
	}
}
