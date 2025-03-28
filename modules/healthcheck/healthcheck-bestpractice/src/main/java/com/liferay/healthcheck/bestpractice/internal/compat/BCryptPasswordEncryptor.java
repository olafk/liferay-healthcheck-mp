/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.healthcheck.bestpractice.internal.compat;

import com.liferay.petra.string.CharPool;
import com.liferay.petra.string.StringBundler;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * edited-down to the necessary compatibility layer, as DXP-2025-Q1 and CE GA 132
 * differed in the PasswordEncryptor API
 */

public class BCryptPasswordEncryptor {

	public String getEncryptedPasswordAlgorithmSettings(
		String encryptedPassword) {

		int index = encryptedPassword.indexOf(CharPool.CLOSE_CURLY_BRACE);

		if (index < 0) {
			return null;
		}

		String rounds = String.valueOf(_ROUNDS);

		Matcher matcher = _encryptedPasswordPattern.matcher(encryptedPassword);

		if (matcher.find()) {
			rounds = matcher.group(1);
		}

		return StringBundler.concat(
			encryptedPassword.substring(1, index), CharPool.FORWARD_SLASH,
			rounds);
	}

	private static final int _ROUNDS = 10;

	private static final Pattern _encryptedPasswordPattern = Pattern.compile(
		"\\{BCrypt}\\$2a\\$(\\d+)\\$", Pattern.CASE_INSENSITIVE);

}