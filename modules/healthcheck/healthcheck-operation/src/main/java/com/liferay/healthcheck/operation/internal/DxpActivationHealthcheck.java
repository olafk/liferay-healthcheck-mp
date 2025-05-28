/**
 * SPDX-FileCopyrightText: (c) 2025 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.healthcheck.operation.internal;

import com.liferay.healthcheck.Healthcheck;
import com.liferay.healthcheck.HealthcheckItem;
import com.liferay.healthcheck.operation.internal.configuration.HealthcheckOperationalSystemConfiguration;
import com.liferay.portal.configuration.module.configuration.ConfigurationProvider;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.license.util.LicenseManagerUtil;
import com.liferay.portal.kernel.model.CompanyConstants;
import com.liferay.portal.kernel.util.ReleaseInfo;

import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;
import java.util.Map;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

@Component(
	configurationPid = "com.liferay.healthcheck.operation.internal.configuration.HealthcheckOperationalSystemConfiguration",
	service = Healthcheck.class
)
public class DxpActivationHealthcheck implements Healthcheck {

	@Override
	public Collection<HealthcheckItem> check(long companyId) throws PortalException {
		LinkedList<HealthcheckItem> result = new LinkedList<HealthcheckItem>();
		
		if(ReleaseInfo.isDXP()) {
			HealthcheckOperationalSystemConfiguration configuration = null;
			
			if (companyId != CompanyConstants.SYSTEM) {
				configuration =
					_configurationProvider.getCompanyConfiguration(
						HealthcheckOperationalSystemConfiguration.class, companyId);
			}
			else {
				configuration =
					_configurationProvider.getSystemConfiguration(
						HealthcheckOperationalSystemConfiguration.class);
			}
			int remainingActivationWeeks = configuration.remainingActivationWeeks();

			Map<String, String> licenseProperties = LicenseManagerUtil.getLicenseProperties("Portal");
			long expires = Long.valueOf(licenseProperties.get("expirationDate"));
			long now = new Date().getTime();
			long remainingMillis = expires - now;
			long remainingWeeks = remainingMillis / (1000 * 60 * 60 * 24 * 7);
	
			result.add(new HealthcheckItem(
					remainingWeeks > remainingActivationWeeks,
					_ADDITIONAL_DOCUMENTATION, null,
					_MSG, remainingWeeks, remainingActivationWeeks));
		}
		return result;
	}

	@Override
	public String getCategory() {
		return "healthcheck-category-operation";
	}

	@Reference
	private ConfigurationProvider _configurationProvider;

	private final String[] _ADDITIONAL_DOCUMENTATION = {
			"https://learn.liferay.com/w/dxp/installation-and-upgrades/setting-up-liferay/activating-liferay-dxp"
	};
	
	private final String _MSG = "your-dxp-activation-key-is-valid-for-another-x-weeks";
}
