/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.equinox.transforms.replace;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public class ReplaceTransformer {

	public InputStream getInputStream(InputStream inputStream,
			final URL transformerUrl) throws IOException {

		return transformerUrl.openStream();
	}
	
}
