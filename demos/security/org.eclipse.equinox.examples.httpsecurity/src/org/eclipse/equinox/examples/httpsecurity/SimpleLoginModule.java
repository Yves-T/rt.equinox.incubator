package org.eclipse.equinox.examples.httpsecurity;

import java.io.IOException;
import java.security.Principal;
import java.util.Map;

import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;

public class SimpleLoginModule implements LoginModule {

	private CallbackHandler handler;
	private Subject subject;
	private Principal user;

	public void initialize(Subject subject, final CallbackHandler handler,
			Map arg2, Map arg3) {
		this.handler = handler;
		this.subject = subject;
	}

	public boolean login() throws LoginException {
		final Callback[] callbacks = { new NameCallback("Username"),
				new PasswordCallback("Password", false) };

		try {
			handler.handle(callbacks);
		} catch (IOException e) {
			throw new LoginException(e.getMessage());
		} catch (UnsupportedCallbackException e) {
			throw new LoginException(e.getMessage());
		}

		if ("user".equals(((NameCallback) callbacks[0]).getName())
				&& "password".equals(new String(((PasswordCallback) callbacks[1])
						.getPassword()))) {
			user = new Principal() {
				public boolean equals(Object obj) {
					if (!(obj instanceof Principal))
						return false;
					return getName().equals(((Principal) obj).getName());
				}

				public int hashCode() {
					return getName().hashCode();
				}

				public String toString() {
					return getName().toString();
				}

				public String getName() {
					// TODO Auto-generated method stub
					return ((NameCallback) callbacks[0]).getName();
				}
			};
			return true;
		}else
			throw new LoginException("Login failed");

	}

	public boolean logout() throws LoginException {
		return true;
	}

	public boolean abort() throws LoginException {
		return true;
	}

	public boolean commit() throws LoginException {
		subject.getPrincipals().add(user);
		return true;
	}
}
