package org.eclipse.equinox.internal.p2.ui.analysis.model;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import javax.xml.parsers.ParserConfigurationException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.equinox.internal.p2.core.helpers.LogHelper;
import org.eclipse.equinox.internal.p2.engine.Profile;
import org.eclipse.equinox.internal.p2.engine.ProfileParser;
import org.eclipse.equinox.internal.p2.engine.ProfileWriter;
import org.eclipse.equinox.internal.p2.engine.ProfileXMLConstants;
import org.eclipse.equinox.internal.p2.ui.analysis.AnalysisActivator;
import org.eclipse.equinox.internal.p2.ui.analysis.Messages;
import org.eclipse.equinox.internal.provisional.p2.core.Version;
import org.eclipse.equinox.internal.provisional.p2.engine.IProfile;
import org.eclipse.equinox.internal.provisional.p2.metadata.IInstallableUnit;
import org.eclipse.equinox.internal.provisional.p2.query.Collector;
import org.eclipse.equinox.internal.provisional.p2.query.IQueryable;
import org.eclipse.equinox.internal.provisional.p2.query.Query;
import org.eclipse.osgi.util.NLS;
import org.osgi.framework.BundleContext;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class ForeignProfile implements IQueryable, IProfile {
	private IProfile profile;
	private File profileFile;

	public ForeignProfile(File profileFile) {
		Parser parser = new Parser(AnalysisActivator.getDefault().getContext(), null);
		try {
			parser.parse(profileFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
		Map profiles = parser.getProfileMap();
		if (profiles.size() == 0)
			return;
		Iterator iter = profiles.keySet().iterator();

		profile = (IProfile) profiles.get(iter.next());
		this.profileFile = profileFile;
	}

	public File getProfileFile() {
		return profileFile;
	}

	public Collector query(Query query, Collector collector, IProgressMonitor monitor) {
		return profile.query(query, collector, monitor);
	}

	public void addInstallableUnit(IInstallableUnit iu) {
		if (profile instanceof Profile)
			((Profile) profile).addInstallableUnit(iu);
		else
			throw new UnsupportedOperationException("Current profile does not support adding InstallableUnits");
	}

	public void removeInstallableUnit(IInstallableUnit iu) {
		if (profile instanceof Profile)
			((Profile) profile).removeInstallableUnit(iu);
		else
			throw new UnsupportedOperationException("Current profile does not support removing InstallableUnits");
	}

	public String removeInstallableUnitProperty(IInstallableUnit iu, String key) {
		if (profile instanceof Profile)
			return ((Profile) profile).removeInstallableUnitProperty(iu, key);
		throw new UnsupportedOperationException("Current profile does not support removing InstallableUnit properties");
	}

	public String setInstallableUnitProperty(IInstallableUnit iu, String key, String value) {
		if (profile instanceof Profile)
			return ((Profile) profile).setInstallableUnitProperty(iu, key, value);
		throw new UnsupportedOperationException("Current profile does not support setting InstallableUnit properties");
	}

	/*
	 * 	Modified from org.eclipse.equinox.internal.p2.engine.SimpleProfileRegistry
	 */
	public class Parser extends ProfileParser {
		private final Map profileHandlers = new HashMap();

		public Parser(BundleContext context, String bundleId) {
			super(context, bundleId);
		}

		public void parse(File file) throws IOException {
			parse(new BufferedInputStream(new FileInputStream(file)));
		}

		public synchronized void parse(InputStream stream) throws IOException {
			try {
				// TODO: currently not caching the parser since we make no assumptions
				//		 or restrictions on concurrent parsing
				getParser();
				ProfileHandler profileHandler = new ProfileHandler();
				xmlReader.setContentHandler(new ProfileDocHandler(PROFILE_ELEMENT, profileHandler));
				xmlReader.parse(new InputSource(stream));
				profileHandlers.put(profileHandler.getProfileId(), profileHandler);
			} catch (SAXException e) {
				throw new IOException(e.getMessage());
			} catch (ParserConfigurationException e) {
				throw new IOException(e.getMessage());
			} finally {
				stream.close();
			}
		}

		protected Object getRootObject() {
			return this;
		}

		public Map getProfileMap() {
			Map profileMap = new HashMap();
			for (Iterator it = profileHandlers.keySet().iterator(); it.hasNext();) {
				String profileId = (String) it.next();
				addProfile(profileId, profileMap);
			}
			return profileMap;
		}

		private void addProfile(String profileId, Map profileMap) {
			if (profileMap.containsKey(profileId))
				return;

			ProfileHandler profileHandler = (ProfileHandler) profileHandlers.get(profileId);
			Profile parentProfile = null;

			String parentId = profileHandler.getParentId();
			if (parentId != null) {
				addProfile(parentId, profileMap);
				parentProfile = (Profile) profileMap.get(parentId);
			}

			Profile profile = new Profile(profileId, parentProfile, profileHandler.getProperties());
			profile.setTimestamp(profileHandler.getTimestamp());

			IInstallableUnit[] ius = profileHandler.getInstallableUnits();
			if (ius != null) {
				for (int i = 0; i < ius.length; i++) {
					IInstallableUnit iu = ius[i];
					profile.addInstallableUnit(iu);
					Map iuProperties = profileHandler.getIUProperties(iu);
					if (iuProperties != null) {
						for (Iterator it = iuProperties.entrySet().iterator(); it.hasNext();) {
							Entry entry = (Entry) it.next();
							String key = (String) entry.getKey();
							String value = (String) entry.getValue();
							profile.setInstallableUnitProperty(iu, key, value);
						}
					}
				}
			}
			profile.setChanged(false);
			profileMap.put(profileId, profile);
		}

		private final class ProfileDocHandler extends DocHandler {

			public ProfileDocHandler(String rootName, RootHandler rootHandler) {
				super(rootName, rootHandler);
			}

			public void processingInstruction(String target, String data) throws SAXException {
				if (ProfileXMLConstants.PROFILE_TARGET.equals(target)) {
					Version repositoryVersion = extractPIVersion(target, data);
					if (!ProfileXMLConstants.XML_TOLERANCE.isIncluded(repositoryVersion)) {
						throw new SAXException(NLS.bind(Messages.Parser_Has_Incompatible_Version, repositoryVersion, ProfileXMLConstants.XML_TOLERANCE));
					}
				}
			}
		}

		protected String getErrorMessage() {
			return Messages.Parser_Error_Parsing_Registry;
		}

		public String toString() {
			// TODO:
			return null;
		}

	}

	/*
	 * 	Copied from org.eclipse.equinox.internal.p2.engine.SimpleProfileRegistry
	 */
	static class Writer extends ProfileWriter {

		public Writer(OutputStream output) throws IOException {
			super(output, new ProcessingInstruction[] {ProcessingInstruction.makeTargetVersionInstruction(PROFILE_TARGET, ProfileXMLConstants.CURRENT_VERSION)});
		}
	}

	public boolean saveProfile() {
		long previousTimestamp = 0;
		if (profile instanceof Profile) {
			previousTimestamp = ((Profile) profile).getTimestamp();
			((Profile) profile).setTimestamp(System.currentTimeMillis());
			((Profile) profile).setChanged(false);
		}
		OutputStream os = null;
		try {
			os = new BufferedOutputStream(new FileOutputStream(profileFile));
			Writer writer = new Writer(os);
			writer.writeProfile(profile);
			return true;
		} catch (IOException e) {
			if (profile instanceof Profile)
				((Profile) profile).setTimestamp(previousTimestamp);
			LogHelper.log(new Status(IStatus.ERROR, AnalysisActivator.PLUGIN_ID, NLS.bind("Error persisting profile {0}.", profile.getProfileId()), e));
			return false;
		} finally {
			try {
				if (os != null)
					os.close();
			} catch (IOException e) {
				// ignore
			}
		}
	}

	public Collector available(Query query, Collector collector, IProgressMonitor monitor) {
		return profile.available(query, collector, monitor);
	}

	public Map getInstallableUnitProperties(IInstallableUnit iu) {
		return profile.getInstallableUnitProperties(iu);
	}

	public String getInstallableUnitProperty(IInstallableUnit iu, String key) {
		return profile.getInstallableUnitProperty(iu, key);
	}

	public Map getLocalProperties() {
		return profile.getLocalProperties();
	}

	public String getLocalProperty(String key) {
		return profile.getLocalProperty(key);
	}

	public IProfile getParentProfile() {
		return profile.getParentProfile();
	}

	public String getProfileId() {
		return profile.getProfileId() + "[" + profileFile + "]"; //$NON-NLS-1$//$NON-NLS-2$
	}

	public Map getProperties() {
		return profile.getProperties();
	}

	public String getProperty(String key) {
		return profile.getProperty(key);
	}

	public String[] getSubProfileIds() {
		return profile.getSubProfileIds();
	}

	public long getTimestamp() {
		return profile.getTimestamp();
	}

	public boolean hasSubProfiles() {
		return profile.hasSubProfiles();
	}

	public boolean isRootProfile() {
		return profile.isRootProfile();
	}
}
