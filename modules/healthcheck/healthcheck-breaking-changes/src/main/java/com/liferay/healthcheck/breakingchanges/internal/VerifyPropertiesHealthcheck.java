/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.healthcheck.breakingchanges.internal;

import com.liferay.healthcheck.Healthcheck;
import com.liferay.healthcheck.HealthcheckItem;
import com.liferay.healthcheck.breakingchanges.internal.copied.VerifyProperties2025q12;
import com.liferay.portal.kernel.exception.PortalException;

import java.util.Collection;
import java.util.LinkedList;

import org.osgi.service.component.annotations.Component;

@Component(service = Healthcheck.class)
public class VerifyPropertiesHealthcheck implements Healthcheck {

	@Override
	public Collection<HealthcheckItem> check(long companyId) throws PortalException {
		LinkedList<HealthcheckItem> result = new LinkedList<HealthcheckItem>();
		try {
			LinkedList<String> messages = VerifyProperties2025q12.verify();
			if(messages.isEmpty()) {
				result.add(new HealthcheckItem(true, _ADDITIONAL_DOCUMENTATION, null, _MSG));
			}
			for (String message : messages) {
				result.add(new HealthcheckItem(false, _ADDITIONAL_DOCUMENTATION, null, _MSG_FAILED, message));
			}
		} catch (Exception e) {
			result.add(new HealthcheckItem(false, _ADDITIONAL_DOCUMENTATION, null, _MSG_FAILED, e.getClass().getName() + " " + e.getMessage()));
		}
		return result;
	}

	@Override
	public String getCategory() {
		return "healthcheck-category-breakingchanges";
	}

	private String[] _ADDITIONAL_DOCUMENTATION = {
			"https://github.com/liferay/liferay-portal/blob/master/portal-impl/src/com/liferay/portal/verify/VerifyProperties.java"
	};
	private String _MSG="healthcheck-verify-properties-success";
	private String _MSG_FAILED="healthcheck-verify-properties-message-x";
}
