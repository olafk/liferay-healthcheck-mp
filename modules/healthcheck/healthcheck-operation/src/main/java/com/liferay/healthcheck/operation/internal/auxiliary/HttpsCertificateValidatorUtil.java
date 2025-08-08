package com.liferay.healthcheck.operation.internal.auxiliary;

import com.liferay.healthcheck.HealthcheckItem;
import com.liferay.healthcheck.operation.internal.auxiliary.cert.HttpsCertificateValidatorImpl;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.settings.CompanyServiceSettingsLocator;
import com.liferay.portal.kernel.settings.FallbackKeysSettingsUtil;
import com.liferay.portal.kernel.settings.SettingsException;
import com.liferay.portal.kernel.settings.SettingsLocator;
import com.liferay.portal.kernel.settings.TypedSettings;
import com.liferay.portal.kernel.util.HtmlUtil;

import java.io.IOException;
import java.net.URL;
import java.net.UnknownHostException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.HashMap;

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
 * As we're dealing with weeks validity, no attention has been paid to 
 * time zones. It's irrelevant if we warn a few hours too early or too late 
 * on this scale, and when the results are cached for 12h anyway.
 * 
 * TODO: Implement proxy connections to external servers.
 * 
 * @author Olaf Kock
 */

public class HttpsCertificateValidatorUtil  {
	private static final String _MSG = "certificate-for-x-is-valid-for-x-weeks";
	private static final String _MSG_EXPIRED = "certificate-for-x-is-expired-since-x-x-weeks-ago";
	private static final String _MSG_NOT_HTTPS = "url-x-is-not-https";
	private static final String _MSG_UNKNOWN_HOST = "cant-resolve-url-x-for-certificate-check";
	private static final String _MSG_IO = "ioexception-when-connecting-to-x-x";
	
	public static void validateCertificate(URL url, long companyId, String hint, Collection<HealthcheckItem> result) throws Exception {
		try {
			if("https".equals(url.getProtocol())) {
				HttpsCertificateValidatorImpl validator = getCompanyValidator(companyId);
				long minimumValidity = getConfigurationValue(companyId, "minimumCertValidityWeeks");
				LocalDateTime validity = validator.extractValidity(url);
				long weeksValid = ChronoUnit.WEEKS.between(LocalDateTime.now(), validity);
				if(weeksValid<0) {
					result.add(new HealthcheckItem(false, hint, _MSG_EXPIRED, url.getHost(), validity, Math.abs(weeksValid)));
				} else {
					result.add(new HealthcheckItem((weeksValid > minimumValidity), hint, _MSG, url.getHost(), weeksValid, validity.toString()));
				}
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
	
	private static HttpsCertificateValidatorImpl getCompanyValidator(long companyId) throws SettingsException {
		HttpsCertificateValidatorImpl validator = validatorCache.get(companyId);
		if(validator == null) {
			long maxCacheAgeHours = getConfigurationValue(companyId, "maxCacheAgeHours");
			validator = new HttpsCertificateValidatorImpl(maxCacheAgeHours);
			validatorCache.put(companyId, validator);
		}
		return validator;
	}

	private static HashMap<Long, HttpsCertificateValidatorImpl> validatorCache = new HashMap<>();
	
	/**
	 * retrieve the virtual-instance-specific validity period for https certificates
	 * 
	 * @param companyId
	 * @return
	 * @throws SettingsException
	 */
	private static long getConfigurationValue(long companyId, String key) throws SettingsException {
		String settingsId = "com.liferay.healthcheck.operation.internal.configuration.HealthcheckOperationalConfiguration";
		SettingsLocator settingsLocator = new CompanyServiceSettingsLocator(companyId, settingsId, settingsId);
		TypedSettings settings = new TypedSettings(FallbackKeysSettingsUtil.getSettings(settingsLocator));
		long minimumValidity = settings.getLongValue(key);
		return minimumValidity;
	}
    static Log _log = LogFactoryUtil.getLog(HttpsCertificateValidatorUtil.class);
}
