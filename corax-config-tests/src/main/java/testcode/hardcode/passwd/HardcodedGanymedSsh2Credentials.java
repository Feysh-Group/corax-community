package testcode.hardcode.passwd;

import ch.ethz.ssh2.Connection;

import java.io.IOException;

// cwe798
public class HardcodedGanymedSsh2Credentials {
	public static void main(Connection conn) {
    // BAD: Hardcoded credentials used for the session username and/or password.
    try {
		  conn.authenticateWithPassword("username", "password"); // $ HardcodedCredentialsApiCall $ HardcodedCredentialsSourceCall
    } catch(IOException e) { }
	}
}