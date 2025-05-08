package com.liferay.healthcheck.operation.internal.auxiliary.cert;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.X509TrustManager;

/**
 * When healthchecks connect to external systems, we're only extracting the duration
 * and host name - this makes it ok to trust those connections by default (only with
 * an established connection can we extract the values we're interested in)
 * 
 * The underlying assumption is: If the connection is not considered trustworthy by
 * the system (e.g. due to a self-signed certificate), the malfunction will surface
 * more-or-less immediately anyway. 
 * 
 * Certificate-related healthchecks have been created to warn of expiring certificates
 * <i>long before</i> they are actually expired. Once they expire (or are untrusted), 
 * malfunction is likely detected anyway.
 * 
 * This trust manager implements the level of trust we need in this particular, 
 * very specialized, use case. Do not use elsewhere, as full explicit trust is 
 * typically not what you want!
 */

class DefaultTrustManager implements X509TrustManager {
    @Override
    public void checkClientTrusted(X509Certificate[] arg0, String arg1) 
    		throws CertificateException {}

    @Override
    public void checkServerTrusted(X509Certificate[] arg0, String arg1) 
    		throws CertificateException {}

    @Override
    public X509Certificate[] getAcceptedIssuers() {
        return null;
    }
}