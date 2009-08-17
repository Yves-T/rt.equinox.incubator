package org.eclipse.equinox.internal.p2.ui.analysis;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.equinox.internal.p2.ui.analysis.messages"; //$NON-NLS-1$

	public static String Parser_Has_Incompatible_Version;
	public static String Parser_Error_Parsing_Registry;

	public static String AddProfileDialog_Title;

	public static String IUAnalysisPage_AvailableIn;
	public static String IUAnalysisPage_ErrorNoPlanner;
	public static String IUAnalysisPage_LocatingRequiredBy;
	public static String IUAnalysisPage_LocatingSources;
	public static String IUAnalysisPage_None;
	public static String IUAnalysisPage_NoSources;
	public static String IUAnalysisPage_Requirements;
	public static String IUAnalysisPage_RequiredBy;
	public static String IUAnalysisPage_Searching;
	public static String IUAnalysisPage_UnknownSourceProfile;

	public static String IURequirementPage_Artifacts;
	public static String IURequirementPage_Both;
	public static String IURequirementPage_IUs;

	public static String ProfileInstallabilityPage_AllArtifactsAvailable;
	public static String ProfileInstallabilityPage_AllIUsAvailable;
	public static String ProfileInstallabilityPage_ArtifactRepositories;
	public static String ProfileInstallabilityPage_MetadataRepositories;
	public static String ProfileInstallabilityPage_MissingArtifacts;
	public static String ProfileInstallabilityPage_MissingIUs;
	public static String ProfileInstallabilityPage_QueryButton;
	public static String ProfileInstallabilityPage_Results;

	public static String ProfilesView2_AddProfileText;
	public static String ProfilesView2_AddProfileToolTip;

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
