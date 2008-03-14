package org.eclipse.equinox.examples.httpsecurity;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServlet;

import org.osgi.service.http.HttpContext;

public class SecureServlet extends HttpServlet {

	public void service(ServletRequest req, ServletResponse res)
			throws ServletException, IOException {
		res.getOutputStream().println("Hello Security: " + req.getAttribute(HttpContext.AUTHORIZATION));
	}
	
}
