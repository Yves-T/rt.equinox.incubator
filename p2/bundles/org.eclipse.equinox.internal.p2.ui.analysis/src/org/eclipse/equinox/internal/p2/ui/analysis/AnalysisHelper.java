package org.eclipse.equinox.internal.p2.ui.analysis;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.equinox.internal.p2.director.QueryableArray;
import org.eclipse.equinox.internal.p2.director.Slicer;
import org.eclipse.equinox.internal.p2.metadata.IRequiredCapability;
import org.eclipse.equinox.internal.p2.metadata.repository.CompositeMetadataRepository;
import org.eclipse.equinox.internal.p2.ui.analysis.viewers.TreeElement;
import org.eclipse.equinox.p2.engine.IProfile;
import org.eclipse.equinox.p2.engine.query.IUProfilePropertyQuery;
import org.eclipse.equinox.p2.metadata.IInstallableUnit;
import org.eclipse.equinox.p2.metadata.IRequirement;
import org.eclipse.equinox.p2.query.IQuery;
import org.eclipse.equinox.p2.query.IQueryable;
import org.eclipse.equinox.p2.query.QueryUtil;
import org.eclipse.equinox.p2.repository.IRepositoryManager;
import org.eclipse.equinox.p2.repository.artifact.IArtifactRepositoryManager;
import org.eclipse.equinox.p2.repository.metadata.IMetadataRepository;
import org.eclipse.equinox.p2.repository.metadata.IMetadataRepositoryManager;

public class AnalysisHelper {

	// Get the root IUs of a profile
	public static IInstallableUnit[] getProfileRoots(IProfile profile, IProgressMonitor monitor) {
		return (IInstallableUnit[]) profile.query(new IUProfilePropertyQuery(IProfile.PROP_PROFILE_ROOT_IU, Boolean.TRUE.toString()), monitor).toArray(IInstallableUnit.class);
	}

	// Determine if the profile is valid (ie, all dependencies met)
	public static IStatus checkProfileValidity(IProfile profile, Collection<IInstallableUnit> collector, IProgressMonitor monitor) {
		return checkValidity(profile, new IInstallableUnit[0], collector, monitor);
	}

	// Check the validity of a profile when some IUs are removed.  (Can be none)
	public static IStatus checkValidity(IProfile profile, IInstallableUnit[] iusToRemove, Collection<IInstallableUnit> collector, IProgressMonitor monitor) {
		try {
			monitor.beginTask("Checking profile", 70);
			Collection<IInstallableUnit> profileRootIUs = new ArrayList<IInstallableUnit>(Arrays.asList(getProfileRoots(profile, new SubProgressMonitor(monitor, 10))));
			Collection<IInstallableUnit> profileIUs = new ArrayList<IInstallableUnit>(getProfileIUs(profile, new SubProgressMonitor(monitor, 10)));

			// Remove IUs from those collected from the profile
			for (int i = 0; i < iusToRemove.length; i++) {
				profileRootIUs.remove(iusToRemove[i]);
				profileIUs.remove(iusToRemove[i]);
			}

			Slicer slicer = new Slicer(new QueryableArray(profileIUs.toArray(new IInstallableUnit[profileIUs.size()])), new Hashtable(profile.getProperties()), true);
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
					if (children[i].getMessage().contains(iu.toString()) && wouldBeSatisified(iu, iusToRemove))
						if (!collector.add(iu))
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
	public static IRequirement[] getMissingRequirements(IInstallableUnit[] roots, IQueryable<IInstallableUnit> queryable, Map<String, String> properties, IProgressMonitor monitor) {
		AnalysisSlicer aslicer = new AnalysisSlicer(queryable, properties, true);
		aslicer.slice(roots, monitor);
		Collection<IRequirement> req = aslicer.getMissingRequirements();

		return req.toArray(new IRequirement[req.size()]);
	}

	/*
	 * Find IUs which satisfy the requirements
	 */
	public static IInstallableUnit[] satisfyRequirements(IRequirement[] req, Map<String, String> properties, IProgressMonitor monitor) {
		return getMetadataRepository().query(createQuery(req), monitor).toArray(IInstallableUnit.class);
	}

	private static IQuery<IInstallableUnit> createQuery(IRequirement[] requirements) {
		List<IQuery<IInstallableUnit>> queries = new ArrayList<IQuery<IInstallableUnit>>();
		for (IRequirement req : requirements)
			queries.add(QueryUtil.createMatchQuery(req.getMatches(), new Object[] {}));
		return QueryUtil.createCompoundQuery(queries, false);
	}

	//  Remove the specified IUs from queryable
	public static IInstallableUnit[] subtract(IQueryable<IInstallableUnit> queryable, IInstallableUnit[] toRemove) {
		Set<IInstallableUnit> set = queryable.query(QueryUtil.ALL_UNITS, new NullProgressMonitor()).toSet();

		for (int i = 0; i < toRemove.length; i++)
			set.remove(toRemove[i]);

		return set.toArray(new IInstallableUnit[set.size()]);
	}

	// Determine if any IU requirement is be satisfied by one of the removed Installable units
	private static boolean wouldBeSatisified(IInstallableUnit iu, IInstallableUnit[] removed) {
		if (removed.length == 0)
			return true;

		for (IRequirement req : iu.getRequirements())
			for (int i = 0; i < removed.length; i++)
				if (removed[i].satisfies(req))
					return true;
		return false;
	}

	// Get all the installable units in a profile
	private static Collection<IInstallableUnit> getProfileIUs(IProfile profile, IProgressMonitor monitor) {
		return profile.query(QueryUtil.ALL_UNITS, monitor).toSet();
	}

	// Get a composite metadata repository with children: all non-system repositories
	public static IMetadataRepository getMetadataRepository() {
		IMetadataRepositoryManager mgr = getMetadataRepositoryManager();
		URI[] addresses = mgr.getKnownRepositories(IRepositoryManager.REPOSITORIES_NON_SYSTEM);
		CompositeMetadataRepository repo = CompositeMetadataRepository.createMemoryComposite(AnalysisActivator.getDefault().getAgent());
		for (int i = 0; i < addresses.length; i++)
			repo.addChild(addresses[i]);

		return repo;
	}

	public static TreeElement diff(IInstallableUnit iu1, IInstallableUnit iu2) {
		TreeElement iuElement = new TreeElement(iu1.toString());

		for (IRequirement req : iu1.getRequirements()) {
			TreeElement element = requirementDifference((IRequiredCapability) req, iu2.getRequirements());
			if (element != null)
				iuElement.addChild(element);
		}

		if (iuElement.getChildren().length == 0)
			return null;
		return iuElement;
	}

	private static TreeElement requirementDifference(IRequiredCapability req, Collection<IRequirement> reqs) {
		IRequiredCapability candidate = null;
		for (IRequirement aRequirement : reqs) {
			if (req.getName().equals(((IRequiredCapability) aRequirement).getName())) {
				candidate = (IRequiredCapability) aRequirement;
				if (req.getFilter() != null && req.getFilter().equals(candidate.getFilter()))
					break;
			}
		}
		//		for (int i = 0; i < reqs.length; i++) {
		//			if (req.getName().equals(reqs[i].getName())) {
		//				candidate = reqs[i];
		//				if (req.getFilter() != null && req.getFilter().equals(candidate.getFilter()))
		//					break;
		//			}
		//		}
		if (candidate == null)
			return null;

		TreeElement element = new TreeElement(req.toString());
		if (req.isGreedy() != candidate.isGreedy())
			element.addChild(req.isGreedy() ? "Profile requirement is greedy while source requirement is not" : "Profile requirement is not greedy while source requirement is");
		// TODO Determine replacements
		//		if (req.isMultiple() != candidate.isMultiple())
		//			element.addChild(req.isMultiple() ? "Profile requirement is multiple while source requirement is not" : "Profile requirement is not multiple while source requirement is");
		//		if (req.isOptional() != candidate.isOptional())
		//			element.addChild(req.isOptional() ? "Profile requirement is optional while source requirement is not" : "Profile requirement is not optional while source requirement is");
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

	public static IArtifactRepositoryManager getArtifactRepositoryManager() {
		return (IArtifactRepositoryManager) AnalysisActivator.getDefault().getAgent().getService(IArtifactRepositoryManager.SERVICE_NAME);
	}

	public static IMetadataRepositoryManager getMetadataRepositoryManager() {
		return (IMetadataRepositoryManager) AnalysisActivator.getDefault().getAgent().getService(IMetadataRepositoryManager.SERVICE_NAME);
	}
}
