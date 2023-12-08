package testcode.insecurecipher;

import javax.crypto.Cipher;
import java.security.Provider;

/**
 * Use for test the identification of DES ciphers, AES/ECB.
 */
public class BlockCipherList {

    public static void main(String[] args) throws Exception {

        //Note : Not a realistic code sample (no encryption occurs)

        Cipher.getInstance("AES/CBC/NoPadding");
        Cipher.getInstance("AES/CBC/PKCS5Padding", "SunJCE");
        Cipher.getInstance("AES/ECB/NoPadding", "IBMJCE");  // $EcbMode
        Cipher.getInstance("AES/ECB/PKCS5Padding", new DummyProvider());  // $EcbMode
        Cipher.getInstance("DES/CBC/NoPadding", new DummyProvider());  // $DesUsage
        Cipher.getInstance("DES/CBC/PKCS5Padding");  // $DesUsage
        Cipher.getInstance("DES/ECB/NoPadding");  // $DesUsage $EcbMode
        Cipher.getInstance("DES/ECB/PKCS5Padding");  // $DesUsage $EcbMode
        Cipher.getInstance("DESede/CBC/NoPadding");  // $TdesUsage
        Cipher.getInstance("DESede/CBC/PKCS5Padding");  // $TdesUsage
        Cipher.getInstance("DESede/ECB/NoPadding");  // $TdesUsage $EcbMode
        Cipher.getInstance("DESede/ECB/PKCS5Padding");  // $TdesUsage $EcbMode
        Cipher.getInstance("RSA/ECB/PKCS1Padding");
        Cipher.getInstance("RSA/ECB/OAEPWithSHA-1AndMGF1Padding");
        Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding");
        Cipher.getInstance("RC2/ECB/PKCS5Padding");
        Cipher.getInstance("ARCFOUR/ECB/NOPADDING");
        Cipher.getInstance("DES/CBC/NoPadding", "SunJCE");  // $DesUsage
        Cipher.getInstance("DES");  // $DesUsage $EcbMode
        Cipher.getInstance("RSA"); //Just to test a cipher with a different format in the input
    }

    /**
     * Sun provider are at risk to be remove. This dummy provider will
     * be easier to maintain.
     */
    static class DummyProvider extends Provider {

        protected DummyProvider() {
            super("dummy", 1.0, "");
        }
    }
}
