package environment;

import java.net.URI;

public class Location {
	private final URI location;

	public Location(URI location) {
		this.location = location;
		System.out.println("Location " + location);
	}

	public URI toURI() {
		return location;
	}
}
