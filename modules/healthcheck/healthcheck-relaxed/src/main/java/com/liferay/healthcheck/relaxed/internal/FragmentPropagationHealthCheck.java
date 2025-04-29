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

@Component(service = Healthcheck.class)
public class FragmentPropagationHealthCheck extends RelaxedHealthcheckBaseImpl {
	private static final String LINK = "/group/control_panel/manage?p_p_id=com_liferay_configuration_admin_web_portlet_InstanceSettingsPortlet&p_p_lifecycle=0&p_p_state=maximized&p_p_mode=view&_com_liferay_configuration_admin_web_portlet_InstanceSettingsPortlet_mvcRenderCommandName=%2Fconfiguration_admin%2Fview_configuration_screen&_com_liferay_configuration_admin_web_portlet_InstanceSettingsPortlet_configurationScreenKey=fragments-service-company";
	private static final String MSG = "relaxed-healthcheck-setting-fragment-propagation";

	@Override
	public Collection<HealthcheckItem> doCheck(long companyId) throws SettingsException, ConfigurationException {
		String settingsId = "com.liferay.fragment.configuration.FragmentServiceConfiguration";
		SettingsLocator settingsLocator = new CompanyServiceSettingsLocator(companyId, settingsId, settingsId);
		TypedSettings settings = new TypedSettings(FallbackKeysSettingsUtil.getSettings(settingsLocator));
		boolean propagate = settings.getBooleanValue("propagateContributedFragmentChanges") &&
				settings.getBooleanValue("propagateChanges");

		return Arrays.asList(new HealthcheckItem(propagate, LINK, MSG));
	}

	static Log _log = LogFactoryUtil.getLog(FragmentPropagationHealthCheck.class);
}
