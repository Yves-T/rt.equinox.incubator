package org.eclipse.equinox.examples.httpsecurity;

import java.io.IOException;
import java.net.URL;
import java.util.StringTokenizer;

import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.LoginException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.eclipse.equinox.security.auth.ISecureContext;
import org.eclipse.equinox.security.auth.SecurePlatform;
import org.osgi.service.http.HttpContext;

public class SecureHttpContext implements HttpContext {

	public String getMimeType(String name) {
		// nothing
		return null;
	}

	public URL getResource(String name) {
		// nothing
		return null;
	}

	public boolean handleSecurity(HttpServletRequest request,
			HttpServletResponse response) throws IOException {
		String auth = request.getHeader("Authorization");
		if (auth != null) {
			StringTokenizer tok = new StringTokenizer(auth);
			String authscheme = tok.nextToken();

			/* Check to see if we are using basic authentication.
			 * This sample does Basic Authentication.
			 */
			if (authscheme.equals("Basic")) {
				/* Get the username and password from the http request headers. */
				String base64credentials = tok.nextToken();

				/* Basic Authentication uses Base64 encoding.  Use BASE64 class to
				 * decode username and password.
				 */
				String credentials = new String(Base64.decode(base64credentials
						.getBytes()));
				int colon = credentials.indexOf(':');
				String userid = credentials.substring(0, colon);
				String password = credentials.substring(colon + 1);

				Subject subject = null;;
				try {
					subject = login(request, userid, password);
				} catch (LoginException e) {
					// do nothing
				}

				if (subject != null) {
					request.setAttribute(HttpContext.REMOTE_USER, userid);
					request.setAttribute(HttpContext.AUTHENTICATION_TYPE,
							authscheme);
					request.setAttribute(HttpContext.AUTHORIZATION,
							subject);

					return (true);
				}
			}
		}

		// force a session to be created
		request.getSession(true);
		response.setHeader("WWW-Authenticate",
				"Basic realm=\"Equinox Handle Security Sample\"");

		try {
			response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
		} catch (IOException e) {
			// do nothing
		}

		return (false);
	}

	private Subject login(HttpServletRequest request, final String userid, final String password) throws LoginException {
		HttpSession session = request.getSession(false);
		if (session == null)
			return null;
		ISecureContext context = (ISecureContext) session.getAttribute("securitycontext");
		if (context == null) {
			context = SecurePlatform.createContext("SimpleConfig", Activator.bundle.getEntry("jaas_config.txt"),
				new CallbackHandler() {
					public void handle(Callback[] callbacks)
							throws IOException, UnsupportedCallbackException {
						for (int i = 0; i < callbacks.length; i++) {
							if (callbacks[i] instanceof NameCallback)
								((NameCallback) callbacks[i]).setName(userid);
							else if (callbacks[i] instanceof PasswordCallback)
								((PasswordCallback) callbacks[i])
										.setPassword(password.toCharArray());
							else
								throw new UnsupportedCallbackException(
										callbacks[i]);
						}
					}
				});
			session.setAttribute("securitycontext", context);
		}
		return context.getSubject();
	}

}
