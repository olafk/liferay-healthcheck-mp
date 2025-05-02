package com.liferay.healthcheck.operation.internal;

import com.liferay.healthcheck.Healthcheck;
import com.liferay.healthcheck.HealthcheckItem;
import com.liferay.healthcheck.operation.internal.auxiliary.HttpsCertificateValidatorUtil;
import com.liferay.object.constants.ObjectActionExecutorConstants;
import com.liferay.object.model.ObjectAction;
import com.liferay.object.model.ObjectDefinition;
import com.liferay.object.service.ObjectActionLocalService;
import com.liferay.object.service.ObjectDefinitionLocalService;
import com.liferay.portal.kernel.workflow.WorkflowConstants;

import java.net.URI;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

@Component(
		service=Healthcheck.class
)
public class ObjectWebhookCertificateValidationHealthcheck implements Healthcheck {

	@Override
	public Collection<HealthcheckItem> check(long companyId) throws Exception {
		LinkedList<HealthcheckItem> result = new LinkedList<HealthcheckItem>();
		List<ObjectDefinition> objectDefinitions = _objectDefinitionLocalService.getObjectDefinitions(companyId, true, WorkflowConstants.STATUS_APPROVED);
		for (ObjectDefinition objectDefinition : objectDefinitions) {
			List<ObjectAction> objectActions = _objectActionLocalService.getObjectActions(objectDefinition.getObjectDefinitionId());
			for (ObjectAction objectAction : objectActions) {
				String objectActionExecutorKey = objectAction.getObjectActionExecutorKey();
				if(ObjectActionExecutorConstants.KEY_WEBHOOK.equals(objectActionExecutorKey)) {
					String url = objectAction.getParametersUnicodeProperties().get("url");
					HttpsCertificateValidatorUtil.validateCertificate(new URI(url).toURL(), companyId, generateLink(objectAction), result);
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
}
