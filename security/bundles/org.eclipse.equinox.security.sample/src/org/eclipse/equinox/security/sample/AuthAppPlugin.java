/*******************************************************************************
 * Copyright (c) 2005, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.equinox.security.sample;

//import org.eclipse.osgi.internal.provisional.verifier.CertificateTrustAuthority;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.*;
import org.eclipse.equinox.security.sample.engine.MyTrustEngine;
import org.eclipse.equinox.security.storage.ISecurePreferences;
import org.eclipse.equinox.security.storage.SecurePreferencesFactory;
import org.eclipse.equinox.security.storage.provider.IProviderHints;
import org.eclipse.osgi.service.resolver.PlatformAdmin;
import org.eclipse.osgi.service.security.TrustEngine;
import org.osgi.framework.*;
import org.osgi.service.packageadmin.PackageAdmin;
import org.osgi.util.tracker.ServiceTracker;

public class AuthAppPlugin implements BundleActivator {

	//	private static char[] PASSWORD_DEFAULT = {'c', 'h', 'a', 'n', 'g', 'e', 'i', 't'};
	//	private static String TYPE_DEFAULT = "JKS"; //$NON-NLS-1$
	private static final String MY_KEYSTORE_JKS = "data/keystore.jks"; //$NON-NLS-1$
	private static ServiceRegistration systemTrustEngineReg;
	private static final char[] PASSWORD_DEFAULT = {'c', 'h', 'a', 'n', 'g', 'e', 'i', 't'};
	private static final String TYPE_DEFAULT = "jks";

	/**
	 * The unique identifier constant of this plug-in.
	 */
	public static final String PI_AUTH = "org.eclipse.equinox.security.sample"; //$NON-NLS-1$

	private static BundleContext bundleContext;

	private static ServiceTracker platformAdminTracker;
	private static ServiceTracker certTrustAuthorityTracker;
	private static ServiceTracker packageAdminTracker;

	private static ISecurePreferences passStorePreference;

	private static final File WINDOWS_PASSWORD_FILE = new File("c:/mypassword.txt");

	public void start(BundleContext context) throws Exception {
		bundleContext = context;
	}

	public void stop(BundleContext context) throws Exception {
		bundleContext = context;

		if (null != platformAdminTracker)
			platformAdminTracker.close();

		if (null != certTrustAuthorityTracker)
			certTrustAuthorityTracker.close();

		if (null != packageAdminTracker)
			packageAdminTracker.close();

		if (null != systemTrustEngineReg)
			systemTrustEngineReg.unregister();
	}

	public static BundleContext getBundleContext() {
		return bundleContext;
	}

	public static PlatformAdmin getPlatformAdmin() {
		if (null == platformAdminTracker) {
			platformAdminTracker = new ServiceTracker(bundleContext, PlatformAdmin.class.getName(), null);
			platformAdminTracker.open();
		}
		return (PlatformAdmin) platformAdminTracker.getService();
	}

	//	public static CertificateTrustAuthority getCertTrustAuthority() {
	//		if (null == certTrustAuthorityTracker) {
	//			certTrustAuthorityTracker = new ServiceTracker(bundleContext, CertificateTrustAuthority.class.getName(), null);
	//			certTrustAuthorityTracker.open();
	//		}
	//		return (CertificateTrustAuthority) certTrustAuthorityTracker.getService();
	//	}

	public static PackageAdmin getPackageAdmin() {
		if (null == packageAdminTracker) {
			packageAdminTracker = new ServiceTracker(bundleContext, PackageAdmin.class.getName(), null);
			packageAdminTracker.open();
		}
		return (PackageAdmin) packageAdminTracker.getService();
	}

	public static ISecurePreferences getPassStoreSecurePreference() {
		if (passStorePreference == null) {
			if (!WINDOWS_PASSWORD_FILE.exists()) {
				try {
					WINDOWS_PASSWORD_FILE.createNewFile();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					return null;
				}
			}

			Map options = new HashMap();
			options.put(IProviderHints.REQUIRED_MODULE_ID, "org.eclipse.equinox.security.ui.DefaultPasswordProvider");
			try {
				passStorePreference = SecurePreferencesFactory.open(WINDOWS_PASSWORD_FILE.toURL(), options).node("org.eclipse.equinox.sample.password");
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return passStorePreference;
	}

	public static boolean InstallTrustEngine() {
		if (null == systemTrustEngineReg) {
			MyTrustEngine myTrustEngine = new MyTrustEngine(bundleContext.getBundle().getEntry(MY_KEYSTORE_JKS), PASSWORD_DEFAULT, TYPE_DEFAULT, "MyKeyStoreEngine");

			Hashtable properties = new Hashtable(7);
			properties.put(Constants.SERVICE_RANKING, new Integer(Integer.MAX_VALUE));
			//		properties.put(SignedContentConstants.TRUST_ENGINE, SignedContentConstants.DEFAULT_TRUST_ENGINE);
			//		KeyStoreTrustEngine systemTrustEngine = new KeyStoreTrustEngine(CACERTS_PATH, CACERTS_TYPE, null, "System"); //$NON-NLS-1$
			systemTrustEngineReg = bundleContext.registerService(TrustEngine.class.getName(), myTrustEngine, properties);
		}
		return true;
	}
}
