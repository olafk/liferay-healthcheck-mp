package com.liferay.healthcheck.web.internal.template;


import com.liferay.healthcheck.web.internal.registry.HealthcheckRegistry;
import com.liferay.portal.kernel.template.TemplateContextContributor;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

@Component(
		immediate=true,
		property = {
				"type=" + TemplateContextContributor.TYPE_GLOBAL,
				"service.ranking:Integer=1"
		},
		service=TemplateContextContributor.class
)

public class HealthcheckTemplateContextContributor implements TemplateContextContributor {

    @Override
	public void prepare(Map<String, Object> contextObjects, HttpServletRequest httpServletRequest) {
        contextObjects.put("healthcheckFailures", _healthcheckRegistry.getHealthcheckResult());
    }


	@Reference 
	private HealthcheckRegistry _healthcheckRegistry;

}
