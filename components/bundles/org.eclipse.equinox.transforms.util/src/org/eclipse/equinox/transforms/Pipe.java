/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.equinox.transforms;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

public class Pipe {

	private InputStream input;
	private PipedInputStream pipedInputStream;
	private PipedOutputStream pipedOutputStream;

	public Pipe(InputStream original) throws IOException {
		this.input = original;
		this.pipedInputStream = new PipedInputStream() {
			private boolean started = false;

			private synchronized void start() throws IOException {
				if (!started) {
					started = true;
					Thread pipeThread = new Thread(new Runnable() {
						public void run() {
							try {
								pipeInput(input, pipedOutputStream);
								pipedOutputStream.close();
							} catch (IOException e) {
								e.printStackTrace();
							}

						};
					});
					pipeThread.start();
				}
			}

			public synchronized int available() throws IOException {
				start();
				return super.available();
			}

			public synchronized int read() throws IOException {
				start();
				int c = super.read();
				return c;
			}

			public int read(byte[] b) throws IOException {
				start();
				return super.read(b);
			}

			public synchronized int read(byte[] b, int off, int len)
					throws IOException {
				start();
				return super.read(b, off, len);
			}

			public synchronized void reset() throws IOException {
				started = false;
				super.reset();
			}
		};
		this.pipedOutputStream = new PipedOutputStream(pipedInputStream);

	}

	public InputStream getPipedInputStream() {
		return pipedInputStream;

	}

	protected void pipeInput(InputStream original, OutputStream result)
			throws IOException {
		byte[] buffer = new byte[2048];
		int len = 0;
		while ((len = original.read(buffer)) != 0) {
			result.write(buffer, 0, len);
		}
	}
}
