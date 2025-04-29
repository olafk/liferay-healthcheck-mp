/**
 * Copyright (c) 2022-present Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

package com.liferay.healthcheck.relaxed.internal;

import com.liferay.healthcheck.Healthcheck;
import com.liferay.healthcheck.HealthcheckItem;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.module.configuration.ConfigurationException;
import com.liferay.portal.kernel.settings.CompanyServiceSettingsLocator;
import com.liferay.portal.kernel.settings.FallbackKeysSettingsUtil;
import com.liferay.portal.kernel.settings.SettingsException;
import com.liferay.portal.kernel.settings.SettingsLocator;
import com.liferay.portal.kernel.settings.TypedSettings;

import java.util.Arrays;
import java.util.Collection;

import org.osgi.service.component.annotations.Component;

/**
 * HealthCheck for relaxed-security Demo Systems Demo Systems should have the
 * session extended as long as a browser is open, to cater for longer Q&A
 * sessions without any interruption when the logout-message is missed.
 * 
 * @author Olaf Kock
 */
@Component(service = Healthcheck.class)
public class SessionTimeoutHealthCheck extends RelaxedHealthcheckBaseImpl {

	private static final String LINK = "/group/control_panel/manage?p_p_id=com_liferay_configuration_admin_web_portlet_InstanceSettingsPortlet&_com_liferay_configuration_admin_web_portlet_InstanceSettingsPortlet_mvcRenderCommandName=%2Fconfiguration_admin%2Fedit_configuration&_com_liferay_configuration_admin_web_portlet_InstanceSettingsPortlet_factoryPid=com.liferay.frontend.js.web.internal.session.timeout.configuration.SessionTimeoutConfiguration&_com_liferay_configuration_admin_web_portlet_InstanceSettingsPortlet_pid=com.liferay.frontend.js.web.internal.session.timeout.configuration.SessionTimeoutConfiguration.scoped"; //%7Ec9c6ce8e-d639-4884-9841-83ba2696e7ff";
	private static final String MSG = "relaxed-healthcheck-setting-session-extension-enabled";

	@Override
	public Collection<HealthcheckItem> doCheck(long companyId) throws SettingsException, ConfigurationException {
		// setting migrated from portal.properties to OSGi config in release 2024q2 or earlier
		// As these healthchecks have to compile on 2024q2 and later (due to Java21 limitations):
		// Do not check for portal.properties configuration - OSGi is sufficient.
		String settingsId = "com.liferay.frontend.js.web.internal.session.timeout.configuration.SessionTimeoutConfiguration";
		SettingsLocator settingsLocator = new CompanyServiceSettingsLocator(companyId, settingsId, settingsId);
		TypedSettings settings = new TypedSettings(FallbackKeysSettingsUtil.getSettings(settingsLocator));
		boolean autoExtend = settings.getBooleanValue("autoExtend");
		return Arrays.asList(new HealthcheckItem(autoExtend, LINK, MSG));
	}

	static Log _log = LogFactoryUtil.getLog(SessionTimeoutHealthCheck.class);
}