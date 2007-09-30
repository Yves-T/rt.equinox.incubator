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

import java.util.HashSet;
import java.util.Iterator;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.equinox.prov.core.helpers.MultiStatus;
import org.eclipse.equinox.prov.engine.Engine;
import org.eclipse.equinox.prov.engine.InstallOperand;
import org.eclipse.equinox.prov.engine.Profile;
import org.eclipse.equinox.prov.engine.ProvisioningOperandCollection;
import org.eclipse.equinox.prov.metadata.InstallableUnit;
import org.eclipse.equinox.prov.operations.InstallOperation;
import org.eclipse.equinox.prov.resolution.ResolutionHelper;

public class ProfileFiller {
	HashSet allThingsToInstall = new HashSet();
	Profile profile;

	public void add(InstallableUnit toAdd) {
		allThingsToInstall.add(toAdd);
	}

	public void setProfile(Profile profile) {
		this.profile = profile;
	}

	private ProvisioningOperandCollection buildInstallRequestData(HashSet toInstall, Profile p) {
		ProvisioningOperandCollection operands = new ProvisioningOperandCollection(toInstall.size());
		for (Iterator iterator = toInstall.iterator(); iterator.hasNext();) {
			InstallableUnit iu = (InstallableUnit) iterator.next();
			InstallOperand toAdd = new InstallOperand(iu, p);
			operands.add(toAdd);
		}
		return operands;
	}

	public MultiStatus generate(IProgressMonitor pm) {
		new ResolutionHelper(profile.getSelectionContext(), null).attachCUs(allThingsToInstall);
		return new Engine().perform(new InstallOperation(), buildInstallRequestData(allThingsToInstall, profile), pm);
	}
}
