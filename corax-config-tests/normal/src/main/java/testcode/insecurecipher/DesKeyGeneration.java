package testcode.insecurecipher;

import javax.crypto.KeyGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Provider;

public class DesKeyGeneration {

    public static void weakDesKeyGenerator() throws NoSuchAlgorithmException, NoSuchProviderException {
        KeyGenerator.getInstance("DES");  // $DesUsage
        KeyGenerator.getInstance("des");  // $DesUsage
        KeyGenerator.getInstance("DES",new DummyProvider());  // $DesUsage
        KeyGenerator.getInstance("des",new DummyProvider());  // $DesUsage
        KeyGenerator.getInstance("DES", "SUN");  // $DesUsage
        KeyGenerator.getInstance("des", "SUN");  // $DesUsage
        KeyGenerator.getInstance("DESede");  // $TdesUsage
        KeyGenerator.getInstance("DESEDE");  // $TdesUsage
        KeyGenerator.getInstance("DESede",new DummyProvider());  // $TdesUsage
        KeyGenerator.getInstance("DESede", "SUN");  // $TdesUsage
        KeyGenerator.getInstance("AES"); //OK!
        KeyGenerator.getInstance("RSA"); //OK!
    }

    static class DummyProvider extends Provider {

        protected DummyProvider() {
            super("dummy", 1.0, "");
        }
    }
}
