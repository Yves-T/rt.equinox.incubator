package org.eclipse.equinox.deploymentadmin;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.jar.Manifest;

import org.osgi.framework.Constants;
import org.osgi.framework.Version;
import org.osgi.service.deploymentadmin.DeploymentAdmin;
import org.osgi.service.deploymentadmin.DeploymentException;
import org.osgi.service.deploymentadmin.DeploymentPackage;
import org.osgi.service.deploymentadmin.spi.DeploymentSession;

public class InstallDeploymentSession extends AbstractDeploymentSession  implements DeploymentSession {

	private InputStream in;
	private DeploymentAdminImpl admin;

	InstallDeploymentSession(InputStream in, DeploymentAdminImpl admin) {
		if (in == null)
			throw new IllegalArgumentException("InputStream must not be null"); //$NON-NLS-1$
		this.in = in;
		this.admin = admin;
	}
	
	
	void begin() throws IOException, DeploymentException {
		JarInputStream jis = null;
		try {
			jis = new JarInputStream(in);
		} catch (IOException e) {
			throw new DeploymentException(DeploymentException.CODE_NOT_A_JAR);
		}		
		
		Manifest manifest = jis.getManifest();
		if (manifest == null)
			throw new DeploymentException(DeploymentException.CODE_ORDER_ERROR);
		
		validateManifest(manifest);
		
		DeploymentPackageImpl source = new DeploymentPackageImpl(manifest);
		DeploymentPackageImpl target = admin.getDeploymentPackageImpl(source.getName());
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
	}

	void commit() {
		// TODO Auto-generated method stub
		
	}

	void prepare() {
		// TODO Auto-generated method stub
		
	}
	
	private void uninstallStaleBundles() {
	}

	private void dropStaleResources() {
	}

	private void startCustomizers() {
	}


	void rollback() {
		// TODO Auto-generated method stub
		
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

		DeploymentPackage target = admin.getDeploymentPackage(symbolicName);
		
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
			
			DeploymentPackage bundleDeploymentPackage = admin.getDeploymentPackage(bsn);
			if (isMissing && bundleDeploymentPackage != target)
				throw new DeploymentException(DeploymentException.CODE_MISSING_BUNDLE);
			
			if (bundleDeploymentPackage != null && bundleDeploymentPackage != target)
				throw new DeploymentException(DeploymentException.CODE_BUNDLE_SHARING_VIOLATION);
		}	
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
}
