/*******************************************************************************
 * Copyright (c) 2006 Cognos Incorporated
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Cognos Incorporated - initial API and implementation
 *******************************************************************************/
package org.eclipse.equinox.deploymentadmin;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.jar.Manifest;

import org.osgi.framework.Bundle;
import org.osgi.framework.Constants;
import org.osgi.framework.Version;
import org.osgi.service.deploymentadmin.BundleInfo;
import org.osgi.service.deploymentadmin.DeploymentAdmin;
import org.osgi.service.deploymentadmin.DeploymentException;
import org.osgi.service.deploymentadmin.DeploymentPackage;
import org.osgi.service.deploymentadmin.spi.DeploymentSession;

public class DeploymentAdminImpl implements DeploymentAdmin {

	private Map deploymentPackagesMap = new HashMap();

	public boolean cancel() {
		return false;
	}

	public DeploymentPackage getDeploymentPackage(String symbName) {
		return getDeploymentPackageImpl(symbName);
	}

	private DeploymentPackageImpl getDeploymentPackageImpl(String symbName) {
		return (DeploymentPackageImpl) deploymentPackagesMap.get(symbName);
	}
	
	public DeploymentPackage getDeploymentPackage(Bundle bundle) {
		if (! bundle.getLocation().startsWith("osgi-dp:"))
			return null;
		
		return getDeploymentPackage(bundle.getSymbolicName(), new Version((String) bundle.getHeaders().get(Constants.BUNDLE_VERSION)));
	}
	
	protected DeploymentPackage getDeploymentPackage(String bsn, Version version) {
		for( Iterator it = deploymentPackagesMap.values().iterator(); it.hasNext();) {
			DeploymentPackage deploymentPackage = (DeploymentPackage) it.next();
			BundleInfo[] bundleInfos = deploymentPackage.getBundleInfos();
			for (int i=0;i<bundleInfos.length;i++)
				if (bundleInfos[i].getSymbolicName().equals(bsn) && bundleInfos[i].getVersion().equals(version))
					return deploymentPackage;
		}
		return null;
	}	

	public DeploymentPackage installDeploymentPackage(InputStream in) throws DeploymentException {
		DeploymentSession session = new DeploymentSessionImpl();
		try {
			JarInputStream jis = new JarInputStream(in);
			Manifest manifest = jis.getManifest();
			if (manifest == null)
				throw new DeploymentException(DeploymentException.CODE_ORDER_ERROR);

			validateManifest(manifest);
			
			DeploymentPackageImpl source = new DeploymentPackageImpl(manifest);
			DeploymentPackageImpl target = getDeploymentPackageImpl(source.getName());
			if (target != null)
				target.stopBundles();
			
			JarEntry currentEntry = jis.getNextJarEntry();
			
			while (currentEntry != null && processSignatureFile(currentEntry))
					currentEntry = jis.getNextJarEntry();
			
			String bundleLocalization = source.getHeader("Bundle-Localization");
			while (currentEntry != null && processLocalizationFile(bundleLocalization, currentEntry))
				currentEntry = jis.getNextJarEntry();
			
			while (currentEntry != null && processBundle(currentEntry))
				currentEntry = jis.getNextJarEntry();

			startCustomizers();
			
			while (currentEntry != null && processResource(currentEntry))
				currentEntry = jis.getNextJarEntry();

			dropStaleResources();
			uninstallStaleBundles();
			prepare();
			commit();
			source.startBundles();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return null;
		
	}

	private boolean processResource(JarEntry currentEntry) {
		return true;
	}

	private boolean processBundle(JarEntry currentEntry) throws IOException {
		String bsn = currentEntry.getAttributes().getValue(Constants.BUNDLE_SYMBOLICNAME);
		if(bsn != null) {
			return true;			
		}
		return false;
	}

	private void commit() {	
	}


	private void uninstallStaleBundles() {
	}

	private void dropStaleResources() {
	}

	private void prepare() {
	}

	private void startCustomizers() {
	}

	private boolean processLocalizationFile(String bundleLocalization, JarEntry currentEntry) {
		String name = currentEntry.getName();
		if(name.startsWith(bundleLocalization)) {
			return true;			
		}
		return false;
	}

	private boolean processSignatureFile(JarEntry currentEntry) {
		String name = currentEntry.getName();
		if(name.startsWith("META-INF/")) {
			if (name.endsWith(".SF")) {
				return true;
			} else if (name.endsWith("*.DSA")) {
				return true;
			} else if (name.endsWith("*.RS")) {
				return true;
			}
		}
		return false;
	}

	private void validateManifest(Manifest manifest) throws DeploymentException{
		
		Attributes mainAttributes = manifest.getMainAttributes();
		String symbolicName = mainAttributes.getValue("DeploymentPackage-SymbolicName");
		if (symbolicName == null)
			throw new DeploymentException(DeploymentException.CODE_MISSING_HEADER);
				
		String version = mainAttributes.getValue("DeploymentPackage-Version");
		if (version == null)
			throw new DeploymentException(DeploymentException.CODE_MISSING_HEADER);
		
		try {
			if (Version.parseVersion(version).equals(Version.emptyVersion))
				throw new DeploymentException(DeploymentException.CODE_BAD_HEADER);
		} catch (IllegalArgumentException e) {
			throw new DeploymentException(DeploymentException.CODE_BAD_HEADER);
		}

		DeploymentPackage target = getDeploymentPackage(symbolicName);
		
		String fixPack = mainAttributes.getValue("DeploymentPackage-FixPack");
		boolean isFixPack = (fixPack != null);
		if (isFixPack) {
			VersionRange fixPackVersionRange = null;
			try {
				fixPackVersionRange = new VersionRange(fixPack);
			} catch (IllegalArgumentException e) {
				throw new DeploymentException(DeploymentException.CODE_BAD_HEADER);
			}
			
			if (target == null || fixPackVersionRange.isIncluded(target.getVersion()))
				throw new DeploymentException(DeploymentException.CODE_MISSING_FIXPACK_TARGET);
		}

		for (Iterator it = manifest.getEntries().values().iterator(); it.hasNext();) {
			Attributes attributes = (Attributes) it.next();
			String missing = attributes.getValue("DeploymentPackage-Missing");
			if (!isFixPack && missing != null)
				throw new DeploymentException(DeploymentException.CODE_BAD_HEADER);
			
			boolean isMissing = new Boolean(missing).booleanValue();
			
			String bsn = attributes.getValue("Bundle-SymbolicName");
			if (bsn == null)
				continue;
			
			DeploymentPackage bundleDeploymentPackage = getDeploymentPackage(bsn);
			if (isMissing && bundleDeploymentPackage != target)
				throw new DeploymentException(DeploymentException.CODE_MISSING_BUNDLE);
			
			if (bundleDeploymentPackage != null && bundleDeploymentPackage != target)
				throw new DeploymentException(DeploymentException.CODE_BUNDLE_SHARING_VIOLATION);
		}	
	}

	public DeploymentPackage[] listDeploymentPackages() {
		Collection deploymentPackages = deploymentPackagesMap.values();	
		return (DeploymentPackage[]) deploymentPackages.toArray(new DeploymentPackage[deploymentPackages.size()]);
	}

	void add(DeploymentPackageImpl impl) {
		deploymentPackagesMap.put(impl.getName(), impl);
	}
	
	void remove(DeploymentPackageImpl impl) {
		deploymentPackagesMap.remove(impl.getName());
	}

}
