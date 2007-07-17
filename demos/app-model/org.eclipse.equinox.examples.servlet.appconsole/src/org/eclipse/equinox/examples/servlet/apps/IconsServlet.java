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
package org.eclipse.equinox.examples.servlet.apps;

import java.io.*;
import java.net.URL;
import javax.servlet.ServletException;
import javax.servlet.http.*;

public class IconsServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		doPost(req, resp);
	}

	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String iconPath = req.getParameter("url");
		if (iconPath == null)
			return;
		URL iconURL = new URL(iconPath);
		InputStream in = iconURL.openStream();
		OutputStream out = null;
		try {
			out = resp.getOutputStream();
			byte[] buffer = new byte[1024];
			for (int numRead = in.read(buffer); numRead > 0; numRead = in.read(buffer))
				out.write(buffer, 0, numRead);
		} finally {
			try {
				in.close();
			} catch (IOException e) {
				// nothing;
			}
			if (out != null)
				try {
					out.close();
				} catch (IOException e) {
					// nothing;
				}
		}

	}

}
