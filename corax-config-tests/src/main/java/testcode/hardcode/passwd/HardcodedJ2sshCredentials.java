package testcode.hardcode.passwd;

import com.sshtools.j2ssh.authentication.PasswordAuthenticationClient;
import com.sshtools.j2ssh.authentication.SshAuthenticationClient;

// cwe798
public class HardcodedJ2sshCredentials {
  public static void main(SshAuthenticationClient client1, PasswordAuthenticationClient client2) {
    // BAD: Hardcoded credentials used for the session username and/or password.
    client1.setUsername("Username"); // $ HardcodedCredentialsApiCall $ HardcodedCredentialsSourceCall
    client2.setUsername("Username"); // $ HardcodedCredentialsApiCall $ HardcodedCredentialsSourceCall
    client2.setPassword("password"); // $ HardcodedCredentialsApiCall $ HardcodedCredentialsSourceCall
  }
}