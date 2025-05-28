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
import com.liferay.portal.kernel.service.CompanyLocalService;
import com.liferay.portal.util.PropsValues;

import java.util.Arrays;
import java.util.Collection;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * HealthCheck for relaxed-security Demo Systems Ensure that user updates don't
 * require to be approved by clicking a link that's mailed to the user. This is
 * due to demo systems rarely having a valid mail server set up.
 * 
 * @author Olaf Kock
 */

@Component(service = Healthcheck.class)

public class DontRequireMailForUserUpdatesHealthCheck extends RelaxedHealthcheckBaseImpl {

	private static final String LINK = "https://docs.liferay.com/portal/7.4-latest/propertiesdoc/portal.properties.html#Company";
	private static final String MSG = "relaxed-healthcheck-setting-strangers-need-to-be-validated-by-mail";

	@Override
	public Collection<HealthcheckItem> doCheck(long companyId) throws PortalException {
		boolean verifyStrangers = PropsValues.COMPANY_SECURITY_STRANGERS_VERIFY;
		return Arrays.asList(new HealthcheckItem(!verifyStrangers, LINK, MSG));
	}

	@Reference
	CompanyLocalService companyLocalService;
}
