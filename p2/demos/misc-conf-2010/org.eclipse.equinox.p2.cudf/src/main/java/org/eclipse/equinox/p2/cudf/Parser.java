/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: 
 * IBM Corporation - initial implementation and ideas 
 ******************************************************************************/
package org.eclipse.equinox.p2.cudf;

import java.io.*;
import java.util.*;
import org.eclipse.equinox.p2.cudf.metadata.*;
import org.eclipse.equinox.p2.cudf.query.*;
import org.eclipse.equinox.p2.cudf.solver.ProfileChangeRequest;

public class Parser {

	private static final boolean FORCE_QUERY = false; //TO SET TO FALSE FOR COMPETITION
	private static final boolean DEBUG = false; //TO SET TO FALSE FOR COMPETITION
	private static final boolean TIMING = false; //TO SET TO FALSE FOR COMPETITION
	private InstallableUnit currentIU = null;
	private ProfileChangeRequest currentRequest = null;
	private List allIUs = new ArrayList();
	private QueryableArray query = null;
	private List preInstalled = new ArrayList(10000);

	class Tuple {
		String name;
		String version;
		String operator;
		Set extraData;

		Tuple(String line) {
			String[] tuple = new String[3];
			int i = 0;
			for (StringTokenizer iter = new StringTokenizer(line, " \t"); iter.hasMoreTokens(); i++)
				tuple[i] = iter.nextToken();
			name = tuple[0];
			operator = tuple[1];
			version = tuple[2];
		}
	}

	public ProfileChangeRequest parse(File file) {
		try {
			return parse(new FileInputStream(file));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return null;
		}
	}

	public ProfileChangeRequest parse(InputStream stream) {
		long start;
		if (TIMING)
			start = System.currentTimeMillis();
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new InputStreamReader(stream, "UTF-8"));
			String next = reader.readLine();
			while (true) {

				// look-ahead to check for line continuation
				String line = next;
				for (next = reader.readLine(); next != null && next.length() > 0 && next.charAt(0) == ' '; next = reader.readLine()) {
					line = line + next.substring(1);
				}

				// terminating condition of the loop... reached the end of the file
				if (line == null) {
					validateAndAddIU();
					break;
				}

				// end of stanza
				if (line.length() == 0) {
					validateAndAddIU();
					continue;
				}

				// preamble stanza
				if (line.startsWith("#") || line.startsWith("preamble: ") || line.startsWith("property: ") || line.startsWith("univ-checksum: ")) {
					// ignore
				}

				// request stanza
				else if (line.startsWith("request: ")) {
					handleRequest(line);
				} else if (line.startsWith("install: ")) {
					handleInstall(line);
				} else if (line.startsWith("upgrade: ")) {
					handleUpgrade(line);
				} else if (line.startsWith("remove: ")) {
					handleRemove(line);
				}

				// package stanza
				else if (line.startsWith("package: ")) {
					handlePackage(line);
				} else if (line.startsWith("version: ")) {
					handleVersion(line);
				} else if (line.startsWith("installed: ")) {
					handleInstalled(line);
				} else if (line.startsWith("depends: ")) {
					handleDepends(line);
				} else if (line.startsWith("conflicts: ")) {
					handleConflicts(line);
				} else if (line.startsWith("provides: ")) {
					handleProvides(line);
				} else if (line.startsWith("expected: ")) {
					handleExpected(line);
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (reader != null)
				try {
					reader.close();
				} catch (IOException e) {
					// ignore
				}
		}
		if (TIMING)
			System.out.println("# Time to parse:" + (System.currentTimeMillis() - start));
		if (DEBUG)
			for (Iterator iter = allIUs.iterator(); iter.hasNext();)
				debug((InstallableUnit) iter.next());
		if (FORCE_QUERY) {
			if (query == null)
				initializeQueryableArray();
			if (currentRequest == null)
				currentRequest = new ProfileChangeRequest(query);
		}
		debug(currentRequest);
		return currentRequest;
	}

	private void handleExpected(String line) {
		currentRequest.setExpected(Integer.decode(line.substring("expected: ".length()).trim()).intValue());
	}

	/*
	 * Ensure that the current IU that we have been building is validate and if so, then
	 * add it to our collected list of all converted IUs from the file.
	 */
	private void validateAndAddIU() {
		if (currentIU == null)
			return;
		// For a package stanze, the id and version are the only mandatory elements
		if (currentIU.getId() == null)
			throw new IllegalStateException("Malformed \'package\' stanza. No package element found.");
		if (currentIU.getVersion() == null)
			throw new IllegalStateException("Malformed \'package\' stanza. Package " + currentIU.getId() + " does not have a version.");
		if (currentIU.getProvidedCapabilities().length == 0) {
			currentIU.setCapabilities(new IProvidedCapability[] {new ProvidedCapability(currentIU.getId(), new VersionRange(currentIU.getVersion(), true, currentIU.getVersion(), true))});
		}
		allIUs.add(currentIU);
		// reset to be ready for the next stanza
		currentIU = null;
	}

	private void handleInstalled(String line) {
		String value = line.substring("installed: ".length());
		if (value.length() != 0) {
			if (DEBUG)
				if (!Boolean.valueOf(value).booleanValue()) {
					System.err.println("Unexcepted value for installed.");
					return;
				}
			currentIU.setInstalled(true);
			preInstalled.add(new RequiredCapability(currentIU.getId(), new VersionRange(currentIU.getVersion()), true));
		}
	}

	private void handleInstall(String line) {
		line = line.substring("install: ".length());
		List installRequest = createRequires(line, true);
		for (Iterator iterator = installRequest.iterator(); iterator.hasNext();) {
			currentRequest.addInstallableUnit((IRequiredCapability) iterator.next());
		}
		return;
	}

	private void handleRequest(String line) {
		initializeQueryableArray();
		currentRequest = new ProfileChangeRequest(query);
		currentRequest.setPreInstalledIUs(preInstalled);
	}

	private void handleRemove(String line) {
		line = line.substring("remove: ".length());
		List removeRequest = createRequires(line, true);
		for (Iterator iterator = removeRequest.iterator(); iterator.hasNext();) {
			currentRequest.removeInstallableUnit((IRequiredCapability) iterator.next());
		}
		return;
	}

	private void initializeQueryableArray() {
		query = new QueryableArray((InstallableUnit[]) allIUs.toArray(new InstallableUnit[allIUs.size()]));
	}

	private void handleUpgrade(String line) {
		line = line.substring("upgrade: ".length());
		List updateRequest = createRequires(line, true);
		for (Iterator iterator = updateRequest.iterator(); iterator.hasNext();) {
			IRequiredCapability requirement = (IRequiredCapability) iterator.next();
			currentRequest.upgradeInstallableUnit(requirement);

			//Add a requirement forcing uniqueness of the upgraded package in the resulting solution
			currentRequest.upgradeInstallableUnit(new RequiredCapability(requirement.getName(), VersionRange.emptyRange, 1));

			//Add a requirement forcing the solution to be greater or equal to the highest installed version
			requirement = getHighestInstalledVersion(requirement);
			if (requirement != null)
				currentRequest.upgradeInstallableUnit(requirement);
		}
		return;
	}

	private IRequiredCapability getHighestInstalledVersion(IRequiredCapability req) {
		Version highestVersion = null;
		Collector c = query.query(new CapabilityQuery(req), new Collector(), null);
		for (Iterator iterator = c.iterator(); iterator.hasNext();) {
			InstallableUnit candidate = (InstallableUnit) iterator.next();
			if (!candidate.isInstalled())
				continue;
			if (candidate.getId().equals(req.getName())) {
				if (highestVersion == null || candidate.getVersion().getMajor() > highestVersion.getMajor())
					highestVersion = candidate.getVersion();
			} else {
				//Requesting the upgrade of a virtual package
				IProvidedCapability[] prov = candidate.getProvidedCapabilities();
				for (int i = 0; i < prov.length; i++) {
					if (prov[i].getVersion().equals(VersionRange.emptyRange))
						continue;
					if (prov[i].getName().equals(req.getName()) && (highestVersion == null || prov[i].getVersion().getMinimum().getMajor() > highestVersion.getMajor()))
						highestVersion = prov[i].getVersion().getMinimum();
				}
			}
		}
		return new RequiredCapability(req.getName(), new VersionRange(highestVersion, true, Version.maxVersion, true));
	}

	/*
	 * Convert the version string to a version object and set it on the IU
	 */
	private void handleVersion(String line) {
		currentIU.setVersion(new Version(line.substring("version: ".length())));
	}

	private void handleDepends(String line) {
		mergeRequirements(createRequires(line.substring("depends: ".length()), true));
	}

	/*
	 * Conflicts are like depends except NOT'd.
	 */
	private void handleConflicts(String line) {
		List reqs = createRequires(line.substring("conflicts: ".length()), false);
		List conflicts = new ArrayList();
		for (Iterator iter = reqs.iterator(); iter.hasNext();) {
			IRequiredCapability req = (IRequiredCapability) iter.next();
			if (currentIU.getId().equals(req.getName())) {
				currentIU.setSingleton(true);
			} else {
				conflicts.add(new NotRequirement(req));
			}
		}
		mergeRequirements(conflicts);
	}

	/*
	 * Set the given list of requirements on teh current IU. Merge if necessary.
	 */
	private void mergeRequirements(List requirements) {
		if (currentIU.getRequiredCapabilities() != null) {
			IRequiredCapability[] current = currentIU.getRequiredCapabilities();
			for (int i = 0; i < current.length; i++)
				requirements.add(current[i]);
		}
		currentIU.setRequiredCapabilities((IRequiredCapability[]) requirements.toArray(new IRequiredCapability[requirements.size()]));
	}

	/*
	 * Returns a map where the key is the package name and the value is a Tuple.
	 * If there is more than one entry for a particular package, the extra entries are included
	 * in the extraData field of the Tuple. 
	 */
	private Map createPackageList(String line) {
		Map result = new HashMap();
		for (StringTokenizer outer = new StringTokenizer(line, ","); outer.hasMoreTokens();) {
			Tuple tuple = new Tuple(outer.nextToken());
			Tuple existing = (Tuple) result.get(tuple.name);
			if (existing == null) {
				result.put(tuple.name, tuple);
			} else {
				Set others = existing.extraData;
				if (others == null)
					existing.extraData = new HashSet();
				existing.extraData.add(tuple);
			}
		}
		return result;
	}

	private List createRequires(String line, boolean expandNotEquals) {
		ArrayList ands = new ArrayList();
		StringTokenizer s = new StringTokenizer(line, ",");
		while (s.hasMoreElements()) {
			StringTokenizer subTokenizer = new StringTokenizer(s.nextToken(), "|");
			if (subTokenizer.countTokens() == 1) { //This token does not contain a |.
				Object o = createRequire(subTokenizer.nextToken(), expandNotEquals);
				if (o instanceof IRequiredCapability)
					ands.add(o);
				else
					ands.addAll((Collection) o);
				continue;
			}

			IRequiredCapability[] ors = new RequiredCapability[subTokenizer.countTokens()];
			int i = 0;
			while (subTokenizer.hasMoreElements()) {
				ors[i++] = (IRequiredCapability) createRequire(subTokenizer.nextToken(), expandNotEquals);
			}
			ands.add(new ORRequirement(ors));
		}
		return ands;
	}

	private Object createRequire(String nextToken, boolean expandNotEquals) {
		//>, >=, =, <, <=, !=
		StringTokenizer expressionTokens = new StringTokenizer(nextToken.trim(), ">=!<", true);
		int tokenCount = expressionTokens.countTokens();

		if (tokenCount == 1) // a
			return new RequiredCapability(expressionTokens.nextToken().trim(), VersionRange.emptyRange);

		if (tokenCount == 3) // a > 2, a < 2, a = 2
			return new RequiredCapability(expressionTokens.nextToken().trim(), createRange3(expressionTokens.nextToken(), expressionTokens.nextToken()));

		if (tokenCount == 4) { //a >= 2, a <=2, a != 2
			String id = expressionTokens.nextToken().trim();
			String signFirstChar = expressionTokens.nextToken();
			expressionTokens.nextToken();//skip second char of the sign
			String version = expressionTokens.nextToken().trim();
			if (!("!".equals(signFirstChar))) // a >= 2 a <= 2
				return new RequiredCapability(id, createRange4(signFirstChar, version));

			//a != 2
			if (expandNotEquals) {
				return new ORRequirement(new IRequiredCapability[] {new RequiredCapability(id, createRange3("<", version)), new RequiredCapability(id, createRange3(">", version))});
			}
			ArrayList res = new ArrayList(2);
			res.add(new RequiredCapability(id, createRange3("<", version)));
			res.add(new RequiredCapability(id, createRange3(">", version)));
			return res;
		}
		return null;
	}

	private VersionRange createRange3(String sign, String versionAsString) {
		int version = Integer.decode(versionAsString.trim()).intValue();
		sign = sign.trim();
		if (">".equals(sign))
			return new VersionRange(new Version(version), false, Version.maxVersion, false);
		if ("<".equals(sign))
			return new VersionRange(Version.emptyVersion, false, new Version(version), false);
		if ("=".equals(sign))
			return new VersionRange(new Version(version));
		throw new IllegalArgumentException(sign);
	}

	private VersionRange createRange4(String sign, String versionAsString) {
		int version = Integer.decode(versionAsString.trim()).intValue();
		if (">".equals(sign)) //THIS IS FOR >=
			return new VersionRange(new Version(version), true, Version.maxVersion, false);
		if ("<".equals(sign)) //THIS IS FOR <=
			return new VersionRange(Version.emptyVersion, false, new Version(version), true);
		return null;
	}

	private IProvidedCapability createProvidedCapability(Tuple tuple) {
		Set extraData = tuple.extraData;
		// one constraint so simply return the capability
		if (extraData == null)
			return new ProvidedCapability(tuple.name, createVersionRange(tuple.operator, tuple.version));
		// 2 constraints (e.g. a>=1, a<4) so create a real range like a[1,4)
		if (extraData.size() == 1)
			return new ProvidedCapability(tuple.name, createVersionRange(tuple, (Tuple) extraData.iterator().next()));
		// TODO merge more than 2 requirements (a>2, a<4, a>3)
		return new ProvidedCapability(tuple.name, createVersionRange(tuple.operator, tuple.version));
	}

	/*
	 * Create and return a version range object which merges the 2 given versions and operators.
	 * e.g  a>=1 and a<4 becomes a[1,4)
	 */
	private VersionRange createVersionRange(Tuple t1, Tuple t2) {
		Version one = Version.parseVersion(t1.version);
		Version two = Version.parseVersion(t2.version);
		if (one.compareTo(two) < 0) {
			return new VersionRange(one, include(t1.operator), two, include(t2.operator));
		} else if (one.compareTo(two) == 0) {
			return new VersionRange(one, include(t1.operator), one, include(t1.operator));
		} else if (one.compareTo(two) > 0) {
			return new VersionRange(two, include(t2.operator), one, include(t1.operator));
		}
		// should never reach this. avoid compile error.
		return null;
	}

	/*
	 * Helper method for when we are creating version ranges and calculating "includeMin/Max".
	 */
	private boolean include(String operator) {
		return "=".equals(operator) || "<=".equals(operator) || ">=".equals(operator);
	}

	/*
	 * Create and return a version range based on the given operator and number. Note that != is
	 * handled elsewhere.
	 */
	private VersionRange createVersionRange(String operator, String number) {
		if (operator == null || number == null)
			return VersionRange.emptyRange;
		if ("=".equals(operator))
			return new VersionRange('[' + number + ',' + number + ']');
		if ("<".equals(operator))
			return new VersionRange("[0," + number + ')');
		if (">".equals(operator))
			return new VersionRange('(' + number + ',' + Integer.MAX_VALUE + ']');
		if ("<=".equals(operator))
			return new VersionRange("[0," + number + ']');
		if (">=".equals(operator))
			return new VersionRange('[' + number + ',' + Integer.MAX_VALUE + ']');
		return VersionRange.emptyRange;
	}

	// package name matches: "^[a-zA-Z0-9+./@()%-]+$"
	private void handlePackage(String readLine) {
		currentIU = new InstallableUnit();
		currentIU.setId(readLine.substring("package: ".length()));
	}

	private void handleProvides(String line) {
		line = line.substring("provides: ".length());
		Map pkgs = createPackageList(line);
		IProvidedCapability[] providedCapabilities = new ProvidedCapability[pkgs.size() + 1];
		int i = 0;
		for (Iterator iter = pkgs.keySet().iterator(); iter.hasNext();) {
			Tuple tuple = (Tuple) pkgs.get(iter.next());
			providedCapabilities[i++] = createProvidedCapability(tuple);
		}
		providedCapabilities[i++] = new ProvidedCapability(currentIU.getId(), new VersionRange(currentIU.getVersion(), true, currentIU.getVersion(), true));
		currentIU.setCapabilities(providedCapabilities);
	}

	//	// copied from ProfileSynchronizer
	private void debug(ProfileChangeRequest request) {
		if (!DEBUG || request == null)
			return;
		//		System.out.println("\nProfile Change Request:");
		//		InstallableUnit[] toAdd = request.getAddedInstallableUnit();
		//		if (toAdd == null || toAdd.length == 0) {
		//			System.out.println("No installable units to add.");
		//		} else {
		//			for (int i = 0; i < toAdd.length; i++)
		//				System.out.println("Adding IU: " + toAdd[i].getId() + ' ' + toAdd[i].getVersion());
		//		}
		//		Map propsToAdd = request.getInstallableUnitProfilePropertiesToAdd();
		//		if (propsToAdd == null || propsToAdd.isEmpty()) {
		//			System.out.println("No IU properties to add.");
		//		} else {
		//			for (Iterator iter = propsToAdd.keySet().iterator(); iter.hasNext();) {
		//				Object key = iter.next();
		//				System.out.println("Adding IU property: " + key + "->" + propsToAdd.get(key));
		//			}
		//		}
		//
		//		InstallableUnit[] toRemove = request.getRemovedInstallableUnits();
		//		if (toRemove == null || toRemove.length == 0) {
		//			System.out.println("No installable units to remove.");
		//		} else {
		//			for (int i = 0; i < toRemove.length; i++)
		//				System.out.println("Removing IU: " + toRemove[i].getId() + ' ' + toRemove[i].getVersion());
		//		}
		//		Map propsToRemove = request.getInstallableUnitProfilePropertiesToRemove();
		//		if (propsToRemove == null || propsToRemove.isEmpty()) {
		//			System.out.println("No IU properties to remove.");
		//		} else {
		//			for (Iterator iter = propsToRemove.keySet().iterator(); iter.hasNext();) {
		//				Object key = iter.next();
		//				System.out.println("Removing IU property: " + key + "->" + propsToRemove.get(key));
		//			}
		//		}
	}

	// dump info to console
	private void debug(InstallableUnit unit) {
		if (!DEBUG)
			return;
		System.out.println("\nInstallableUnit: " + unit.getId());
		System.out.println("Version: " + unit.getVersion());
		if (unit.isInstalled())
			System.out.println("Installed: true");
		IRequiredCapability[] reqs = unit.getRequiredCapabilities();
		for (int i = 0; i < reqs.length; i++) {
			System.out.println("Requirement: " + reqs[i]);
		}
	}

}
