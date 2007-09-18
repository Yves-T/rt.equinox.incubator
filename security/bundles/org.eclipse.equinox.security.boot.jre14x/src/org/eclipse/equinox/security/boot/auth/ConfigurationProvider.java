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
package org.eclipse.equinox.security.boot.auth;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.security.auth.login.AppConfigurationEntry;
import javax.security.auth.login.Configuration;

import org.eclipse.equinox.internal.security.boot.MessageAccess;

public class ConfigurationProvider extends Configuration {

//	private static Logger s_logger = Logger.getLogger( ConfigurationProvider.class.getPackage( ).toString( ));
	
	private static Logger s_logger; 
	static {
	  Class cls = ConfigurationProvider.class;
	  Package configProviderPack = cls.getPackage();
	  String packageStr = configProviderPack.toString();
	  s_logger = Logger.getLogger( packageStr);
	}
	
	private static Configuration s_target = null;
	
	public static void setTargetConfiguration( Configuration proxy) {

		if ( s_logger.isLoggable( Level.FINER)) {
			s_logger.entering( ConfigurationProvider.class.toString( ), "setTargetConfiguration", new Object[] {proxy}); //$NON-NLS-1$
		}

		s_target = proxy;
		
		if ( s_logger.isLoggable( Level.FINER)) {
			s_logger.exiting( ConfigurationProvider.class.toString( ), "setTargetConfiguration"); //$NON-NLS-1$
		}
	}
	
	public ConfigurationProvider( ) {
		
		if ( s_logger.isLoggable( Level.FINER)) {
			s_logger.entering( ConfigurationProvider.class.toString( ), "<<constructor>>"); //$NON-NLS-1$
		}
		if ( s_logger.isLoggable( Level.FINER)) {
			s_logger.exiting( ConfigurationProvider.class.toString( ), "<<constructor>>"); //$NON-NLS-1$
		}
	}
	
	/* (non-Javadoc)
	 * @see javax.security.auth.login.Configuration#getAppConfigurationEntry(java.lang.String)
	 */
	public synchronized AppConfigurationEntry[] getAppConfigurationEntry( String name) {
		
		if ( s_logger.isLoggable( Level.FINER)) {
			s_logger.entering( ConfigurationProvider.class.toString( ), "getAppConfigurationEntry", new Object[] {name}); //$NON-NLS-1$
		}
		
		if ( null == s_target) {
			throw new RuntimeException( MessageAccess.getString( "err.configuration.not.set.1")); //$NON-NLS-1$
		}
		
		AppConfigurationEntry[] entries = s_target.getAppConfigurationEntry( name);
		
		if ( s_logger.isLoggable( Level.FINER)) {
			s_logger.exiting( ConfigurationProvider.class.toString( ), "getAppConfigurationEntry", new Object[] {entries}); //$NON-NLS-1$
		}
		
		return entries;
	}

	/* (non-Javadoc)
	 * @see javax.security.auth.login.Configuration#refresh()
	 */
	public synchronized void refresh( ) {
		
		if ( s_logger.isLoggable( Level.FINER)) {
			s_logger.entering( ConfigurationProvider.class.toString( ), "refresh"); //$NON-NLS-1$
		}
		
		s_target.refresh( );

		if ( s_logger.isLoggable( Level.FINER)) {
			s_logger.exiting( ConfigurationProvider.class.toString( ), "refresh"); //$NON-NLS-1$
		}
	}
	
	public synchronized void setAppConfigurationEntry( String providerId, String name, AppConfigurationEntry[] entryList) {
		
		if ( s_logger.isLoggable( Level.FINER)) {
			s_logger.entering( ConfigurationProvider.class.toString( ), "setAppConfigurationEntry"); //$NON-NLS-1$
		}
		
		if ( s_target instanceof ConfigurationProvider) {
			((ConfigurationProvider)s_target).setAppConfigurationEntry( providerId, name, entryList);
		}
		else {
			throw new UnsupportedOperationException( );
		}

		if ( s_logger.isLoggable( Level.FINER)) {
			s_logger.exiting( ConfigurationProvider.class.toString( ), "setAppConfigurationEntry"); //$NON-NLS-1$
		}
	}
	
	public synchronized String[] listAppConfigurationEntries( String providerId) {
		
		if ( s_logger.isLoggable( Level.FINER)) {
			s_logger.entering( ConfigurationProvider.class.toString( ), "listAppConfigurationEntries"); //$NON-NLS-1$
		}
		
		String[] returnValue = null;
		
		if ( s_target instanceof ConfigurationProvider) {
			returnValue = ((ConfigurationProvider)s_target).listAppConfigurationEntries( providerId);
		}
		else {
			throw new UnsupportedOperationException( );
		}

		if ( s_logger.isLoggable( Level.FINER)) {
			s_logger.exiting( ConfigurationProvider.class.toString( ), "listAppConfigurationEntries"); //$NON-NLS-1$
		}
		
		return returnValue;
	}
	
}
