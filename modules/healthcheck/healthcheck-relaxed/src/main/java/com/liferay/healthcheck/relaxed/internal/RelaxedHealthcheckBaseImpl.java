package com.liferay.healthcheck.relaxed.internal;

import com.liferay.healthcheck.Healthcheck;
import com.liferay.healthcheck.HealthcheckItem;
import com.liferay.healthcheck.relaxed.internal.configuration.HealthcheckRelaxedConfiguration;
import com.liferay.portal.configuration.module.configuration.ConfigurationProviderUtil;
import com.liferay.portal.kernel.service.CompanyLocalServiceUtil;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Relaxed Healthchecks run conditionally, as they don't promote production settings.
 * This base class implements the conditional check, so that the individual 
 * implementations can stay clean and concise.
 * 
 * @author Olaf Kock
 */

public abstract class RelaxedHealthcheckBaseImpl implements Healthcheck {

	@Override
	public final Collection<HealthcheckItem> check(long companyId) throws Exception {
		if(isShow(companyId)) {
			return doCheck(companyId);
		} else {
			return Collections.emptyList();
		}
	}
	
	@Override
	public final String getCategory() {
		return "healthcheck-category-relaxed";
	}
	
	protected boolean isConditional() {
		return true;
	}
	
	protected boolean isShow(long companyId) throws Exception {
		// using Util-classes instead of proper DS because this is a super-class, and
		// @Reference is not processed for superclasses of the implementation classes
		// that we're doing this for.
		HealthcheckRelaxedConfiguration config = ConfigurationProviderUtil.getCompanyConfiguration(HealthcheckRelaxedConfiguration.class, companyId);
		if(! config.isRelaxed()) {
			return false;
		}
		String virtualHostname = CompanyLocalServiceUtil.getCompany(
				companyId
			).getVirtualHostname();
		List<String> hostnames = Arrays.asList(config.relaxedInstanceHostnames());
		return hostnames.contains(virtualHostname);
	}
	
	public abstract Collection<HealthcheckItem> doCheck(long companyId) throws Exception;
}
