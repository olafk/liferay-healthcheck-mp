/**
 * SPDX-FileCopyrightText: (c) 2024 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.healthcheck.operation.internal;

import com.liferay.configuration.admin.constants.ConfigurationAdminPortletKeys;
import com.liferay.healthcheck.Healthcheck;
import com.liferay.healthcheck.HealthcheckItem;
import com.liferay.healthcheck.HostnameDetector;
import com.liferay.petra.string.StringBundler;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.service.CompanyLocalService;

import java.util.Arrays;
import java.util.Collection;
import java.util.Set;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * Checks if the configured virtualHost for the company has been accessed yet.
 * This helps identifying if this system has been restored under a different name
 * and might need other adjustments as well.
 *
 * @author Olaf Kock
 */
@Component(service = Healthcheck.class)
public class VirtualHostHealthcheck implements Healthcheck {

	@Override
	public Collection<HealthcheckItem> check(long companyId)
		throws PortalException {

		String virtualHostname = _companyLocalService.getCompany(
			companyId
		).getVirtualHostname();
		Set<String> requestedHostnames = _hostnameDetector.getAccessedUrls(
			companyId);

		if (requestedHostnames.contains("https://" + virtualHostname) ||
			requestedHostnames.contains("http://" + virtualHostname)) {

			return Arrays.asList(
				new HealthcheckItem(
					true, _CONFIGURATION_LINK, _MSG, virtualHostname));
		}

		return Arrays.asList(
			new HealthcheckItem(
				false, _CONFIGURATION_LINK, _MSG_ERROR, virtualHostname));
	}

	@Override
	public String getCategory() {
		return "healthcheck-category-operation";
	}

	private static final String _CONFIGURATION_LINK = StringBundler.concat(
		"/group/control_panel/manage?p_p_id=",
		ConfigurationAdminPortletKeys.INSTANCE_SETTINGS, "&_",
		ConfigurationAdminPortletKeys.INSTANCE_SETTINGS, "_mvcRenderCommandName=%2Fconfiguration_admin%2Fview_configuration_screen&_",
		ConfigurationAdminPortletKeys.INSTANCE_SETTINGS,
		"_configurationScreenKey=general");

	private static final String _MSG =
		"the-configured-company-virtual-host-x-has-been-accessed";

	private static final String _MSG_ERROR =
		"the-configured-company-virtual-host-x-has-not-been-accessed-yet";

	private static final String _PID =
		"com.liferay.healthcheck.operation.internal.configuration." +
			"HealthcheckOperationalSystemConfiguration";

	@Reference
	private CompanyLocalService _companyLocalService;

	@Reference
	private HostnameDetector _hostnameDetector;

}