package testcode.hardcode.cryptokey;

import io.vertx.core.Vertx;
import io.vertx.ext.web.handler.CSRFHandler;
import sun.security.provider.DSAPublicKeyImpl;

import javax.crypto.spec.*;
import javax.net.ssl.KeyManagerFactory;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.kerberos.KerberosKey;
import javax.security.auth.kerberos.KerberosTicket;
import java.io.FileInputStream;
import java.math.BigInteger;
import java.net.PasswordAuthentication;
import java.security.KeyRep;
import java.security.KeyStore;
import java.security.spec.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Scanner;

public class HardcodedKey {

    private static String PWD1 = "secret4";
    private static char[] PWD2 = {'s', 'e', 'c', 'r', 'e', 't', '5'};
    private char[] PWD3 = {'s', 'e', 'c', 'r', 'e', 't', '5'};
    private static BigInteger big = new BigInteger("1000000");
    private static final byte[] PUBLIC_KEY = new byte[]{1, 2, 3, 4, 5, 6, 7};

    public void bad1() throws Exception {
        char[] passphrase = "secret1".toCharArray();
        KeyStore ks = KeyStore.getInstance("JKS");
        ks.load(new FileInputStream("keystore"), passphrase);                     // $HardCodePassword
    }

    public static void bad2() throws Exception {
        final String passphrase = "secret2";
        System.out.println("secret2");
        KeyStore ks = KeyStore.getInstance("JKS");
        FileInputStream fs = new FileInputStream("keystore");
        ks.load(fs, passphrase.toCharArray());                                          // $HardCodePassword
    }

    public void bad3() throws Exception {
        char[] passphrase = {'s', 'e', 'c', 'r', 'e', 't', '3'};
        KeyStore.getInstance("JKS").load(new FileInputStream("keystore"), passphrase); // $HardCodePassword
    }

    public void bad4() throws Exception {
        KeyStore ks = KeyStore.getInstance("JKS");
        ks.load(new FileInputStream("keystore"), PWD1.toCharArray());             // $HardCodePassword
    }

    public static void bad5a() throws Exception {
        KeyStore ks = KeyStore.getInstance("JKS");
        ks.load(new FileInputStream("keystore"), PWD2);                           // $HardCodePassword
    }

    public void bad5b() throws Exception {
        KeyStore ks = KeyStore.getInstance("JKS");
        ks.load(new FileInputStream("keystore"), PWD3);                           // $HardCodePassword
    }

    public void bad6() throws Exception {
        String pwdStr = "secret6";
        char[] pwd1 = pwdStr.toCharArray();
        KeyStore ks = KeyStore.getInstance("JKS");
        char[] pwd2 = pwd1;
        ks.load(new FileInputStream("keystore"), pwd2);                           // $HardCodePassword
    }

    public void bad7() throws Exception {
        byte[] bytes = new byte[2];
        char[] pwd = "secret7".toCharArray();
        new PBEKeySpec(pwd);                                                            // $HardCodePassword
        new PBEKeySpec(pwd, bytes, 1);                                      // $HardCodePassword
        new PBEKeySpec(pwd, bytes, 1, 1);                         // $HardCodePassword
        PasswordAuthentication auth = new PasswordAuthentication("user", pwd);// $HardCodeUserName $HardCodePassword
        PasswordCallback callback = new PasswordCallback("str", true);
        callback.setPassword(pwd);                                                       // $HardCodePassword
        KeyStore.PasswordProtection protection = new KeyStore.PasswordProtection(pwd);   // $HardCodePassword
        KerberosKey key = new KerberosKey(null, pwd, "alg");           // $HardCodePassword
        KeyManagerFactory.getInstance("").init(null, pwd);                  // $HardCodePassword
    }


    public void bad8a() throws Exception {
        new DESKeySpec(null); // should not be reported
        byte[] key = {1, 2, 3, 4, 5, 6, 7, 8};
        DESKeySpec spec = new DESKeySpec(key);                                           // $HardCodeKey
        KeySpec spec2 = new DESedeKeySpec(key);                                          // $HardCodeKey
        KerberosKey kerberosKey = new KerberosKey(null, key, 0, 0);// $HardCodeKey
        System.out.println(spec.getKey()[0] + kerberosKey.getKeyType());
        new SecretKeySpec(key, "alg");                                          // $HardCodeKey
        new SecretKeySpec(key, 0, 0, "alg");                         // $HardCodeKey
        new X509EncodedKeySpec(key);                                                     // $HardCodeKey
        new PKCS8EncodedKeySpec(key);                                                    // $HardCodeKey
        new KeyRep(null, "alg", "format", key);                     // $HardCodeKey
        new KerberosTicket(null, null, null, key, 0, null, null, null, null, null, null); // $HardCodeKey
        new DSAPublicKeyImpl(key);                                                        // $HardCodeKey
    }

    public void bad8b() {
        byte[] key = "secret8".getBytes();
        System.out.println("something");
        new SecretKeySpec(key, "alg");                                          // $HardCodeKey
    }


    public void bad9() throws SQLException {
        String pass = "secret9";
        Connection connection = DriverManager.getConnection("url", "user", PWD1); // $HardCodeUserName $HardCodePassword
        System.out.println(connection.getCatalog());
        connection = DriverManager.getConnection("url", "user", pass);          // $HardCodeUserName $HardCodePassword
        System.out.println(connection.getCatalog());
    }


    public void bad10() throws Exception {
        BigInteger bigInteger = new BigInteger("12345", 5);
        new DSAPrivateKeySpec(bigInteger, null, null, null);                    // $HardCodeKey
        new DSAPublicKeySpec(bigInteger, null, bigInteger, null); // report once   // $HardCodeKey
        new DHPrivateKeySpec(bigInteger, null, null);                              // $HardCodeKey
        new DHPublicKeySpec(bigInteger, null, null);                               // $HardCodeKey
        new ECPrivateKeySpec(bigInteger, null);                                   // $HardCodeKey
        new RSAPrivateKeySpec(bigInteger, null);                            // $HardCodeKey
        new RSAMultiPrimePrivateCrtKeySpec(bigInteger, null, null, null, null, null, null, null, null);// $HardCodeKey
        new RSAPrivateCrtKeySpec(bigInteger, null, null, null, null, null, null, null);// $HardCodeKey
        new RSAPublicKeySpec(bigInteger, null);                              // $HardCodeKey
        new DSAPublicKeyImpl(bigInteger, null, null, null);                      // $HardCodeKey
    }


    public void bad11() {
        new DSAPrivateKeySpec(null, null, null, null); // should not be reported
        System.out.println();
        new DSAPrivateKeySpec(big, null, null, null);                           // $HardCodeKey
    }


    public void bad12() throws Exception {
        byte[] key = "secret8".getBytes("UTF-8");
        BigInteger bigInteger = new BigInteger(key);
        new DSAPrivateKeySpec(bigInteger, null, null, null);                    // $HardCodeKey
    }


    public void bad13() throws Exception {
        String pwd = null;
        if (PWD2[3] < 'u') { // non-trivial condition
            pwd = "hardcoded";
        }
        if (pwd != null) {
            KeyStore.getInstance("JKS").load( // should be reported
                    new FileInputStream("keystore"), pwd.toCharArray());            // $HardCodePassword
        }
    }


    public Connection bad14() throws Exception {
        String pwd;
        if (PWD2[2] % 2 == 1) { // non-trivial condition
            pwd = "hardcoded1";
        } else { // different constant but still hard coded
            pwd = "hardcoded2";
        }
        return DriverManager.getConnection("url", "user", pwd);                  // $HardCodeUserName $HardCodePassword
    }


    public void bad15(Vertx vertx) throws Exception {
        String pwd;
        if (PWD2[2] % 2 == 1) { // non-trivial condition
            pwd = "hardcoded1";
        } else { // different constant but still hard coded
            pwd = "hardcoded2";
        }
        CSRFHandler.create(vertx, pwd);                                                   // $HardcodeCredentialChecker
    }

    private byte[] pwd4; // not considered hard coded !$HardCodeKey
    private char[] pwd5 = null;  // !$HardCodeKey
    private char[] pwd6 = new char[7];  // !$HardCodeKey


    public void good1() throws Exception {
        KeyStore ks = KeyStore.getInstance("JKS");
        ks.load(new FileInputStream("keystore"), getSymbolicPassword());          // !$HardCodePassword
    }

    public void good2() throws Exception {
        String pwd = "uiiii".substring(3) + getSymbolicPassword();
        char[] pwdArray = pwd.toCharArray();
        KeyStore ks = KeyStore.getInstance("JKS");
        ks.load(new FileInputStream("keystore"), pwdArray);                       // !$HardCodePassword
    }

    public void good3() throws Exception {
        String key = "hard coded";
        key = new String(getSymbolicPassword()); // no longer hard coded
        String message = "can be hard coded";
        byte[] byteStringToEncrypt = message.getBytes("UTF-8");
        new SecretKeySpec(key.getBytes(), "AES"); // should not report         // !$HardCodeKey
        byte[] bytes = {0, 0, 7};
        new PBEKeySpec(getSymbolicPassword(), bytes, 1); // different parameter hard coded !$HardCodeKey
        byte[] newArray = new byte[1024]; // not considered hard coded
        new X509EncodedKeySpec(newArray);                                               // !$HardCodeKey
    }
    
    private static char[] getSymbolicPassword() {
        Scanner sc = new Scanner(System.in);
        return sc.nextLine().toCharArray();
    }
}
