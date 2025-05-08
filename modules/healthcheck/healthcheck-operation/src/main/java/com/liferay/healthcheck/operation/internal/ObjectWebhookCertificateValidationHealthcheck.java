package com.liferay.healthcheck.operation.internal;

import com.liferay.healthcheck.Healthcheck;
import com.liferay.healthcheck.HealthcheckItem;
import com.liferay.healthcheck.operation.internal.auxiliary.HttpsCertificateValidatorUtil;
import com.liferay.healthcheck.operation.internal.configuration.HealthcheckOperationalConfiguration;
import com.liferay.object.constants.ObjectActionExecutorConstants;
import com.liferay.object.model.ObjectAction;
import com.liferay.object.model.ObjectDefinition;
import com.liferay.object.service.ObjectActionLocalService;
import com.liferay.object.service.ObjectDefinitionLocalService;
import com.liferay.portal.configuration.module.configuration.ConfigurationProvider;
import com.liferay.portal.kernel.model.CompanyConstants;
import com.liferay.portal.kernel.service.CompanyLocalService;
import com.liferay.portal.kernel.workflow.WorkflowConstants;

import java.net.URI;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

@Component(
		configurationPid = "com.liferay.healthcheck.operation.internal.configuration.HealthcheckOperationalConfiguration",
		service=Healthcheck.class
)
public class ObjectWebhookCertificateValidationHealthcheck implements Healthcheck {

	@Override
	public Collection<HealthcheckItem> check(long companyId) throws Exception {
		LinkedList<HealthcheckItem> result = new LinkedList<HealthcheckItem>();
		String virtualHostname = _companyLocalService.getCompany(
				companyId
			).getVirtualHostname();

		HealthcheckOperationalConfiguration
		healthcheckOperationalConfiguration = null;
		if (companyId != CompanyConstants.SYSTEM) {
			healthcheckOperationalConfiguration =
				_configurationProvider.getCompanyConfiguration(
					HealthcheckOperationalConfiguration.class, companyId);
		}
		else {
			healthcheckOperationalConfiguration =
				_configurationProvider.getSystemConfiguration(
					HealthcheckOperationalConfiguration.class);
		}
		Set<String> hostWhitelist = new HashSet<>(
				Arrays.asList(
					healthcheckOperationalConfiguration.
						webhookHostWhitelist()));

		List<ObjectDefinition> objectDefinitions = _objectDefinitionLocalService.getObjectDefinitions(companyId, true, WorkflowConstants.STATUS_APPROVED);
		for (ObjectDefinition objectDefinition : objectDefinitions) {
			List<ObjectAction> objectActions = _objectActionLocalService.getObjectActions(objectDefinition.getObjectDefinitionId());
			for (ObjectAction objectAction : objectActions) {
				String objectActionExecutorKey = objectAction.getObjectActionExecutorKey();
				if(ObjectActionExecutorConstants.KEY_WEBHOOK.equals(objectActionExecutorKey)) {
					String sUrl = objectAction.getParametersUnicodeProperties().get("url");
					URL url = new URI(sUrl).toURL();
					if(hostWhitelist.contains(url.getHost())) {
						// Note: Our own virtualHost name is included in the result, so that the check 
						// can be explicitly ignored only until the virtualHost name changes (e.g. when 
						// a PRD backup is restored in UAT). (the UI allows ignoring certain output
						// based on unique message&parameters
						result.add(new HealthcheckItem(true, generateLink(objectAction), _MSG_WHITELISTED, objectDefinition.getName(), objectAction.getName(), url.getHost(), virtualHostname));
					} else {
						result.add(new HealthcheckItem(false, generateLink(objectAction), _MSG, objectDefinition.getName(), objectAction.getName(), url.getHost(), virtualHostname));
					}
					HttpsCertificateValidatorUtil.validateCertificate(url, companyId, generateLink(objectAction), result);
				}
			}
		}
		if(result.isEmpty()) {
			result.add(new HealthcheckItem(true, null, _MSG_NO_WEBHOOK));
		}
		return result;
	}

	private String generateLink(ObjectAction objectAction) {
		return BASE_LINK + objectAction.getObjectDefinitionId();
	}

	@Override
	public String getCategory() {
		return "healthcheck-category-operation";
	}

	@Reference
	private ObjectActionLocalService _objectActionLocalService;
	
	@Reference
	private ObjectDefinitionLocalService _objectDefinitionLocalService;

	private static String PID = "com_liferay_object_web_internal_object_definitions_portlet_ObjectDefinitionsPortlet";

	private static String BASE_LINK= "/group/control_panel/manage?p_p_id=" 
			+ PID + "&_"
			+ PID + "_mvcRenderCommandName=%2Fobject_definitions%2Fedit_object_definition&_"
			+ PID + "_screenNavigationCategoryKey=actions&_"
			+ PID + "_objectDefinitionId=";
	
	private static String _MSG_NO_WEBHOOK = "no-webhook-found-for-certificate-validation-check";

	private static String _MSG_WHITELISTED = "found-whitelisted-host-x-for-objectaction-webhook-x-x";

	private static String _MSG = "found-objectaction-webhook-x-x-configured-for-nonwhitelisted-host-name-x";
	
	@Reference
	private ConfigurationProvider _configurationProvider;

	@Reference
	private CompanyLocalService _companyLocalService;
}
