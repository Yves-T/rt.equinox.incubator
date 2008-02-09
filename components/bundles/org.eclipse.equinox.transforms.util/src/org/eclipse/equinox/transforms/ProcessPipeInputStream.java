/*******************************************************************************
 * Copyright (c) 2006, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.equinox.transforms;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public abstract class ProcessPipeInputStream extends InputStream {
	private Process process = null;
	private InputStream original;

	public ProcessPipeInputStream(InputStream original) {
		this.original = original;
	}

	protected abstract String getCommandString();

	protected String[] getEnvironment() {
		return null;
	}

	protected File getWorkingDirectory() {
		return null;
	}

	public int read() throws IOException {
		if (process == null) {

			process = Runtime.getRuntime().exec(getCommandString(),
					getEnvironment(), getWorkingDirectory());

			Thread thread = new Thread(new Runnable() {

				public void run() {
					byte[] buffer = new byte[2048];
					int len = 0;
					try {
						while ((len = original.read(buffer)) > 0) {
							process.getOutputStream().write(buffer, 0, len);
						}
						process.getOutputStream().close();
					} catch (IOException e) {
						e.printStackTrace();
						process.destroy();
					}

				}
			});
			thread.start();

		}
		return process.getInputStream().read();
	}

	public synchronized void reset() throws IOException {
		if (process != null) {
			process.destroy();
			process = null;
		}
		super.reset();
	}
}
