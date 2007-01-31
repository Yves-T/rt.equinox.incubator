/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.equinox.frameworkadmin.examples;

import java.io.*;

public class InputStreamMonitorThread extends Thread {
		static void monitorThreadStart(Process process, InputStreamMonitorThread threadStandard, InputStreamMonitorThread threadError) {
			threadStandard = new InputStreamMonitorThread("S", process.getInputStream());
			threadError = new InputStreamMonitorThread("E", process.getErrorStream());
			threadStandard.start();
			threadError.start();
		}
		static void stopProcess(Process process, InputStreamMonitorThread threadStandard, InputStreamMonitorThread threadError) {
			if (process != null) {
				try {
					process.exitValue();
				} catch (IllegalThreadStateException e) {
					process.destroy();
					process = null;
				}
			}
		
			if (threadStandard != null)
				threadStandard.inactivate();
			if (threadError != null)
				threadError.inactivate();
		}
		private final String name ;
		private BufferedReader br;

		private boolean active = false;
		
		InputStreamMonitorThread(String name,InputStream is) {
			this.name=name;
			br = new BufferedReader(new InputStreamReader(is));
		}

		public void inactivate(){
			active = false;
		}

		public void run() {
			String line = null;
			try {
				active =true;
				while (active == true && (line = br.readLine()) != null ) {
					System.out.println("["+name+"]" + line);
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}