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

import java.util.Set;
import java.util.jar.Manifest;

import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.Version;
import org.osgi.service.deploymentadmin.BundleInfo;
import org.osgi.service.deploymentadmin.DeploymentException;
import org.osgi.service.deploymentadmin.DeploymentPackage;

public class DeploymentPackageImpl implements DeploymentPackage {

	public static final DeploymentPackage EMPTY = new DeploymentPackage (){

		public Bundle getBundle(String symbolicName) {
			return null;
		}

		public BundleInfo[] getBundleInfos() {
			return new BundleInfo[0];
		}

		public String getHeader(String header) {
			if (header.equals(("DeploymentPackage-SymbolicName")))
				return "";
			else if (header.equals(("DeploymentPackage-Version")))
				return "0.0.0";
			else
				return null;
		}

		public String getName() {
			return "";
		}

		public String getResourceHeader(String resource, String header) {
			return null;
		}

		public ServiceReference getResourceProcessor(String resource) {
			return null;
		}

		public String[] getResources() {
			return new String[0];
		}

		public Version getVersion() {
			return new Version("0.0.0");
		}

		public boolean isStale() {
			return true;
		}

		public void uninstall() throws DeploymentException {
			throw new IllegalStateException();
		}

		public void uninstallForced() throws DeploymentException {
			throw new IllegalStateException();
		}
		
	};
	private String name;
	private Manifest manifest;
	private Version version;
	DeploymentAdminImpl admin;

	public DeploymentPackageImpl(Manifest manifest) throws DeploymentException {
		manifest = this.manifest;
		name = manifest.getMainAttributes().getValue("DeploymentPackage-SymbolicName"); //$NON-NLS-1$
		version = new Version(manifest.getMainAttributes().getValue("DeploymentPackage-Version")); //$NON-NLS-1$
			
		if (name == null || version == null)
			throw new DeploymentException(DeploymentException.CODE_MISSING_HEADER);
	}

	public Bundle getBundle(String symbolicName) {
		return null;
	}

	public BundleInfo[] getBundleInfos() {
		return null;
	}

	public String getHeader(String header) {
		return manifest.getMainAttributes().getValue(header);
	}

	public String getName() {
		return name;
	}

	public String getResourceHeader(String resource, String header) {
		return manifest.getAttributes(resource).getValue(header);
	}

	public ServiceReference getResourceProcessor(String resource) {
		return null;
	}

	public String[] getResources() {
		Set resourceNamesSet = manifest.getEntries().keySet();		
		return (String[]) resourceNamesSet.toArray(new String[resourceNamesSet.size()]);
	}

	public Version getVersion() {
		return version;
	}

	public boolean isStale() {
		return false;
	}

	public void uninstall() throws DeploymentException {
		admin.remove(this);
	}

	public void uninstallForced() throws DeploymentException {
		admin.remove(this);
	}

	public void stopBundles() {
		// TODO Auto-generated method stub
		
	}

	public void startBundles() {
		// TODO Auto-generated method stub
		
	}
}
