package com.liferay.healthcheck.operation.internal.auxiliary;

import com.liferay.healthcheck.HealthcheckItem;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.settings.CompanyServiceSettingsLocator;
import com.liferay.portal.kernel.settings.FallbackKeysSettingsUtil;
import com.liferay.portal.kernel.settings.SettingsException;
import com.liferay.portal.kernel.settings.SettingsLocator;
import com.liferay.portal.kernel.settings.TypedSettings;
import com.liferay.portal.kernel.util.HtmlUtil;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.vulcan.util.LocalDateTimeUtil;

import java.io.IOException;
import java.net.URL;
import java.net.UnknownHostException;
import java.security.SecureRandom;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

/**
 * This implementation can be used to add HTTPS certificate validity checks
 * to all other Healthchecks that reference URLs. With this add-on, they can
 * visualize certificates that need renewal, which is otherwise easy to miss
 * until the certificate actually expires. 
 * 
 * The configuration is a bit messy - it is looking up company-specific 
 * configuration and will cache the validity results for 12 hours, to limit
 * time-consuming external connections and to make sure that external servers
 * aren't getting upset with occasional connections.
 * 
 * As we're dealing with weeks validity, no attention has been paid for 
 * time zones. It's irrelevant if we warn a few hours too early or too late 
 * on this scale.
 * 
 * TODO: Implement proxy connections to external servers.
 * 
 * @author Olaf Kock
 */

public class HttpsCertificateValidatorUtil  {
	private static final String _MSG = "certificate-for-x-is-valid-for-x-weeks";
	private static final String _MSG_NOT_HTTPS = "url-x-is-not-https";
	private static final String _MSG_UNKNOWN_HOST = "cant-resolve-url-x-for-certificate-check";
	private static final String _MSG_IO = "ioexception-when-connecting-to-x-x";
	
	public static void validateCertificate(URL url, long companyId, String hint, Collection<HealthcheckItem> result) throws Exception {
		try {
			if("https".equals(url.getProtocol())) {
				long minimumValidity = getMinimumValidity(companyId);
				LocalDateTime validity = extractValidity(url); 
				long weeksValid = ChronoUnit.WEEKS.between(LocalDateTime.now(), validity);
				result.add(new HealthcheckItem((weeksValid > minimumValidity), hint, _MSG, url.toString(), weeksValid));
			} else {
				result.add(new HealthcheckItem(false, hint, _MSG_NOT_HTTPS, url.toString()));
			}
		} catch (UnknownHostException e) {
			result.add(new HealthcheckItem(false, hint, _MSG_UNKNOWN_HOST, url.toString()));
		} catch (IOException e) {
			result.add(new HealthcheckItem(false, hint, _MSG_IO, url.toString(), HtmlUtil.escape(e.getMessage())));
		} catch (IllegalStateException e) {
			result.add(new HealthcheckItem(false, hint, _MSG_IO, url.toString(), HtmlUtil.escape(e.getMessage())));
		}
	}

	/**
	 * retrieve the virtual-instance-specific validity period for https certificates
	 * 
	 * @param companyId
	 * @return
	 * @throws SettingsException
	 */
	private static long getMinimumValidity(long companyId) throws SettingsException {
		String settingsId = "com.liferay.healthcheck.operation.internal.configuration.HealthcheckOperationalConfiguration";
		SettingsLocator settingsLocator = new CompanyServiceSettingsLocator(companyId, settingsId, settingsId);
		TypedSettings settings = new TypedSettings(FallbackKeysSettingsUtil.getSettings(settingsLocator));
		long minimumValidity = settings.getLongValue("minimumCertValidityWeeks");
		return minimumValidity;
	}
	
	/**
	 * Retrieve the shortest validity of any certificate in the certificate chain to url.
	 * The result is cached for 12 hours, before another connection is made
	 * 
	 * @param url
	 * @return
	 * @throws Exception
	 */
	private static LocalDateTime extractValidity(URL url) throws Exception {
		
		// temp:
		_cache.clear();
		
		
		// connect to server only every 12 hours
		int maxValidationAgeHours = 12;
		ValidityCache cachedResult = _cache.get(url);
		if(cachedResult == null || 
				ChronoUnit.HOURS.between(
						cachedResult.lastChecked, LocalDateTime.now()) > maxValidationAgeHours) {
			LocalDateTime validity = extractValidityFromServer(url);
			cachedResult = new ValidityCache(validity);
			_cache.put(url, cachedResult);
		} else {
			_log.debug("using cached result for " + url + ": " + cachedResult.validity);
		}
		return cachedResult.validity;
	}
	
	private static final class ValidityCache {
		public ValidityCache(LocalDateTime validity) {
			this.validity = validity;
			this.lastChecked = LocalDateTime.now();
		}
		public LocalDateTime validity;
		public LocalDateTime lastChecked;
	}
	
	private static HashMap<URL, ValidityCache> _cache = new HashMap<URL, ValidityCache>(); 
	
	/**
	 * Note: We don't need any trust relationship with the server we connect to, because 
	 * (a) they're provided by our administrators and should be trustworthy and
	 * (b) we're only extracting the certificate validity and are not communicating
	 * any other relevant data over this connection.
	 * 
	 * @param url
	 * @return
	 * @throws Exception
	 */
	
	private static LocalDateTime extractValidityFromServer(URL url) throws Exception {
    	@SuppressWarnings("deprecation")
		Date resultDate = new Date(9999, 1, 1);
        SSLContext ctx = SSLContext.getInstance("TLS");
        ctx.init(new KeyManager[0], new TrustManager[] {new DefaultTrustManager()}, new SecureRandom());
        SSLContext.setDefault(ctx);

        HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
        
    	LinkedList<String> subjectAlternativeNames = new LinkedList<String>();
        conn.setHostnameVerifier(new HostnameVerifier() {
            @Override
            public boolean verify(String arg0, SSLSession arg1) {
            	_log.warn("hostname verification triggered for " + arg0);
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
								_log.warn("available SAN: " + hostname);
								subjectAlternativeNames.add(hostname);
							}
						}
					}
				} catch (Exception e) {
					_log.error(e);
				}
                return true;
            }
        });
       	conn.getResponseCode();
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
    
    private static class DefaultTrustManager implements X509TrustManager {
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
    
    static Log _log = LogFactoryUtil.getLog(HttpsCertificateValidatorUtil.class);
}
