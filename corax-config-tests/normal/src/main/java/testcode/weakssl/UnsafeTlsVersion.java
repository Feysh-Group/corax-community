package testcode.weakssl;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

public class UnsafeTlsVersion {

  public static void testSslContextWithProtocol() throws NoSuchAlgorithmException {

    // unsafe
    SSLContext.getInstance("SSL");     // $SslContext
    SSLContext.getInstance("SSLv2");   // $SslContext
    SSLContext.getInstance("SSLv3");   // $SslContext
    SSLContext.getInstance("TLS");     // $SslContext
    SSLContext.getInstance("TLSv1");   // $SslContext
    SSLContext.getInstance("TLSv1.1"); // $SslContext

    // safe
    SSLContext.getInstance("TLSv1.2"); // !$SslContext
    SSLContext.getInstance("TLSv1.3"); // !$SslContext
  }

  public static void testCreateSslParametersWithProtocol(String[] cipherSuites) {

    // unsafe
    createSslParameters(cipherSuites, "SSLv3");       // $SslContext
    createSslParameters(cipherSuites, "TLS");         // $SslContext
    createSslParameters(cipherSuites, "TLSv1");       // $SslContext
    createSslParameters(cipherSuites, "TLSv1.1");     // $SslContext
    createSslParameters(cipherSuites, "TLSv1", "TLSv1.1", "TLSv1.2");     // $SslContext

    // safe
    createSslParameters(cipherSuites, "TLSv1.2");     // !$SslContext
    createSslParameters(cipherSuites, "TLSv1.3");     // !$SslContext
  }

  public static SSLParameters createSslParameters(String[] cipherSuites, String... protocols) {
    return new SSLParameters(cipherSuites, protocols);
  }

  public static void testSettingProtocolsForSslParameters() {

    // unsafe
    new SSLParameters().setProtocols(new String[] { "SSLv3" });    // $SslContext
    new SSLParameters().setProtocols(new String[] { "TLS" });      // $SslContext
    new SSLParameters().setProtocols(new String[] { "TLSv1" });    // $SslContext
    new SSLParameters().setProtocols(new String[] { "TLSv1.1" });  // $SslContext

    SSLParameters parameters = new SSLParameters();
    parameters.setProtocols(new String[] { "TLSv1.1", "TLSv1.2" });// $SslContext

    // safe
    new SSLParameters().setProtocols(new String[] { "TLSv1.2" });  // !$SslContext

    parameters = new SSLParameters();
    parameters.setProtocols(new String[] { "TLSv1.2", "TLSv1.3" });// !$SslContext
  }

  public static void testSettingProtocolForSslSocket() throws IOException {

    // unsafe
    createSslSocket("SSLv3");               // $SslContext
    createSslSocket("TLS");                 // $SslContext
    createSslSocket("TLSv1");               // $SslContext
    createSslSocket("TLSv1.1");             // $SslContext
    createSslSocket("TLSv1.1", "TLSv1.2");  // $SslContext

    // safe
    createSslSocket("TLSv1.2");             // !$SslContext
    createSslSocket("TLSv1.3");             // !$SslContext
  }

  public static SSLSocket createSslSocket(String... protocols) throws IOException {
    SSLSocket socket = (SSLSocket) SSLSocketFactory.getDefault().createSocket();
    socket.setEnabledProtocols(protocols);
    return socket;
  }

  public static void testSettingProtocolForSslServerSocket() throws IOException {

    // unsafe
    createSslServerSocket("SSLv3");                 // $SslContext
    createSslServerSocket("TLS");                   // $SslContext
    createSslServerSocket("TLSv1");                 // $SslContext
    createSslServerSocket("TLSv1.1");               // $SslContext
    createSslServerSocket("TLSv1.1", "TLSv1.2");    // $SslContext

    // safe
    createSslServerSocket("TLSv1.2");               // !$SslContext
    createSslServerSocket("TLSv1.3");               // !$SslContext
  }

  public static SSLServerSocket createSslServerSocket(String... protocols) throws IOException {
    SSLServerSocket socket = (SSLServerSocket) SSLServerSocketFactory.getDefault().createServerSocket();
    socket.setEnabledProtocols(protocols);
    return socket;
  }

  public static void testSettingProtocolForSslEngine() throws NoSuchAlgorithmException {

    // unsafe
    createSslEngine("SSLv3");                // $SslContext
    createSslEngine("TLS");                  // $SslContext
    createSslEngine("TLSv1");                // $SslContext
    createSslEngine("TLSv1.1");              // $SslContext
    createSslEngine("TLSv1.1", "TLSv1.2");   // $SslContext

    // safe
    createSslEngine("TLSv1.2");              // !$SslContext
    createSslEngine("TLSv1.3");              // !$SslContext
  }

  public static SSLEngine createSslEngine(String... protocols) throws NoSuchAlgorithmException {
    SSLEngine engine = SSLContext.getDefault().createSSLEngine();
    engine.setEnabledProtocols(protocols);
    return engine;
  }
}
