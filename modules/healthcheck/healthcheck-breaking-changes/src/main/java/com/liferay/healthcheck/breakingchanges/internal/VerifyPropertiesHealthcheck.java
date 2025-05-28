/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.healthcheck.breakingchanges.internal;

import com.liferay.healthcheck.Healthcheck;
import com.liferay.healthcheck.HealthcheckItem;
import com.liferay.healthcheck.breakingchanges.internal.copied.VerifyProperties2024q213;
import com.liferay.healthcheck.breakingchanges.internal.copied.VerifyProperties2024q313;
import com.liferay.healthcheck.breakingchanges.internal.copied.VerifyProperties2024q47;
import com.liferay.healthcheck.breakingchanges.internal.copied.VerifyProperties2025q12;
import com.liferay.healthcheck.breakingchanges.internal.copied.VerifyProperties2025q20;
import com.liferay.healthcheck.breakingchanges.internal.copied.VerifyPropertiesGA120;
import com.liferay.healthcheck.breakingchanges.internal.copied.VerifyPropertiesGA125;
import com.liferay.healthcheck.breakingchanges.internal.copied.VerifyPropertiesGA129;
import com.liferay.healthcheck.breakingchanges.internal.copied.VerifyPropertiesGA132;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.util.ReleaseInfo;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.osgi.service.component.annotations.Component;

@Component(service = Healthcheck.class)
public class VerifyPropertiesHealthcheck implements Healthcheck {

	@Override
	public Collection<HealthcheckItem> check(long companyId) throws PortalException {
		LinkedList<HealthcheckItem> result = new LinkedList<HealthcheckItem>();
		try {
			List<String> messages = Collections.emptyList();
			String version = ReleaseInfo.getVersionDisplayName().toLowerCase();
			
			if(version.startsWith("2025.q2")) {
				messages = VerifyProperties2025q20.verify();
				result.add(new HealthcheckItem(true, _ADDITIONAL_DOCUMENTATION, null, _MSG_EXACT_VERSION, "2025.q2", ReleaseInfo.getVersionDisplayName()));
			} else if(version.startsWith("2025.q1")) {
				messages = VerifyProperties2025q12.verify();
				result.add(new HealthcheckItem(true, _ADDITIONAL_DOCUMENTATION, null, _MSG_EXACT_VERSION, "2025.q1", ReleaseInfo.getVersionDisplayName()));
			} else if (version.startsWith("2024.q4")) {
				messages = VerifyProperties2024q47.verify();
				result.add(new HealthcheckItem(true, _ADDITIONAL_DOCUMENTATION, null, _MSG_EXACT_VERSION, "2024.q4", ReleaseInfo.getVersionDisplayName()));
			} else if (version.startsWith("2024.q3")) {
				messages = VerifyProperties2024q313.verify();
				result.add(new HealthcheckItem(true, _ADDITIONAL_DOCUMENTATION, null, _MSG_EXACT_VERSION, "2024.q3", ReleaseInfo.getVersionDisplayName()));
			} else if (version.startsWith("2024.q2")) {
				messages = VerifyProperties2024q213.verify();
				result.add(new HealthcheckItem(true, _ADDITIONAL_DOCUMENTATION, null, _MSG_EXACT_VERSION, "2024.q2", ReleaseInfo.getVersionDisplayName()));
			} else if (version.startsWith("7.4.3.129")) {
				messages = VerifyPropertiesGA129.verify();
				result.add(new HealthcheckItem(true, _ADDITIONAL_DOCUMENTATION, null, _MSG_EXACT_VERSION, "7.4 CE GA129", ReleaseInfo.getVersionDisplayName()));
			} else if (version.startsWith("7.4.3.132")) {
				messages = VerifyPropertiesGA132.verify();
				result.add(new HealthcheckItem(true, _ADDITIONAL_DOCUMENTATION, null, _MSG_EXACT_VERSION, "7.4 CD GA132", ReleaseInfo.getVersionDisplayName()));
			} else if (version.startsWith("7.4.3.125")) {
				messages = VerifyPropertiesGA125.verify();
				result.add(new HealthcheckItem(true, _ADDITIONAL_DOCUMENTATION, null, _MSG_EXACT_VERSION, "7.4 CD GA125", ReleaseInfo.getVersionDisplayName()));
			} else if (version.startsWith("7.4.3.120")) {
				messages = VerifyPropertiesGA120.verify();
				result.add(new HealthcheckItem(true, _ADDITIONAL_DOCUMENTATION, null, _MSG_EXACT_VERSION, "7.4 CD GA120", ReleaseInfo.getVersionDisplayName()));
			} else /* TODO: Implement more checks for other versions */ {
				messages = VerifyProperties2025q12.verify();
				result.add(new HealthcheckItem(false, _ADDITIONAL_DOCUMENTATION, null, _MSG_UNIMPLEMENTED_VERSION, version,  "2025.q1"));
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
