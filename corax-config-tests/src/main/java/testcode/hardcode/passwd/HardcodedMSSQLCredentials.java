package testcode.hardcode.passwd;

import com.microsoft.sqlserver.jdbc.SQLServerDataSource;

// cwe798
public class HardcodedMSSQLCredentials {
    public static void main(SQLServerDataSource ds) throws Exception {        
        ds.setUser("Username"); // $ HardcodedCredentialsApiCall      
        ds.setPassword("password"); // $ HardcodedCredentialsApiCall
        ds.getConnection("Username", null); // $ HardcodedCredentialsApiCall
        ds.getConnection(null, "password"); // $ HardcodedCredentialsApiCall
      }
}