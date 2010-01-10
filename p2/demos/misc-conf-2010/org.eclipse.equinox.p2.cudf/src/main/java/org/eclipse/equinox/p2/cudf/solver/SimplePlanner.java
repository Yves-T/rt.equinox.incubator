/*******************************************************************************
 * Copyright (c) 2007-2009 IBM Corporation and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.equinox.p2.cudf.solver;

import java.util.ArrayList;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.equinox.p2.cudf.metadata.*;
import org.eclipse.equinox.p2.cudf.query.QueryableArray;

public class SimplePlanner {
	public static boolean explain = false; //SET THIS TO FALSE FOR THE COMPETITION
	private final static boolean PURGE = true;

	public Object getSolutionFor(ProfileChangeRequest profileChangeRequest, String optFunction, String timeout) {
		QueryableArray profile = profileChangeRequest.getInitialState();

		InstallableUnit updatedPlan = updatePlannerInfo(profileChangeRequest);

		Slicer slice = new Slicer(profile);
		profile = slice.slice(updatedPlan, profileChangeRequest.getExtraRequirements());
		if (PURGE)
			profileChangeRequest.purge();
		Projector projector = new Projector(profile);
		projector.encode(updatedPlan, optFunction, timeout);
		IStatus s = projector.invokeSolver();
		if (s.getSeverity() == IStatus.ERROR) {
			if (explain)
				System.out.println("# " + projector.getExplanation());
			return s;
		}

		return projector.extractSolution();
	}

	private InstallableUnit updatePlannerInfo(ProfileChangeRequest profileChangeRequest) {
		return createIURepresentingTheProfile(profileChangeRequest.getAllRequests());
	}

	private InstallableUnit createIURepresentingTheProfile(ArrayList allRequirements) {
		InstallableUnit iud = new InstallableUnit();
		String time = Long.toString(System.currentTimeMillis());
		iud.setId(time);
		iud.setVersion(new Version(0, 0, 0, time));
		iud.setRequiredCapabilities((IRequiredCapability[]) allRequirements.toArray(new IRequiredCapability[allRequirements.size()]));
		return iud;
	}
}
