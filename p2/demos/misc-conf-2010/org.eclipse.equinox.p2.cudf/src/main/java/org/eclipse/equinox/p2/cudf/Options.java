package org.eclipse.equinox.p2.cudf;

import java.io.File;

public class Options {

	boolean verbose = false;
	String objective = "p2";
	String timeout = "default";
	boolean explain = false;
	public File input;
	public File output;
	public boolean sort = false;
}