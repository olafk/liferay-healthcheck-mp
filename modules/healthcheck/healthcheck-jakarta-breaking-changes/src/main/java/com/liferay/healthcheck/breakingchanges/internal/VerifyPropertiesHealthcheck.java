/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.healthcheck.breakingchanges.internal;

import com.liferay.healthcheck.Healthcheck;
import com.liferay.healthcheck.HealthcheckItem;
import com.liferay.healthcheck.breakingchanges.internal.copied.VerifyProperties2025q35;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.util.ReleaseInfo;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;

import org.osgi.service.component.annotations.Component;

@Component(service = Healthcheck.class)
public class VerifyPropertiesHealthcheck implements Healthcheck {

	@Override
	public Collection<HealthcheckItem> check(long companyId) throws PortalException {
		LinkedList<HealthcheckItem> result = new LinkedList<HealthcheckItem>();
		try {
			Collection<String> messages = Collections.emptyList();
			String version = ReleaseInfo.getVersionDisplayName().toLowerCase();
			
			if(version.startsWith("2025.q3")) {
				VerifyProperties2025q35 verifyProperties2025q30 = new VerifyProperties2025q35();
				verifyProperties2025q30.doVerify();
				messages = verifyProperties2025q30.getLastMessages();
				result.add(new HealthcheckItem(true, _ADDITIONAL_DOCUMENTATION, null, _MSG_EXACT_VERSION, "2025.q3", ReleaseInfo.getVersionDisplayName()));
			} else /* TODO: Implement more checks for other versions */ {
				VerifyProperties2025q35 verifyProperties2025q30 = new VerifyProperties2025q35();
				verifyProperties2025q30.doVerify();
				messages = verifyProperties2025q30.getLastMessages();
				result.add(new HealthcheckItem(false, _ADDITIONAL_DOCUMENTATION, null, _MSG_UNIMPLEMENTED_VERSION, version,  "2025.q3"));
			}
			 			
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
	private String _MSG_EXACT_VERSION="healthcheck-verify-properties-used-from-version-x";
	private String _MSG_UNIMPLEMENTED_VERSION="healthcheck-verify-properties-not-implemented-for-x-using-default-x";
	
}
