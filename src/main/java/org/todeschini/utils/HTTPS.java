package org.todeschini.utils;


import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;

import javax.enterprise.context.ApplicationScoped;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

@ApplicationScoped
public abstract class HTTPS {
	
	@Deprecated
	public static void k() throws NoSuchAlgorithmException, KeyManagementException {
		/**
		 * Faz que ignore a falta de certificado HTTPS
		 */
		TrustManager[] trustManagers = new TrustManager[] {new X509TrustManager() {
	            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
	                return null;
	            }
	            public void checkClientTrusted(X509Certificate[] certs, String authType) {
	            }
	            public void checkServerTrusted(X509Certificate[] certs, String authType) {
	            }
	        }
	    };
	
	    // Install the all-trusting trust manager
	    SSLContext sslCtx;
		sslCtx = SSLContext.getInstance("SSL");
		sslCtx.init(null, trustManagers, new java.security.SecureRandom());
		HttpsURLConnection.setDefaultSSLSocketFactory(sslCtx.getSocketFactory());
		
		// Create all-trusting host name verifier
		HostnameVerifier trusting = new HostnameVerifier() {
			public boolean verify(String hostname, SSLSession sc) {
				return true;
			}
		};
		
		// Install the all-trusting host verifier
		HttpsURLConnection.setDefaultHostnameVerifier(trusting);
		
	}

}
