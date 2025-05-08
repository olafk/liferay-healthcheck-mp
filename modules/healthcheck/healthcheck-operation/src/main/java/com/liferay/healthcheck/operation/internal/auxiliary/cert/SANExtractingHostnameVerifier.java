package com.liferay.healthcheck.operation.internal.auxiliary.cert;

import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;

/**
 * A HostnameVerifier is involved when the requested server name can not be found
 * within the certificate chain (e.g. when a connection is attempted to example.COM, but 
 * the certificate is valid for example.NET). In this case, this HostnameVerifier will
 * allow the connection, but keep track of the certified host names. This way, the 
 * connection can be established, the certificate validation duration can be extracted, 
 * AND the mismatched host names can be presented.
 * 
 * This verifier is good for use in a single connection opening and should be discarded
 * afterwards, as it keeps track of potential SANs that a hosts certificate contains.
 */
final class SANExtractingHostnameVerifier implements HostnameVerifier {
	private final LinkedList<String> subjectAlternativeNames = new LinkedList<>();

	@Override
	public boolean verify(String arg0, SSLSession arg1) {
		HttpsCertificateValidatorImpl._log.warn("hostname verification triggered for " + arg0);
		try {
			Certificate[] peerCertificates = arg1.getPeerCertificates();
			for (Certificate certificate : peerCertificates) {
				X509Certificate xc = (X509Certificate) certificate;
				Collection<List<?>> sans = xc.getSubjectAlternativeNames();
				if(sans != null) {
					for(List<?> list: sans) {
						// list contains 2 elements: Integer (GeneralName, 0-8) 
						// and host name as either String or byte array.
						// see https://docs.oracle.com/javase/8/docs/api/java/security/cert/X509Certificate.html
						Object ohostname = list.get(1);
						String hostname;
						if(ohostname instanceof String) {
							hostname = (String) ohostname;
						} else {
							hostname = new String((byte[]) ohostname);
						}
						HttpsCertificateValidatorImpl._log.warn("available SAN: " + hostname);
						subjectAlternativeNames.add(hostname);
					}
				}
			}
		} catch (Exception e) {
			HttpsCertificateValidatorImpl._log.error(e);
		}
	    return true;
	}
	
	public Collection<String> getRequestedSubjectAlternativeNames() {
		return subjectAlternativeNames;
	}
}