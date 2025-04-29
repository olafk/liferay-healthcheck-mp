package com.liferay.healthcheck.relaxed.internal;

import com.liferay.healthcheck.Healthcheck;
import com.liferay.healthcheck.HealthcheckItem;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import org.osgi.service.component.annotations.Component;

@Component(service=Healthcheck.class)
public class RelaxedHealthcheckIndicator extends RelaxedHealthcheckBaseImpl {

	@Override
	public Collection<HealthcheckItem> doCheck(long companyId) throws Exception {
		if(!isShow(companyId)) {
			return Arrays.asList(new HealthcheckItem(true, null, "you can activate checks for a more relaxed security setup, e.g. if this is a development system"));
		} else {
			return Collections.emptyList();
		}
	}

	protected boolean isConditional() {
		return false;
	}

}
