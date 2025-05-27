package com.liferay.healthcheck.operation.internal;

import com.liferay.healthcheck.Healthcheck;
import com.liferay.healthcheck.HealthcheckItem;
import com.liferay.healthcheck.operation.internal.configuration.HealthcheckOperationalSystemConfiguration;
import com.liferay.portal.configuration.metatype.bnd.util.ConfigurableUtil;
import com.liferay.portal.kernel.cluster.ClusterExecutorUtil;
import com.liferay.portal.kernel.cluster.ClusterNode;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.util.PropsValues;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;

@Component(
		configurationPid = "com.liferay.healthcheck.operation.internal.configuration.HealthcheckOperationalSystemConfiguration",
		service=Healthcheck.class
)
public class ClusterSizeHealthcheck implements Healthcheck {

	@Override
	public Collection<HealthcheckItem> check(long companyId) throws Exception {
		LinkedList<HealthcheckItem> result = new LinkedList<HealthcheckItem>();

		if(PropsValues.CLUSTER_LINK_ENABLED) {
			List<ClusterNode> clusterNodes = ClusterExecutorUtil.getClusterNodes();
			result.add(new HealthcheckItem(
					_expectedClusterSize <= clusterNodes.size(), _ADDITIONAL_INFO,
					null, _MSG, _expectedClusterSize, clusterNodes.size()));
		} else {
			result.add(new HealthcheckItem(_expectedClusterSize <= 1, 
					_ADDITIONAL_INFO, null, _MSG_NO_CLUSTER, _expectedClusterSize));
		}
		
		return result;
	}
	
	@Override
	public String getCategory() {
		return "healthcheck-category-operation";
	}

	@Activate
	@Modified
	protected void activate(Map<String, Object> properties) {
		if (_log.isDebugEnabled()) {
			_log.debug("Activating");
		}

		_expectedClusterSize = ConfigurableUtil.createConfigurable(
			HealthcheckOperationalSystemConfiguration.class, properties
		).expectedClusterSize();
	}

	private int _expectedClusterSize;
	
	private final String _MSG = "expected-minimal-clustersize-x-actual-x";
	private final String _MSG_NO_CLUSTER = "expected-minimal-clustersize-x-actual-could-not-be-determined";
	private final String[] _ADDITIONAL_INFO = {"https://learn.liferay.com/web/guest/w/dxp/installation-and-upgrades/setting-up-liferay/clustering-for-high-availability/configuring-cluster-link"};
	private static final Log _log = LogFactoryUtil.getLog(
			ClusterSizeHealthcheck.class);

}
