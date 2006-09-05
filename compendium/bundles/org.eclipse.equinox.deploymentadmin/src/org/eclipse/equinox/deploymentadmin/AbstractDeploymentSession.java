package org.eclipse.equinox.deploymentadmin;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedAction;

import org.osgi.framework.Bundle;
import org.osgi.service.deploymentadmin.DeploymentException;
import org.osgi.service.deploymentadmin.DeploymentPackage;

public abstract class AbstractDeploymentSession {

	private DeploymentPackage sourceDeploymentPackage;
	private DeploymentPackage targetDeploymentPackage;

	public File getDataFile(Bundle bundle) {
	
		final Bundle dataBundle = bundle;
		
		return (File) AccessController.doPrivileged(new PrivilegedAction() {
			public Object run() {
				try {
					Method getBundleData = dataBundle.getClass().getMethod("getBundleData", null); //$NON-NLS-1$
					Object bundleData = getBundleData.invoke(dataBundle, null);
					
					Method getDataFile = bundleData.getClass().getMethod("getDataFile", new Class[] {String[].class}); //$NON-NLS-1$
					return getDataFile.invoke(bundleData, new Object[] {""}); //$NON-NLS-1$
				} catch (Exception e) {
					throw new IllegalStateException(e.getMessage());
				}
			}
		});
	}

	public DeploymentPackage getSourceDeploymentPackage() {
		return sourceDeploymentPackage;
	}

	public void setSourceDeploymentPackage(DeploymentPackage sourceDeploymentPackage) {
		this.sourceDeploymentPackage = sourceDeploymentPackage;
	}

	public DeploymentPackage getTargetDeploymentPackage() {
		return targetDeploymentPackage;
	}
	
	public void setTargetDeploymentPackage(DeploymentPackage targetDeploymentPackage) {
		this.targetDeploymentPackage = targetDeploymentPackage;
	}
	
	abstract void begin() throws DeploymentException, IOException;
	abstract void prepare() throws DeploymentException;
	abstract void rollback() throws DeploymentException;
	abstract void commit() throws DeploymentException;	
}