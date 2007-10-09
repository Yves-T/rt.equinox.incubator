/*******************************************************************************
 * Copyright (c) 2005, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.equinox.security.boot;

import java.security.*;
import java.util.List;
import java.util.Map;

/**
 * The description of a security service. It encapsulates the properties of a service and contains a 
 * factory method to obtain new implementation instances of this service.
 */
public interface IProviderService {
	/**
	 * Returns the provider that offers this service
	 * @return the provider that offers this service
	 */
	public Provider getProvider();

	/**
	 * Returns the type of this service
	 * @return the type of this service
	 */
	public String getType();

	/**
	 *  Returns the algorithm name of this service
	 * @return the algorithm name of this service
	 */
	public String getAlgorithm();

	/**
	 * Returns the name of the class implementing this service
	 * @return the name of the class implementing this service
	 */
	public String getClassName();

	/**
	 * Returns a {@link List} of aliases or <code>null</code> if algorithm has no aliases
	 * @return a {@link List} of aliases or <code>null</code> if algorithm has no aliases
	 */
	public List getAliases();

	/**
	 * Returns a {@link Map} of attributes or <code>null</code> if this implementation has no attributes
	 * @return a {@link Map} of attributes or <code>null</code> if this implementation has no attributes
	 */
	public Map getAttributes();

	/**
	 * Return a new instance of the implementation described by this service.
	 * <p>
	 * This method will be called by the equinox provider registry and is not intended to 
	 * be called by clients.
	 * </p>
	 * @param parameter the value to pass to the constructor,  or null if this type of service does not use a constructor parameter.
	 * @return a new implementation of this service
	 *
	 * @throws InvalidParameterException if the value of parameter is invalid for this type of service.
	 * @throws NoSuchAlgorithmException if instantiation failed for any other reason.
	 */
	public Object newInstance(Object parameter) throws NoSuchAlgorithmException;

	/**
	 * Test whether this Service can use the specified parameter.  Returns false if this service cannot use the parameter. Returns
	 * true if this service can use the parameter, if a fast test is infeasible, or if the status is unknown.
	 * <p>
	 * This method will be called by the equinox provider registry and is not intended to 
	 * be called by clients.
	 * </p>
	 * @param parameter the parameter to test
	 *
	 * @return false if this this service cannot use the specified  parameter; true if it can possibly use the parameter
	 */
	public boolean supportsParameter(Object parameter);

	/**
	 * Return a String representation of this service.
	 *
	 * @return a String representation of this service.
	 */
	public String toString();

}
