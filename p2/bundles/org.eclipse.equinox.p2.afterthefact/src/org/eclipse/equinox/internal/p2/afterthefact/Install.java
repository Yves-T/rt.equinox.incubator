/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.equinox.internal.p2.afterthefact;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.equinox.internal.p2.artifact.repository.simple.SimpleArtifactRepository;
import org.eclipse.equinox.internal.provisional.p2.artifact.repository.IArtifactRepository;
import org.eclipse.equinox.internal.provisional.p2.artifact.repository.IArtifactRepositoryManager;
import org.eclipse.equinox.internal.provisional.p2.core.ProvisionException;
import org.eclipse.equinox.internal.provisional.p2.director.IPlanner;
import org.eclipse.equinox.internal.provisional.p2.director.PlannerHelper;
import org.eclipse.equinox.internal.provisional.p2.director.ProfileChangeRequest;
import org.eclipse.equinox.internal.provisional.p2.director.ProvisioningPlan;
import org.eclipse.equinox.internal.provisional.p2.engine.DefaultPhaseSet;
import org.eclipse.equinox.internal.provisional.p2.engine.IEngine;
import org.eclipse.equinox.internal.provisional.p2.engine.IProfile;
import org.eclipse.equinox.internal.provisional.p2.engine.IProfileRegistry;
import org.eclipse.equinox.internal.provisional.p2.engine.InstallableUnitOperand;
import org.eclipse.equinox.internal.provisional.p2.engine.InstallableUnitPropertyOperand;
import org.eclipse.equinox.internal.provisional.p2.engine.Operand;
import org.eclipse.equinox.internal.provisional.p2.engine.ProvisioningContext;
import org.eclipse.equinox.internal.provisional.p2.metadata.IInstallableUnit;
import org.eclipse.equinox.internal.provisional.p2.metadata.query.InstallableUnitQuery;
import org.eclipse.equinox.internal.provisional.p2.metadata.repository.IMetadataRepositoryManager;
import org.eclipse.equinox.internal.provisional.p2.query.Collector;
import org.eclipse.osgi.service.resolver.PlatformAdmin;
import org.osgi.service.packageadmin.PackageAdmin;

public class Install {
	private IArtifactRepository repo;

	public void doInstall() {
		//Acquire services that are necessary for the rest of the installation
		IProfileRegistry registry = (IProfileRegistry) ServiceHelper.getService(Activator.getContext(), IProfileRegistry.class.getName());
		PlatformAdmin platformAdmin = (PlatformAdmin) ServiceHelper.getService(Activator.getContext(), PlatformAdmin.class.getName());
		IEngine engine = (IEngine) ServiceHelper.getService(Activator.getContext(), IEngine.class.getName());
		IPlanner planner = (IPlanner) ServiceHelper.getService(Activator.getContext(), IPlanner.class.getName());
		IMetadataRepositoryManager repoMgr = (IMetadataRepositoryManager) ServiceHelper.getService(Activator.getContext(), IMetadataRepositoryManager.class.getName());
		IArtifactRepositoryManager artifactRepoMgr = (IArtifactRepositoryManager) ServiceHelper.getService(Activator.getContext(), IArtifactRepositoryManager.class.getName());

		//Create a bundle pool. This does not have to be done all the time
		repo = createBundlePool(artifactRepoMgr);

		//Get the IU to install
		IInstallableUnit[] iusToInstall = getRandomIUToInstall(repoMgr, artifactRepoMgr);
		
		//Create a representation of what is already installed into the running system
		Collection ius = new Reify().reify(platformAdmin);
		IProfile profile = null;
		try {
			profile = spoofUpProfile(registry, engine, ius);
		} catch (ProvisionException e) {
			e.printStackTrace();
		}
		if (profile == null)
			return;

		//Create a request to install the IUs and compute a plan to install those
		ProfileChangeRequest request = new ProfileChangeRequest(profile);
		request.addInstallableUnits(iusToInstall);
		ProvisioningPlan plan = planner.getProvisioningPlan(request, new ProvisioningContext(), null);
		
		//Execute the plan. This causes the files to be downloaded and the bundles to be installed
		System.out.println(engine.perform(profile, new DefaultPhaseSet(), plan.getOperands(), null, null));
		
		//Refresh the framework to get the bundles installed
		PackageAdmin packageAdmin = (PackageAdmin) ServiceHelper.getService(Activator.getContext(), PackageAdmin.class.getName());
		packageAdmin.refreshPackages(null);
	}

	//Helper method to get IUs to install
	private IInstallableUnit[] getRandomIUToInstall(IMetadataRepositoryManager repoMgr, IArtifactRepositoryManager artifactMgr) {
		try {
			repoMgr.addRepository(new URI("http://download.eclipse.org/releases/galileo"));
			artifactMgr.addRepository(new URI("http://download.eclipse.org/releases/galileo"));
		} catch (URISyntaxException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		Collector c = repoMgr.query(new InstallableUnitQuery("org.eclipse.emf"), new Collector(), new NullProgressMonitor());
		return new IInstallableUnit[] {(IInstallableUnit) c.iterator().next(), Reify.createDefaultBundleConfigurationUnit()};
	}

	private IProfile spoofUpProfile(IProfileRegistry registry, IEngine engine, Collection ius) throws ProvisionException {
		Properties prop = new Properties();
		// prop.setProperty("org.eclipse.bund, value)
		// set the bundle pool
		// create the artifact repository
		prop.setProperty("org.eclipse.equinox.p2.bundlepool", repo.getLocation().toString());
		IProfile profile = registry.addProfile("foobar" + System.currentTimeMillis(), prop);
		Operand[] operands = new Operand[ius.size() * 2];
		int i = 0;
		for (Iterator iter = ius.iterator(); iter.hasNext();) {
			IInstallableUnit iu = (IInstallableUnit) iter.next();
			operands[i++] = new InstallableUnitOperand(null, iu);
			operands[i++] = new InstallableUnitPropertyOperand(iu, "org.eclipse.equinox.p2.internal.inclusion.rules", null, PlannerHelper.createOptionalInclusionRule(iu));
		}
		IStatus status = engine.perform(profile, DefaultPhaseSet.createDefaultPhaseSet(DefaultPhaseSet.PHASE_CHECK_TRUST | DefaultPhaseSet.PHASE_COLLECT | DefaultPhaseSet.PHASE_CONFIGURE | DefaultPhaseSet.PHASE_UNCONFIGURE | DefaultPhaseSet.PHASE_UNINSTALL), operands, new ProvisioningContext(), null);
		if (!status.isOK())
			return null;
		return profile;
	}

	private IArtifactRepository createBundlePool(IArtifactRepositoryManager mgr) {
		final String[][] DEFAULT_MAPPING_RULES = { { "(& (classifier=osgi.bundle))", "${repoUrl}/lib/${id}_${version}.jar" } };//$NON-NLS-1$
		IArtifactRepository repo;
		try {
			try {
				repo = mgr.loadRepository(new URI("file:/Users/Pascal/tmp/bundlepool"), null);
				return repo;
			} catch (ProvisionException e) {
				repo = mgr.createRepository(new URI("file:/Users/Pascal/tmp/bundlepool"), "bundle pool", IArtifactRepositoryManager.TYPE_SIMPLE_REPOSITORY, (Map) null);
			}
		} catch (ProvisionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		// TODO This is a major hack because the rules can't be set when creating the repo. 
		((SimpleArtifactRepository) repo).setRules(DEFAULT_MAPPING_RULES);
		((SimpleArtifactRepository) repo).save();
		//((SimpleArtifactRepository) repo).initializeMapper();
		Method initializeMapper;
		try {
			initializeMapper = SimpleArtifactRepository.class.getDeclaredMethod("initializeMapper", null);
			initializeMapper.setAccessible(true);
			initializeMapper.invoke(repo, null);
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		return repo;
	}

}
