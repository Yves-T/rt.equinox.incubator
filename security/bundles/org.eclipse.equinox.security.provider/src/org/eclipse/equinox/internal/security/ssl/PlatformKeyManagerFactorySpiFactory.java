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
package org.eclipse.equinox.internal.security.ssl;

import java.security.Security;
import java.util.Hashtable;

import javax.net.ssl.KeyManagerFactorySpi;

import org.eclipse.equinox.security.boot.ssl.KeyManagerFactoryProxy;
import org.eclipse.equinox.security.ssl.KeyManagerFactorySpiService;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;


public class PlatformKeyManagerFactorySpiFactory implements KeyManagerFactoryProxy.IKeyManagerFactorySpiFactory{

	private static PlatformKeyManagerFactorySpiFactory s_instance = new PlatformKeyManagerFactorySpiFactory( );
	public static PlatformKeyManagerFactorySpiFactory getInstance( ) { return s_instance; }
		
	private static Hashtable serviceMap = new Hashtable( );
	private BundleContext bundleContext;

	private PlatformKeyManagerFactorySpiFactory( ) { };
	
	public KeyManagerFactorySpi newInstance( ) {
		KeyManagerFactorySpiService currentFactory = (KeyManagerFactorySpiService)serviceMap.get( getCurrentKMFImpl() );
		if ( null == currentFactory) {
			return new NullKeyManagerFactory( );
		}
		return currentFactory.newInstance( null);
	}

	private static String FILTER_STRING = "(objectclass=" + KeyManagerFactorySpiService.class.getName( ) + ")";
	
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
			serviceMap.put( name, (KeyManagerFactorySpiService)bundleContext.getService( ref));
		}
	}
	
	private void unregisterService( ServiceReference ref) {
		
		synchronized ( serviceMap ) {
			String name = ref.getBundle( ).getSymbolicName( );
			serviceMap.remove( name);
		}
	}
	
	private String getCurrentKMFImpl() {
		return Security.getProperty( KeyManagerFactorySpiService.SECURITY_PROPERTY_PROVIDER);
	}
}
