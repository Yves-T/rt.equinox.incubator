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

import java.io.InputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.osgi.framework.Bundle;
import org.osgi.framework.Constants;
import org.osgi.framework.Version;
import org.osgi.service.deploymentadmin.BundleInfo;
import org.osgi.service.deploymentadmin.DeploymentAdmin;
import org.osgi.service.deploymentadmin.DeploymentException;
import org.osgi.service.deploymentadmin.DeploymentPackage;

public class DeploymentAdminImpl implements DeploymentAdmin {

	private Map deploymentPackagesMap = new HashMap();

	public boolean cancel() {
		return false;
	}

	public DeploymentPackage getDeploymentPackage(String symbName) {
		return getDeploymentPackageImpl(symbName);
	}

	DeploymentPackageImpl getDeploymentPackageImpl(String symbName) {
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
		InstallDeploymentSession session = new InstallDeploymentSession(in, this);
		
		try {
			session.begin();
			session.prepare();
			session.commit();
		} catch (Throwable t) {
			try {
				session.rollback();
			} catch (Throwable traceOnly) {
				traceOnly.printStackTrace();
			}
			throw (t instanceof DeploymentException) ? (DeploymentException) t : new DeploymentException(DeploymentException.CODE_OTHER_ERROR, t.getMessage(), t); 
		}		
		return session.getTargetDeploymentPackage();
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
