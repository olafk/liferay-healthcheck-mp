/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.healthcheck.bestpractice.internal.compat;

import com.liferay.petra.reflect.ReflectionUtil;
import com.liferay.petra.string.CharPool;
import com.liferay.petra.string.StringBundler;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.util.Base64;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.Validator;

import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * edited-down to the necessary compatibility layer, as DXP-2025-Q1 and CE GA 132
 * differed in the PasswordEncryptor API
 */

public class PBKDF2PasswordEncryptor {
	public String getEncryptedPasswordAlgorithmSettings(
		String encryptedPassword) {

		try {
			int index = encryptedPassword.indexOf(CharPool.CLOSE_CURLY_BRACE);

			if (index < 0) {
				return null;
			}

			PBKDF2EncryptionConfiguration pbkdf2EncryptionConfiguration =
				new PBKDF2EncryptionConfiguration();

			pbkdf2EncryptionConfiguration.configure(
				StringPool.BLANK, encryptedPassword.substring(index + 1));

			return StringBundler.concat(
				encryptedPassword.substring(1, index), StringPool.FORWARD_SLASH,
				pbkdf2EncryptionConfiguration.getKeySize(),
				StringPool.FORWARD_SLASH,
				pbkdf2EncryptionConfiguration.getRounds());
		}
		catch (Exception pwdEncryptorException) {
			return ReflectionUtil.throwException(pwdEncryptorException);
		}
	}

	private static final int _KEY_SIZE = 160;

	private static final int _ROUNDS = 1300000;

	private static final Pattern _pattern = Pattern.compile(
		"^.*/?([0-9]+)?/([0-9]+)$");

	private static class PBKDF2EncryptionConfiguration {

		public void configure(String algorithm, String encryptedPassword)
			throws Exception {

			if (Validator.isNull(encryptedPassword)) {
				Matcher matcher = _pattern.matcher(algorithm);

				if (matcher.matches()) {
					_keySize = GetterUtil.getInteger(
						matcher.group(1), _KEY_SIZE);

					_rounds = GetterUtil.getInteger(matcher.group(2), _ROUNDS);
				}
			}
			else {
				ByteBuffer byteBuffer = ByteBuffer.wrap(
					Base64.decode(encryptedPassword));

				try {
					@SuppressWarnings("unused")
					int length = byteBuffer.remaining();

					_keySize = byteBuffer.getInt();
					_rounds = byteBuffer.getInt();
				}
				catch (BufferUnderflowException bufferUnderflowException) {
					throw new Exception(
						"Unable to extract salt from encrypted password",
						bufferUnderflowException);
				}
			}

			int index = algorithm.toUpperCase().indexOf("SHA");

			if (index < 0) {
				return;
			}
		}

		public int getKeySize() {
			return _keySize;
		}

		public int getRounds() {
			return _rounds;
		}

		private int _keySize = _KEY_SIZE;
		private int _rounds = _ROUNDS;
	}

}