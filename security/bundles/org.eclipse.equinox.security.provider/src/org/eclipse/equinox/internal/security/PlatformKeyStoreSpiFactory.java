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
package org.eclipse.equinox.internal.security;

import java.security.KeyStoreSpi;
import java.security.Security;
import java.util.Hashtable;

import org.eclipse.equinox.security.KeyStoreProxy;
import org.eclipse.equinox.security.KeyStoreSpiService;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;


public class PlatformKeyStoreSpiFactory implements KeyStoreProxy.IKeyStoreSpiFactory {

	private static PlatformKeyStoreSpiFactory s_instance = new PlatformKeyStoreSpiFactory( );
	public static PlatformKeyStoreSpiFactory getInstance( ) { return s_instance; }
	
	private static Hashtable serviceMap = new Hashtable( );
	private BundleContext bundleContext;

	private PlatformKeyStoreSpiFactory( ) { };
	
	public KeyStoreSpi newInstance( ) {
		String currentProvider = Security.getProperty( KeyStoreSpiService.SECURITY_PROPERTY_PROVIDER);
		KeyStoreSpiService currentFactory = (KeyStoreSpiService)serviceMap.get( currentProvider);
		if ( null == currentFactory) {
			return null; //THROW
		}
		return currentFactory.newInstance( null);
	}
	
	private static String FILTER_STRING = "(objectclass=" + KeyStoreSpiService.class.getName( ) + ")";
	
	public static void attachServiceListener( BundleContext bundleContext) {
		getInstance( ).attachServiceListenerInternal( bundleContext);
	}
	
	private void attachServiceListenerInternal( BundleContext bundleContext) {
		try {
			this.bundleContext = bundleContext;
			bundleContext.addServiceListener( new InternalServiceListener( ), FILTER_STRING);
		}
		catch ( InvalidSyntaxException e) {
			throw new RuntimeException( "FATAL: Syntax of ServiceListener String is invalid!!!!");
		}
	}
	
	private class InternalServiceListener implements ServiceListener {

		public void serviceChanged( ServiceEvent event) {
			
			switch ( event.getType( )) {
				case ServiceEvent.REGISTERED:
					registerService( event.getServiceReference( ));
					break;
					
				case ServiceEvent.UNREGISTERING:
					unregisterService( event.getServiceReference( ));
					break;
					
				case ServiceEvent.MODIFIED:
					break;
					
				default:
					break;
			}
		}
	};
	
	private void registerService( ServiceReference ref) {
		
		synchronized ( serviceMap ) {
			String name = ref.getBundle( ).getSymbolicName( );
			serviceMap.put( name, (KeyStoreSpiService)bundleContext.getService( ref));
		}
	}
	
	private void unregisterService( ServiceReference ref) {
		
		synchronized ( serviceMap ) {
			String name = ref.getBundle( ).getSymbolicName( );
			serviceMap.remove( name);
		}
	}
}
