/*******************************************************************************
 * Copyright (c) 2009, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Sonatype, Inc. - ongoing development
 *******************************************************************************/
package org.eclipse.equinox.internal.p2.afterthefact;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.equinox.p2.metadata.IArtifactKey;
import org.eclipse.equinox.p2.metadata.IInstallableUnit;
import org.eclipse.equinox.p2.metadata.IProvidedCapability;
import org.eclipse.equinox.p2.metadata.IRequirement;
import org.eclipse.equinox.p2.metadata.ITouchpointType;
import org.eclipse.equinox.p2.metadata.MetadataFactory;
import org.eclipse.equinox.p2.metadata.Version;
import org.eclipse.equinox.p2.metadata.VersionRange;
import org.eclipse.equinox.p2.metadata.MetadataFactory.InstallableUnitDescription;
import org.eclipse.equinox.p2.metadata.MetadataFactory.InstallableUnitFragmentDescription;
import org.eclipse.osgi.service.resolver.BundleDescription;
import org.eclipse.osgi.service.resolver.BundleSpecification;
import org.eclipse.osgi.service.resolver.ExportPackageDescription;
import org.eclipse.osgi.service.resolver.ImportPackageSpecification;
import org.eclipse.osgi.service.resolver.PlatformAdmin;
import org.osgi.framework.Constants;

public class Reify {
	public static final ITouchpointType TOUCHPOINT_OSGI = MetadataFactory.createTouchpointType("org.eclipse.equinox.p2.osgi", Version.createOSGi(1, 0, 0)); //$NON-NLS-1$
	public static final String OSGI_BUNDLE_CLASSIFIER = "osgi.bundle"; //$NON-NLS-1$
	private static final String CAPABILITY_NS_OSGI_BUNDLE = "osgi.bundle"; //$NON-NLS-1$
	private static final String CAPABILITY_NS_OSGI_FRAGMENT = "osgi.fragment"; //$NON-NLS-1$
	public static final String TYPE_ECLIPSE_BUNDLE = "bundle"; //$NON-NLS-1$
	public static final String NAMESPACE_ECLIPSE_TYPE = "org.eclipse.equinox.p2.eclipse.type"; //$NON-NLS-1$
	public static final IProvidedCapability BUNDLE_CAPABILITY = MetadataFactory.createProvidedCapability(NAMESPACE_ECLIPSE_TYPE, TYPE_ECLIPSE_BUNDLE, Version.createOSGi(1, 0, 0));
	public static final String CAPABILITY_NS_JAVA_PACKAGE = "java.package"; //$NON-NLS-1$
	
	public static IInstallableUnit createBundleIU(BundleDescription bd, IArtifactKey key) {
		InstallableUnitDescription iu = new MetadataFactory.InstallableUnitDescription();
		iu.setSingleton(bd.isSingleton());
		iu.setId(bd.getSymbolicName());
		iu.setVersion(fromOSGiVersion(bd.getVersion()));
		iu.setFilter(bd.getPlatformFilter());
//		iu.setUpdateDescriptor(MetadataFactory.createUpdateDescriptor(bd.getSymbolicName(), computeUpdateRange(bd.getVersion()), IUpdateDescriptor.NORMAL, null));
		if (key != null)
			iu.setArtifacts(new IArtifactKey[] {key});
		iu.setTouchpointType( TOUCHPOINT_OSGI);

		boolean isFragment = bd.getHost() != null;

		//Process the required bundles
		BundleSpecification requiredBundles[] = bd.getRequiredBundles();
		ArrayList<IRequirement> reqsDeps = new ArrayList<IRequirement>();
		if (isFragment)
			reqsDeps.add(MetadataFactory.createRequirement(CAPABILITY_NS_OSGI_BUNDLE, bd.getHost().getName(), fromOSGiVersionRange(bd.getHost().getVersionRange()), null, false, false));
		for (int j = 0; j < requiredBundles.length; j++)
			reqsDeps.add(MetadataFactory.createRequirement(CAPABILITY_NS_OSGI_BUNDLE, requiredBundles[j].getName(), fromOSGiVersionRange(requiredBundles[j].getVersionRange()), null, requiredBundles[j].isOptional(), false));

		// Process the import packages
		ImportPackageSpecification osgiImports[] = bd.getImportPackages();
		for (int i = 0; i < osgiImports.length; i++) {
			// TODO we need to sort out how we want to handle wild-carded dynamic imports - for now we ignore them
			ImportPackageSpecification importSpec = osgiImports[i];
			String importPackageName = importSpec.getName();
			if (importPackageName.indexOf('*') != -1)
				continue;
			VersionRange versionRange = fromOSGiVersionRange(importSpec.getVersionRange());
			//TODO this needs to be refined to take into account all the attribute handled by imports
			reqsDeps.add(MetadataFactory.createRequirement(CAPABILITY_NS_JAVA_PACKAGE, importPackageName, versionRange, null, isOptional(importSpec), false));
		}
		iu.setRequiredCapabilities(reqsDeps.toArray(new IRequirement[reqsDeps.size()]));

		// Create set of provided capabilities
		ArrayList<IProvidedCapability> providedCapabilities = new ArrayList<IProvidedCapability>();
		providedCapabilities.add(createSelfCapability(bd.getSymbolicName(), fromOSGiVersion(bd.getVersion())));
		providedCapabilities.add(MetadataFactory.createProvidedCapability(CAPABILITY_NS_OSGI_BUNDLE, bd.getSymbolicName(), fromOSGiVersion(bd.getVersion())));

		// Process the export package
		ExportPackageDescription exports[] = bd.getExportPackages();
		for (int i = 0; i < exports.length; i++) {
			//TODO make sure that we support all the refinement on the exports
			providedCapabilities.add(MetadataFactory.createProvidedCapability(CAPABILITY_NS_JAVA_PACKAGE, exports[i].getName(), fromOSGiVersion(exports[i].getVersion())));
		}
		// Here we add a bundle capability to identify bundles
		providedCapabilities.add(BUNDLE_CAPABILITY);
		if (isFragment)
			providedCapabilities.add(MetadataFactory.createProvidedCapability(CAPABILITY_NS_OSGI_FRAGMENT, bd.getHost().getName(), fromOSGiVersion(bd.getVersion())));

		iu.setCapabilities(providedCapabilities.toArray(new IProvidedCapability[providedCapabilities.size()]));
		return MetadataFactory.createInstallableUnit(iu);
	}
	public static IProvidedCapability createSelfCapability(String installableUnitId, Version installableUnitVersion) {
		return MetadataFactory.createProvidedCapability(IInstallableUnit.NAMESPACE_IU_ID, installableUnitId, installableUnitVersion);
	}
	
	private static boolean isOptional(ImportPackageSpecification importedPackage) {
		if (importedPackage.getDirective(Constants.RESOLUTION_DIRECTIVE).equals(ImportPackageSpecification.RESOLUTION_DYNAMIC) || importedPackage.getDirective(Constants.RESOLUTION_DIRECTIVE).equals(ImportPackageSpecification.RESOLUTION_OPTIONAL))
			return true;
		return false;
	}

	static IInstallableUnit createDefaultBundleConfigurationUnit() {
		InstallableUnitFragmentDescription cu = new InstallableUnitFragmentDescription();
		String configUnitId = "org.eclipse.equinox.p2.inprocess.default.osgi.bundle.cu";
		cu.setId(configUnitId);
		Version configUnitVersion = Version.createOSGi(1, 0, 0);
		cu.setVersion(configUnitVersion);
	
		// Add capabilities for fragment, self, and describing the flavor supported
		cu.setProperty(InstallableUnitDescription.PROP_TYPE_FRAGMENT, Boolean.TRUE.toString());
		cu.setCapabilities(new IProvidedCapability[] {MetadataFactory.createProvidedCapability(IInstallableUnit.NAMESPACE_IU_ID, configUnitId, configUnitVersion)});
	
		// Create a required capability on bundles
		IRequirement[] reqs = new IRequirement[] {MetadataFactory.createRequirement("org.eclipse.equinox.p2.eclipse.type", "bundle", VersionRange.emptyRange, null, false, true, false)};
		cu.setHost(reqs);
		Map<String, String> touchpointData = new HashMap<String, String>();
	
		touchpointData.put("install", "installBundle(bundle:${artifact})"); //$NON-NLS-1$ //$NON-NLS-2$
		touchpointData.put("uninstall", "uninstallBundle(bundle:${artifact})"); //$NON-NLS-1$ //$NON-NLS-2$
	
		cu.addTouchpointData(MetadataFactory.createTouchpointData(touchpointData));
		return MetadataFactory.createInstallableUnit(cu);
	}
	
	public Collection<IInstallableUnit> reify(PlatformAdmin platformAdmin) {
		BundleDescription[] bundles = platformAdmin.getState().getBundles();
		Collection<IInstallableUnit> ius = new ArrayList<IInstallableUnit>(bundles.length);
		for (int i = 0; i < bundles.length; i++) {
			ius.add(createBundleIU(bundles[i], null));
		}
		return ius;
	}
	
	private static Version fromOSGiVersion(org.osgi.framework.Version version) {
		if (version == null)
			return null;
		if (version.getMajor() == Integer.MAX_VALUE && version.getMicro() == Integer.MAX_VALUE && version.getMicro() == Integer.MAX_VALUE)
			return Version.MAX_VERSION;
		return Version.createOSGi(version.getMajor(), version.getMinor(), version.getMicro(), version.getQualifier());
	}
	
	private static VersionRange fromOSGiVersionRange(org.eclipse.osgi.service.resolver.VersionRange range) {
		if (range.equals(org.eclipse.osgi.service.resolver.VersionRange.emptyRange))
			return VersionRange.emptyRange;
		return new VersionRange(fromOSGiVersion(range.getMinimum()), range.getIncludeMinimum(), fromOSGiVersion(range.getMaximum()), range.getIncludeMaximum());
	}
}
