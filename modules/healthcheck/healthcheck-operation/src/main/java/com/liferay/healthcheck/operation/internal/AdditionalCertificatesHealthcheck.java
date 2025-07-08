/**
 * SPDX-FileCopyrightText: (c) 2024 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.healthcheck.operation.internal;

import com.liferay.configuration.admin.constants.ConfigurationAdminPortletKeys;
import com.liferay.healthcheck.Healthcheck;
import com.liferay.healthcheck.HealthcheckItem;
import com.liferay.healthcheck.operation.internal.auxiliary.HttpsCertificateValidatorUtil;
import com.liferay.petra.string.StringBundler;
import com.liferay.portal.kernel.settings.CompanyServiceSettingsLocator;
import com.liferay.portal.kernel.settings.FallbackKeysSettingsUtil;
import com.liferay.portal.kernel.settings.SettingsLocator;
import com.liferay.portal.kernel.settings.TypedSettings;

import java.net.URI;
import java.net.URL;
import java.util.Collection;
import java.util.LinkedList;

import org.osgi.service.component.annotations.Component;

/**
 * Check for validity of certificates for a manually configured list of servers
 *
 * @author Olaf Kock
 */
@Component(service = Healthcheck.class)
public class AdditionalCertificatesHealthcheck implements Healthcheck {

	@Override
	public Collection<HealthcheckItem> check(long companyId) throws Exception {
		LinkedList<HealthcheckItem> result = new LinkedList<HealthcheckItem>();
		
		String settingsId = "com.liferay.healthcheck.operation.internal.configuration.HealthcheckOperationalConfiguration";
		SettingsLocator settingsLocator = new CompanyServiceSettingsLocator(companyId, settingsId, settingsId);
		TypedSettings settings = new TypedSettings(FallbackKeysSettingsUtil.getSettings(settingsLocator));
		String[] additionalHosts = settings.getValues("additionalCheckedCertificateHosts");

		for (String host : additionalHosts) {
			URL url;
			if(host.startsWith("https://")) {
				url = new URI(host).toURL();
			} else if(host.startsWith("http://")) {
				url = new URI(host).toURL();
			} else {
				url = new URI("https://" + host).toURL();
			}
			HttpsCertificateValidatorUtil.validateCertificate(
					url, companyId, _CONFIGURATION_LINK, result);
		}

		return result;
	}

	@Override
	public String getCategory() {
		return "healthcheck-category-operation";
	}

	private static final String _CONFIGURATION_LINK = StringBundler.concat(
		"/group/control_panel/manage?p_p_id=",
		ConfigurationAdminPortletKeys.INSTANCE_SETTINGS, "&_",
		ConfigurationAdminPortletKeys.INSTANCE_SETTINGS, "_factoryPid=",
		AdditionalCertificatesHealthcheck._PID, "&_",
		ConfigurationAdminPortletKeys.INSTANCE_SETTINGS,
		"_mvcRenderCommandName=%2Fconfiguration_admin%2Fedit_configuration&_",
		ConfigurationAdminPortletKeys.INSTANCE_SETTINGS, "_pid=",
		AdditionalCertificatesHealthcheck._PID);

	private static final String _PID =
		"com.liferay.healthcheck.operation.internal.configuration." +
			"HealthcheckOperationalConfiguration";
}