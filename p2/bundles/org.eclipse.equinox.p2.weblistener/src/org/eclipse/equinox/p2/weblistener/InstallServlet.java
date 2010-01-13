/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.equinox.p2.weblistener;

import org.eclipse.equinox.p2.metadata.Version;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.*;
import org.eclipse.core.runtime.*;
import org.eclipse.equinox.internal.p2.console.ProvisioningHelper;
import org.eclipse.equinox.internal.p2.core.helpers.ServiceHelper;
import org.eclipse.equinox.internal.provisional.p2.director.IPlanner;
import org.eclipse.equinox.internal.provisional.p2.director.ProfileChangeRequest;
import org.eclipse.equinox.p2.core.ProvisionException;
import org.eclipse.equinox.p2.engine.*;
import org.eclipse.equinox.p2.internal.weblistener.WebListenerActivator;
import org.eclipse.equinox.p2.metadata.IInstallableUnit;
import org.eclipse.equinox.p2.metadata.query.InstallableUnitQuery;
import org.eclipse.equinox.p2.query.IQueryResult;

public class InstallServlet extends HttpServlet implements Servlet {

	private static final long serialVersionUID = 2446005384786238875L;

	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		// TODO Auto-generated method stub
		String id = req.getParameter("id");
		String version = req.getParameter("version");
		String metadataRepo = req.getParameter("metadataRepo");
		String artifactRepo = req.getParameter("artifactRepo");
		try {
			ProvisioningHelper.addMetadataRepository(URIUtil.fromString(metadataRepo));
			ProvisioningHelper.addArtifactRepository(URIUtil.fromString(artifactRepo));
			install(id, version, ProvisioningHelper.getProfile(IProfileRegistry.SELF), null);
		} catch (ProvisionException e) {
			resp.getWriter().println("alert(\'Problem installing\');");
		} catch (URISyntaxException e) {
			resp.getWriter().println("alert(\'Problem installing\');");
		}
		resp.getWriter().println("alert(\'install of " + id + ", " + version + "done\');");
	}

	private IStatus install(String unitId, String version, IProfile profile, IProgressMonitor progress) throws ProvisionException {
		if (profile == null)
			return null;
		IQueryResult units = ProvisioningHelper.getInstallableUnits((URI) null, new InstallableUnitQuery(unitId, Version.create(version)), progress);
		if (units.isEmpty()) {
			StringBuffer error = new StringBuffer();
			error.append("Installable unit not found: " + unitId + ' ' + version + '\n');
			error.append("Repositories searched:\n");
			URI[] repos = ProvisioningHelper.getMetadataRepositories();
			if (repos != null) {
				for (int i = 0; i < repos.length; i++)
					error.append(repos[i] + "\n");
			}
			throw new ProvisionException(error.toString());
		}

		IPlanner planner = (IPlanner) ServiceHelper.getService(WebListenerActivator.getContext(), IPlanner.class.getName());
		if (planner == null)
			throw new ProvisionException("No planner service found.");

		IEngine engine = (IEngine) ServiceHelper.getService(WebListenerActivator.getContext(), IEngine.SERVICE_NAME);
		if (engine == null)
			throw new ProvisionException("No engine service found.");
		ProvisioningContext context = new ProvisioningContext();
		ProfileChangeRequest request = new ProfileChangeRequest(profile);
		request.addInstallableUnits(units);
		request.setInstallableUnitProfileProperty((IInstallableUnit) units.iterator().next(), IProfile.PROP_PROFILE_ROOT_IU, "true");
		IProvisioningPlan result = planner.getProvisioningPlan(request, context, progress);
		if (!result.getStatus().isOK())
			return result.getStatus();

		return engine.perform(result, progress);
	}

}
