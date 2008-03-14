package org.eclipse.equinox.security.sample.engine;

import java.io.IOException;
import java.net.URL;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.Enumeration;
import org.eclipse.osgi.service.security.TrustEngine;

public class MyTrustEngine extends TrustEngine {

	private String engineName;
	private URL storeURL;
	private char[] storePass;
	private String storeType;
	private KeyStore myKeyStore;

	public MyTrustEngine(URL storeURL, char[] password_default, String type_default, String engineName) {
		this.storeURL = storeURL;
		storePass = password_default;
		storeType = type_default;
		this.engineName = engineName;
	}

	protected String doAddTrustAnchor(Certificate anchor, String alias) throws IOException, GeneralSecurityException {
		throw new UnsupportedOperationException();
	}

	protected void doRemoveTrustAnchor(Certificate anchor) throws IOException, GeneralSecurityException {
		throw new UnsupportedOperationException();
	}

	protected void doRemoveTrustAnchor(String alias) throws IOException, GeneralSecurityException {
		throw new UnsupportedOperationException();
	}

	public Certificate findTrustAnchor(Certificate[] certChain) throws IOException {
		// assume the certchain was verified...
		try {
			KeyStore store = getMyKeyStore();
			for (int i = 0; i < certChain.length; i++) {
				synchronized (store) {
					String alias = store.getCertificateAlias(certChain[i]);
					if (alias != null) {
						return store.getCertificate(alias);
					}
				}
			}
		} catch (KeyStoreException e) {
			throw new IOException(e.getMessage());
		} catch (GeneralSecurityException e) {
			return null;
		}
		return null;
	}

	public String[] getAliases() throws IOException, GeneralSecurityException {
		ArrayList returnList = new ArrayList();
		try {
			KeyStore store = getMyKeyStore();
			synchronized (store) {
				for (Enumeration aliases = store.aliases(); aliases.hasMoreElements();) {
					String currentAlias = (String) aliases.nextElement();
					if (store.isCertificateEntry(currentAlias)) {
						returnList.add(currentAlias);
					}
				}
			}
		} catch (KeyStoreException ke) {
			throw new CertificateException(ke.getMessage());
		}
		return (String[]) returnList.toArray(new String[] {});
	}

	public String getName() {
		return engineName;
	}

	public Certificate getTrustAnchor(String alias) throws IOException, GeneralSecurityException {

		if (alias == null) {
			throw new IllegalArgumentException("Alias must be specified"); //$NON-NLS-1$
		}

		try {
			KeyStore store = getMyKeyStore();
			synchronized (store) {
				return store.getCertificate(alias);
			}
		} catch (KeyStoreException ke) {
			throw new CertificateException(ke.getMessage());
		}
	}

	public boolean isReadOnly() {
		return true;
	}

	private KeyStore getMyKeyStore() throws IOException, GeneralSecurityException {
		if (myKeyStore == null) {
			myKeyStore = KeyStore.getInstance(storeType);
			myKeyStore.load(storeURL.openStream(), storePass);
		}
		return myKeyStore;
	}
}
