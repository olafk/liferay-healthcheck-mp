package com.liferay.healthcheck.bestpractice.internal;

import com.liferay.healthcheck.Healthcheck;
import com.liferay.healthcheck.HealthcheckItem;
import com.liferay.osgi.service.tracker.collections.list.ServiceTrackerList;
import com.liferay.osgi.service.tracker.collections.list.ServiceTrackerListFactory;
import com.liferay.portal.kernel.template.TemplateContextContributor;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Modified;

@Component(
		service=Healthcheck.class
)
public class TemplateContextContributorRobustnessHealthcheck implements Healthcheck {

	private static final String[] ADDITIONAL_DOCUMENTATION = new String[] {"https://liferay.atlassian.net/browse/LPD-64125"};
	private static final String MSG = "templatecontextcontributors-implementations-seem-to-be-robust";
	private static final String MSG_PROBLEM = "problem-with-templatecontextcontributor-x-x-x";
	
	@Override
	public Collection<HealthcheckItem> check(long companyId) throws Exception {
		LinkedList<HealthcheckItem> result = new LinkedList<HealthcheckItem>();
		Map<String, Object> contextObjects = new HashMap<String, Object>();
		
		for (TemplateContextContributor templateContextContributor : _serviceTrackerList) {
			try {
				templateContextContributor.prepare(
					contextObjects, null);
			} catch(Throwable t) {
				result.add(
						new HealthcheckItem(
								false, 
								ADDITIONAL_DOCUMENTATION,
								null, 
								MSG_PROBLEM, 
								templateContextContributor.getClass().getName(), 
								t.getClass(), 
								t.getMessage()));
			}
		}
		if(result.isEmpty()) {
			result.add(
					new HealthcheckItem(
							true,
							ADDITIONAL_DOCUMENTATION,
							null, 
							MSG));
		}
		
		return result;
	}

	@Override
	public String getCategory() {
		return "healthcheck-category-bestpractice";
	}

	@Activate
	@Modified
	protected void activate(
			Map<String, Object> properties, BundleContext bundleContext) {
		_serviceTrackerList = ServiceTrackerListFactory.open(
			bundleContext, TemplateContextContributor.class,
			"(type=" + TemplateContextContributor.TYPE_GLOBAL + ")");
	}
	
	@Deactivate
	public void deactivate() {
		_serviceTrackerList.close();
	}

	private volatile ServiceTrackerList<TemplateContextContributor>
	_serviceTrackerList;
}
