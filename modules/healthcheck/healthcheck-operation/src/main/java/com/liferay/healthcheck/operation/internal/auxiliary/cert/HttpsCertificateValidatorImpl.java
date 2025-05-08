package com.liferay.healthcheck.operation.internal.auxiliary.cert;

import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.vulcan.util.LocalDateTimeUtil;

import java.io.IOException;
import java.net.URL;
import java.security.SecureRandom;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;

public class HttpsCertificateValidatorImpl {
	private long maxCacheAgeHours;

	/**
	 * @param maxCacheAgeHours don't reconnect to a server for this long. Set to negative for no-cache.
	 */
	public HttpsCertificateValidatorImpl(long maxCacheAgeHours) {
		this.maxCacheAgeHours = maxCacheAgeHours;
	}

	/**
	 * Retrieve the shortest validity of any certificate in the certificate chain to url.
	 * 
	 * @param url
	 * @return
	 * @throws Exception
	 */
	public LocalDateTime extractValidity(URL url) throws Exception {
		String host = url.getHost();
		ValidityCacheItem cachedResult = _validityCache.get(host);
		if(cachedResult == null || 
				ChronoUnit.HOURS.between(
						cachedResult.lastChecked, LocalDateTime.now()) > (int) maxCacheAgeHours) {
			LocalDateTime validity = extractValidityFromServer(url);
			cachedResult = new ValidityCacheItem(validity);
			_validityCache.put(host, cachedResult);
		} else {
			_log.info("using cached certificate validity for " + host + ": " + cachedResult.validity + ", last validated: " + cachedResult.lastChecked);
		}
		return cachedResult.validity;
	}
	
	private static final class ValidityCacheItem {
		public ValidityCacheItem(LocalDateTime validity) {
			this.validity = validity;
			this.lastChecked = LocalDateTime.now();
		}
		public LocalDateTime validity;
		public LocalDateTime lastChecked;
	}
	
	private HashMap<String, ValidityCacheItem> _validityCache = new HashMap<String, ValidityCacheItem>(); 
	
	/**
	 * Note: We don't need any trust relationship with the server we connect to, because 
	 * (a) they're provided by our administrators and should be at least worthy to connect 
	 * and check, and (b) we're only extracting the certificate validity and are not 
	 * communicating any other relevant data over this connection.
	 *
	 * we will, however, check <i>that</i> the requested server <i>name</i> is present in the 
	 * certificate chain, because an explicit message will help to debug any problems with 
	 * the connection if there is a mismatch
	 * 
	 * @param url
	 * @return
	 * @throws Exception
	 */
	
	private LocalDateTime extractValidityFromServer(URL url) throws Exception {
    	@SuppressWarnings("deprecation")
		Date resultDate = new Date(9999, 1, 1);
        SSLContext ctx = SSLContext.getInstance("TLS");
        ctx.init(new KeyManager[0], new TrustManager[] {new DefaultTrustManager()}, new SecureRandom());
        SSLContext.setDefault(ctx);

        HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
        
        SANExtractingHostnameVerifier sanExtractingHostnameVerifier = new SANExtractingHostnameVerifier();
		conn.setHostnameVerifier(sanExtractingHostnameVerifier);
       	conn.getResponseCode();
    	Collection<String> subjectAlternativeNames = sanExtractingHostnameVerifier.getRequestedSubjectAlternativeNames();
    	
       	if(subjectAlternativeNames.isEmpty()) {
	        Certificate[] certs = conn.getServerCertificates();
	        for (Certificate cert :certs){
	        	X509Certificate x509cert = (X509Certificate) cert;
	        	Date notAfter = x509cert.getNotAfter();
	        	if(notAfter.before(resultDate) ) {
	        		resultDate = notAfter;
	        	}
	        }
	        conn.disconnect();
			LocalDateTime result = LocalDateTimeUtil.toLocalDateTime(resultDate);
			_log.debug("Certificate chain of " + url + " valid until " + result);
			return result; 
       	} else {
	        conn.disconnect();
	        // TODO: If this was not an exception, the message could be properly translated.
       		throw new IOException(
       				"Host name " + url.getHost() + " not found in certificates. Available names: " +
       				StringUtil.merge(subjectAlternativeNames, ", "));
       	}
    }
    
    static Log _log = LogFactoryUtil.getLog(HttpsCertificateValidatorImpl.class);
}
