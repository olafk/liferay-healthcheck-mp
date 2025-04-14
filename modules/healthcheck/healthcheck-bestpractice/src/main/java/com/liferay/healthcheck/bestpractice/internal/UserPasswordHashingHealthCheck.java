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

package com.liferay.healthcheck.bestpractice.internal;

import com.liferay.configuration.admin.constants.ConfigurationAdminPortletKeys;
import com.liferay.healthcheck.Healthcheck;
import com.liferay.healthcheck.HealthcheckItem;
import com.liferay.healthcheck.bestpractice.internal.compat.CompatPasswordEncryptorUtil;
import com.liferay.healthcheck.bestpractice.internal.configuration.HealthcheckBestPracticeConfiguration;
import com.liferay.petra.string.StringBundler;
import com.liferay.portal.configuration.module.configuration.ConfigurationProvider;
import com.liferay.portal.kernel.model.CompanyConstants;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.module.configuration.ConfigurationException;
import com.liferay.portal.kernel.service.UserLocalService;
import com.liferay.portal.kernel.util.PropsKeys;
import com.liferay.portal.kernel.util.PropsUtil;
import com.liferay.portal.kernel.util.comparator.UserEmailAddressComparator;
import com.liferay.portal.kernel.workflow.WorkflowConstants;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * Check for appropriate strength of password hashing algorithms. Includes
 * testing a sample of all users (amount is configurable) for the hashing
 * algorithm that's actually in use. Liferay will upgrade the password hash
 * during log in, when it has access to the clear-text password - simply logging
 * in (locally, not on SSO) is sufficient to update the hash
 *
 * @author Olaf Kock
 */
@Component(configurationPid = "com.liferay.healthcheck.bestpractice.internal.configuration.HealthcheckBestPracticeConfiguration", 
	service = Healthcheck.class)
public class UserPasswordHashingHealthCheck implements Healthcheck {

	@Override
	public Collection<HealthcheckItem> check(long companyId) throws ConfigurationException {

		String hashingAlgorithm = PropsUtil.get(PropsKeys.PASSWORDS_ENCRYPTION_ALGORITHM).toUpperCase();
		List<HealthcheckItem> result = new LinkedList<>();

		if (hashingAlgorithm.startsWith(_BCRYPT)) {
			int workFactor = Integer.valueOf(hashingAlgorithm.substring("BCRYPT/".length()));

			result.add(new HealthcheckItem(workFactor >= _OWASP_BCRYPT_WORKFACTOR, _ADDITIONAL_DOCUMENTATION, null,
					"default-hashing-bcrypt-should-use-workfactor-x-detected-x", _OWASP_BCRYPT_WORKFACTOR,
					hashingAlgorithm));
		} else if (hashingAlgorithm.startsWith(_PBKDF2_WITH_HMAC_SHA1)) {
			int slashPos = hashingAlgorithm.indexOf('/', _PBKDF2_WITH_HMAC_SHA1.length() + 1);
			int workFactor = Integer.valueOf(hashingAlgorithm.substring(slashPos + 1));

			result.add(new HealthcheckItem(workFactor >= _OWASP_PBKDF2_WITH_HMAC_SHA1_WORKFACTOR,
					_ADDITIONAL_DOCUMENTATION, null,
					"default-hashing-pbkdf2withhmacsha1-should-use-workfactor-x-detected-x",
					_OWASP_PBKDF2_WITH_HMAC_SHA1_WORKFACTOR, hashingAlgorithm));

		} else if (hashingAlgorithm.startsWith("MD") || hashingAlgorithm.startsWith("SHA")
				|| hashingAlgorithm.startsWith("NONE")) {

			result.add(new HealthcheckItem(false, _ADDITIONAL_DOCUMENTATION, null, "insecure-hashing-algorithm-x",
					hashingAlgorithm));
		} else {
			result.add(new HealthcheckItem(false, _ADDITIONAL_DOCUMENTATION, null, "unknown-hashing-algorithm-x",
					hashingAlgorithm));
		}

		HealthcheckBestPracticeConfiguration healthcheckBestPracticeConfiguration = null;

		if (companyId != CompanyConstants.SYSTEM) {
			healthcheckBestPracticeConfiguration = _configurationProvider
					.getCompanyConfiguration(HealthcheckBestPracticeConfiguration.class, companyId);
		} else {
			healthcheckBestPracticeConfiguration = _configurationProvider
					.getSystemConfiguration(HealthcheckBestPracticeConfiguration.class);
		}

		int technicalUserCount = 0;
		HashMap<String, Long> algorithms = new HashMap<>();

		algorithms.put(hashingAlgorithm, 0L);

		long usersCount = Math.min(_userLocalService.getUsersCount(companyId, WorkflowConstants.STATUS_APPROVED),
				healthcheckBestPracticeConfiguration.maxUsersTestedForHashingAlgorithm());

		int pageSize = (int) Math.min(usersCount, 100);

		for (int i = 0; i < (usersCount / pageSize); i++) {
			List<User> users;

			try {
				users = _userLocalService.getUsers(companyId, WorkflowConstants.STATUS_APPROVED, i * pageSize,
						(i + 1) * pageSize, UserEmailAddressComparator.getInstance(true));

				for (User user : users) {
					String pwd = user.getPassword();
					if (!(user.isServiceAccountUser() || user.isOnDemandUser()
							|| user.getEmailAddress().equalsIgnoreCase("analytics.administrator@liferay.com"))) {
						String algorithm = CompatPasswordEncryptorUtil.getEncryptedPasswordAlgorithmSettings(pwd);

						_countUp(algorithms, algorithm);
					} else {
						technicalUserCount++;
					}
				}
			} catch (Throwable e) {
				_countUp(algorithms, StringBundler.concat(e.getClass().getName(), " ", e.getMessage()));
			}
		}

		Set<String> whitelistedAlgorithms = new HashSet<>(
				Arrays.asList(healthcheckBestPracticeConfiguration.passwordHashingAlgorithmWhitelist()));

		for (HashMap.Entry<String, Long> entry : algorithms.entrySet()) {
			if (entry.getKey().equalsIgnoreCase(hashingAlgorithm)) {

				result.add(new HealthcheckItem(entry.getValue() > 0, _ADDITIONAL_DOCUMENTATION, _LINK,
						"x-out-of-x-users-use-default-hashing-algorithm-x", entry.getValue(), usersCount,
						hashingAlgorithm));
			} else if (whitelistedAlgorithms.contains(entry.getKey())) {
				result.add(new HealthcheckItem(true, _ADDITIONAL_DOCUMENTATION, _LINK,
						"x-out-of-x-users-use-whitelisted-hashing-algorithm-x", entry.getValue(), usersCount,
						entry.getKey()));
			} else {
				result.add(new HealthcheckItem(false, _ADDITIONAL_DOCUMENTATION, _LINK,
						"x-out-of-x-users-dont-use-hashing-algorithm-x-but-x", entry.getValue(), usersCount,
						hashingAlgorithm, entry.getKey()));
			}
		}

		if (technicalUserCount > 0) {
			result.add(new HealthcheckItem(true, _ADDITIONAL_DOCUMENTATION, _LINK,
					"x-out-of-x-user-accounts-are-technical-users-their-hashed-password-has-not-been-checked",
					technicalUserCount, usersCount));
		}

		return result;
	}

	@Override
	public String getCategory() {
		return "healthcheck-category-bestpractice";
	}

	private void _countUp(Map<String, Long> algorithms, String algorithm) {
		Long currentValue = algorithms.get(algorithm);

		if (currentValue == null) {
			currentValue = Long.valueOf(0);
		}

		currentValue++;
		algorithms.put(algorithm, currentValue);
	}

	private static final String[] _ADDITIONAL_DOCUMENTATION = {
			"https://cheatsheetseries.owasp.org/cheatsheets" + "/Password_Storage_Cheat_Sheet.html",
			"https://liferay.dev/blogs/-/blogs/hashing-performance" };

	private static final String _BCRYPT = "BCRYPT";

	private static final String _CFG = HealthcheckBestPracticeConfiguration.class.getName();

	private static final String _LINK = StringBundler.concat("/group/control_panel/manage?p_p_id=",
			ConfigurationAdminPortletKeys.SYSTEM_SETTINGS, "&_",
			ConfigurationAdminPortletKeys.SYSTEM_SETTINGS,
			"_mvcRenderCommandName=%2Fconfiguration_admin%2Fedit_configuration&_",
			ConfigurationAdminPortletKeys.SYSTEM_SETTINGS, "_factoryPid=", _CFG,
			"&_", ConfigurationAdminPortletKeys.SYSTEM_SETTINGS, "_pid=", _CFG);

	/**
	 * OWASP recommendation as of March 2025: 10. Update when appropriate
	 */
	private static final int _OWASP_BCRYPT_WORKFACTOR = 10;

	/**
	 * OWASP recommendation as of March 2025: 1300000. Update when appropriate
	 */
	private static final int _OWASP_PBKDF2_WITH_HMAC_SHA1_WORKFACTOR = 1300000;

	private static final String _PBKDF2_WITH_HMAC_SHA1 = "PBKDF2WITHHMACSHA1";

	@Reference
	private ConfigurationProvider _configurationProvider;

	@Reference
	private UserLocalService _userLocalService;
}
