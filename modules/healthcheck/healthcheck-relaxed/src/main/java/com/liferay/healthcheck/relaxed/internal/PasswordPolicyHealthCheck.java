/**
 * Copyright (c) 2022-present Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

package com.liferay.healthcheck.relaxed.internal;

import com.liferay.healthcheck.Healthcheck;
import com.liferay.healthcheck.HealthcheckItem;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.model.PasswordPolicy;
import com.liferay.portal.kernel.service.PasswordPolicyLocalService;

import java.util.Arrays;
import java.util.Collection;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * HealthCheck for relaxed-security Demo Systems When an administrator resets a
 * user's password, the default is to require a user to change the password
 * again. In demo systems, administrators and users are typically the same
 * people, so a password would need to be reset twice, which just slows down a
 * demo.
 * 
 * @author Olaf Kock
 */

@Component(service = Healthcheck.class)
public class PasswordPolicyHealthCheck extends RelaxedHealthcheckBaseImpl {

	private static final String LINK = "/group/control_panel/manage?p_p_id=com_liferay_password_policies_admin_web_portlet_PasswordPoliciesAdminPortlet";
	private static final String MSG = "relaxed-healthcheck-setting-password-policy-change-required";
	private static final String LINK_PARAM = "&p_p_lifecycle=0&_com_liferay_password_policies_admin_web_portlet_PasswordPoliciesAdminPortlet_mvcPath=%2Fedit_password_policy.jsp&_com_liferay_password_policies_admin_web_portlet_PasswordPoliciesAdminPortlet_passwordPolicyId=";

	@Override
	public Collection<HealthcheckItem> doCheck(long companyId) throws PortalException {
		PasswordPolicy passwordPolicy = passwordPolicyLocalService.getDefaultPasswordPolicy(companyId);
		boolean changeRequired = passwordPolicy.getChangeRequired();
		return Arrays.asList(new HealthcheckItem(!changeRequired, LINK + LINK_PARAM + passwordPolicy.getPasswordPolicyId(), MSG));
	}

	@Reference
	PasswordPolicyLocalService passwordPolicyLocalService;
}
