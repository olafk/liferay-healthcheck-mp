package com.liferay.healthcheck.bestpractice.internal.compat;

/**
 * edited-down to the necessary compatibility layer, as DXP-2025-Q1 and CE GA 132
 * differed in the PasswordEncryptor API
 */

public class CompatPasswordEncryptorUtil {
	public static String getEncryptedPasswordAlgorithmSettings(String encryptedPassword) {
		int index = encryptedPassword.indexOf('}');

		if (index < 0) {
			return null;
		}

		String algorithm = encryptedPassword.substring(1, index);
		if(algorithm.equals("PBKDF2")) {
			return new PBKDF2PasswordEncryptor().getEncryptedPasswordAlgorithmSettings(encryptedPassword);
		} else if(algorithm.equals("BCRYPT")) {
			return new BCryptPasswordEncryptor().getEncryptedPasswordAlgorithmSettings(encryptedPassword);
		} else {
			return algorithm;
		}
	}

}
