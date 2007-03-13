package org.eclipse.equinox.frameworkadmin.examples;

import java.io.*;
import java.net.*;

/**
 * Tentative implementation.
 * 
 * Current FrameworkAdmin doesn't support in-process launching frameworks.
 * In addition, it doesn't provide a way to control the launched framework.
 * In this implementation, the launched framework is shutdown by using equinox telnet console.
 * If telnet connection is used by other stuff, it cannot be realized.
 * 
 */
public class ShutdownHookForEquinox extends Thread {
	int port;

	ShutdownHookForEquinox(int port) {
		this.port = port;
	}

	public void run() {
		System.out.println("ShutdownHookForEquinox started.");
		Socket mySocket = null;
		PrintWriter out = null;
		BufferedReader in = null;

		try {

			InetAddress address = InetAddress.getByName("localhost");
			System.out.println("trying to connect .....");
			mySocket = new Socket(address, port);
			System.out.println("connected .... ");
			out = new PrintWriter(mySocket.getOutputStream(), true);
			in = new BufferedReader(new InputStreamReader(mySocket.getInputStream()));

			out.println("shutdown");
			while (true) {
				String line = in.readLine();
				if (line != null)
					System.out.println("line:" + line);
				if (line.startsWith("osgi>")) {
					out.println("close");
					break;
				}
			}

		} catch (UnknownHostException e) {
			System.err.println("Don't know about host: ");

			//		System.exit(1);

		} catch (IOException e) {
			System.err.println("Couldn't get I/O for " + "the connection to: ");
			//		System.exit(1);
		} finally {
			if (in != null)
				try {
					in.close();

				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} finally {
					in = null;
				}

			if (out != null) {
				out.close();
				out = null;
			}
		}
		try {
			Main.stop();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("ShutdownHookForEquinox ended.");
	}
}
