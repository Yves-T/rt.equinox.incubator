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
package org.eclipse.equinox.security.sample;

import org.eclipse.jface.action.Action;

public class ReloadAction extends Action {

	public ReloadAction(String label) {
		setText(label);
	}

	public void run() {
		//				System.err.println("Reload...");
		//		
		//				PlatformAdmin plaformAdmin = AuthAppPlugin.getPlatformAdmin();
		//				CertificateTrustAuthority certTrustAuthority = AuthAppPlugin.getCertTrustAuthority();
		//				PackageAdmin packageAdmin = AuthAppPlugin.getPackageAdmin();
		//		
		//				State state = plaformAdmin.getState(false);
		//		
		//				// iterate through each bundle in the state and check
		//				BundleDescription[] bds = plaformAdmin.getState().getDisabledBundles();
		//				List disableBundles = new LinkedList();
		//				for (int i = 0; i < bds.length; i++) {
		//					CertificateChain[] currentCertChain = retrieveCertChain(bds[i]);
		//		
		//					try {
		//						certTrustAuthority.checkTrust(currentCertChain[0].getCertificates());
		//					} catch (CertificateException e) {
		//		
		//						// disable the bundle
		//						DisabledInfo disabledInfo = new DisabledInfo("Disable!", "Disable!", bds[i]);
		//						plaformAdmin.addDisabledInfo(disabledInfo);
		//		
		//						Bundle bd = AuthAppPlugin.getBundleContext().getBundle(bds[i].getBundleId());
		//						disableBundles.add(bd);
		//					}
		//				}
		//		
		//				Bundle[] arrayBundles = (Bundle[]) disableBundles.toArray(new Bundle[disableBundles.size()]);
		//				packageAdmin.refreshPackages(arrayBundles);
	}

	//	private CertificateChain[] retrieveCertChain(BundleDescription bundleDescription) {
	//		Bundle bundle = AuthAppPlugin.getBundleContext().getBundle(bundleDescription.getBundleId());
	//
	//		if (bundle instanceof AbstractBundle) {
	//
	//			AbstractBundle bu = (AbstractBundle) bundle;
	//			BundleData bData = bu.getBundleData();
	//			BaseData baseData = (BaseData) bData;
	//			BundleFile bf = baseData.getBundleFile();
	//
	//			if (bf instanceof SignedBundleFile) {
	//				SignedBundleFile sbf = (SignedBundleFile) bf;
	//				return sbf.getChains();
	//			}
	//		}
	//		return null;
	//	}
}
