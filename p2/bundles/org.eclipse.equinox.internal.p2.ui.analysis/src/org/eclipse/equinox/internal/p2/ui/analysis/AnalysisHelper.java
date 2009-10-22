package org.eclipse.equinox.internal.p2.ui.analysis;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Dictionary;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.equinox.internal.p2.core.helpers.ServiceHelper;
import org.eclipse.equinox.internal.p2.director.QueryableArray;
import org.eclipse.equinox.internal.p2.director.Slicer;
import org.eclipse.equinox.internal.p2.metadata.repository.CompositeMetadataRepository;
import org.eclipse.equinox.internal.p2.ui.analysis.viewers.TreeElement;
import org.eclipse.equinox.internal.provisional.p2.engine.IProfile;
import org.eclipse.equinox.internal.provisional.p2.engine.IUProfilePropertyQuery;
import org.eclipse.equinox.internal.provisional.p2.metadata.IInstallableUnit;
import org.eclipse.equinox.internal.provisional.p2.metadata.IRequiredCapability;
import org.eclipse.equinox.internal.provisional.p2.metadata.query.CapabilityQuery;
import org.eclipse.equinox.internal.provisional.p2.metadata.query.Collector;
import org.eclipse.equinox.internal.provisional.p2.metadata.query.IQueryable;
import org.eclipse.equinox.internal.provisional.p2.metadata.query.InstallableUnitQuery;
import org.eclipse.equinox.internal.provisional.p2.metadata.repository.IMetadataRepository;
import org.eclipse.equinox.internal.provisional.p2.metadata.repository.IMetadataRepositoryManager;
import org.eclipse.equinox.internal.provisional.p2.repository.IRepositoryManager;

public class AnalysisHelper {

	// Get the root IUs of a profile
	public static IInstallableUnit[] getProfileRoots(IProfile profile, IProgressMonitor monitor) {
		return (IInstallableUnit[]) profile.query(new IUProfilePropertyQuery(IInstallableUnit.PROP_PROFILE_ROOT_IU, Boolean.TRUE.toString()), new Collector(), monitor).toArray(IInstallableUnit.class);
	}

	// Determine if the profile is valid (ie, all dependencies met)
	public static IStatus checkProfileValidity(IProfile profile, Collector collector, IProgressMonitor monitor) {
		return checkValidity(profile, new IInstallableUnit[0], collector, monitor);
	}

	// Check the validity of a profile when some IUs are removed.  (Can be none)
	public static IStatus checkValidity(IProfile profile, IInstallableUnit[] iusToRemove, Collector collector, IProgressMonitor monitor) {
		try {
			monitor.beginTask("Checking profile", 70);
			Collection profileRootIUs = new ArrayList(Arrays.asList(getProfileRoots(profile, new SubProgressMonitor(monitor, 10))));
			Collection profileIUs = new ArrayList(getProfileIUs(profile, new SubProgressMonitor(monitor, 10)));

			// Remove IUs from those collected from the profile
			for (int i = 0; i < iusToRemove.length; i++) {
				profileRootIUs.remove(iusToRemove[i]);
				profileIUs.remove(iusToRemove[i]);
			}

			Slicer slicer = new Slicer(new QueryableArray((IInstallableUnit[]) profileIUs.toArray(new IInstallableUnit[profileIUs.size()])), new Hashtable(profile.getProperties()), true);
			slicer.slice((IInstallableUnit[]) profileRootIUs.toArray(new IInstallableUnit[profileRootIUs.size()]), new SubProgressMonitor(monitor, 40));

			IStatus slicerStatus = slicer.getStatus();
			if (slicerStatus.isOK())
				return Status.OK_STATUS;
			else if (slicerStatus.matches(IStatus.CANCEL))
				return slicerStatus;

			// Something is wrong with the profile, attempt to determine which IUs are broken by the change(s)
			IStatus[] children = slicerStatus.getChildren();
			Iterator iterator = profileIUs.iterator();
			while (iterator.hasNext()) {
				IInstallableUnit iu = (IInstallableUnit) iterator.next();
				for (int i = 0; i < children.length; i++) {
					if (children[i].getMessage().indexOf(iu.toString()) >= 0 && wouldBeSatisified(iu, iusToRemove))
						if (!collector.accept(iu))
							return slicerStatus;
				}
			}
			monitor.worked(10);
			return slicerStatus;
		} finally {
			monitor.done();
		}
	}

	/*
	 * Find the requirements missing from the given roots
	 */
	public static IRequiredCapability[] getMissingRequirements(IInstallableUnit[] roots, IQueryable queryable, Dictionary properties, IProgressMonitor monitor) {
		AnalysisSlicer aslicer = new AnalysisSlicer(queryable, properties, true);
		aslicer.slice(roots, new NullProgressMonitor());
		Collection req = aslicer.getMissingRequirements();

		return (IRequiredCapability[]) req.toArray(new IRequiredCapability[req.size()]);
	}

	/*
	 * Find IUs which satisfy the requirements
	 */
	public static IInstallableUnit[] satisfyRequirements(IRequiredCapability[] req, Dictionary properties, IProgressMonitor monitor) {
		IMetadataRepository repo = getMetadataRepository();

		CapabilityQuery query = new CapabilityQuery(req);
		Collector collector = repo.query(query, new Collector(), new NullProgressMonitor());

		return (IInstallableUnit[]) collector.toArray(IInstallableUnit.class);
	}

	//  Remove the specified IUs from queryable
	public static IInstallableUnit[] subtract(IQueryable queryable, IInstallableUnit[] toRemove) {
		Collector collector = queryable.query(InstallableUnitQuery.ANY, new Collector(), new NullProgressMonitor());
		HashSet set = new HashSet(collector.toCollection());

		for (int i = 0; i < toRemove.length; i++) {
			set.remove(toRemove[i]);
		}
		return (IInstallableUnit[]) set.toArray(new IInstallableUnit[set.size()]);
	}

	// Determine if any IU requirement is be satisfied by one of the removed Installable units
	private static boolean wouldBeSatisified(IInstallableUnit iu, IInstallableUnit[] removed) {
		if (removed.length == 0)
			return true;
		IRequiredCapability[] reqs = iu.getRequiredCapabilities();
		for (int n = 0; n < reqs.length; n++)
			for (int i = 0; i < removed.length; i++)
				if (removed[i].satisfies(reqs[n]))
					return true;
		return false;
	}

	// Get all the installable units in a profile
	private static Collection getProfileIUs(IProfile profile, IProgressMonitor monitor) {
		return profile.query(InstallableUnitQuery.ANY, new Collector(), monitor).toCollection();
	}

	// Get a composite metadata repository with children: all non-system repositories
	public static IMetadataRepository getMetadataRepository() {
		IMetadataRepositoryManager mgr = (IMetadataRepositoryManager) ServiceHelper.getService(AnalysisActivator.getDefault().getContext(), IMetadataRepositoryManager.class.getName());
		URI[] addresses = mgr.getKnownRepositories(IRepositoryManager.REPOSITORIES_NON_SYSTEM);
		CompositeMetadataRepository repo = CompositeMetadataRepository.createMemoryComposite();
		for (int i = 0; i < addresses.length; i++)
			repo.addChild(addresses[i]);

		return repo;
	}

	public static TreeElement diff(IInstallableUnit iu1, IInstallableUnit iu2) {
		TreeElement iuElement = new TreeElement(iu1.toString());
		IRequiredCapability[] reqs = iu1.getRequiredCapabilities();

		for (int i = 0; i < reqs.length; i++) {
			TreeElement element = requirementDifference(reqs[i], iu2.getRequiredCapabilities());
			if (element != null)
				iuElement.addChild(element);
		}

		if (iuElement.getChildren().length == 0)
			return null;
		return iuElement;
	}

	private static TreeElement requirementDifference(IRequiredCapability req, IRequiredCapability[] reqs) {
		IRequiredCapability candidate = null;
		for (int i = 0; i < reqs.length; i++) {
			if (req.getName().equals(reqs[i].getName())) {
				candidate = reqs[i];
				if (req.getFilter() != null && req.getFilter().equals(candidate.getFilter()))
					break;
			}
		}
		if (candidate == null)
			return null;

		TreeElement element = new TreeElement(req.toString());
		if (req.isGreedy() != candidate.isGreedy())
			element.addChild(req.isGreedy() ? "Profile requirement is greedy while source requirement is not" : "Profile requirement is not greedy while source requirement is");
		if (req.isMultiple() != candidate.isMultiple())
			element.addChild(req.isMultiple() ? "Profile requirement is multiple while source requirement is not" : "Profile requirement is not multiple while source requirement is");
		if (req.isOptional() != candidate.isOptional())
			element.addChild(req.isOptional() ? "Profile requirement is optional while source requirement is not" : "Profile requirement is not optional while source requirement is");
		if ((req.getFilter() == null && candidate.getFilter() != null) || (req.getFilter() != null && !req.getFilter().equals(candidate.getFilter())))
			element.addChild("Requirement filter differs profile: " + req.getFilter() + "  source:" + candidate.getFilter());
		if (!req.getNamespace().equals(candidate.getNamespace()))
			element.addChild("Requirement namespace differs profile: " + req.getNamespace() + "  source:" + candidate.getNamespace());
		if (!req.getRange().equals(candidate.getRange()))
			element.addChild("Requirement version range differs profile: " + req.getRange() + "  source:" + candidate.getRange());

		if (element.getChildren().length == 0)
			return null;
		return element;
	}
}
