/**
 * SPDX-FileCopyrightText: (c) 2024 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.healthcheck;

import com.liferay.petra.string.StringUtil;
import com.liferay.portal.kernel.util.ArrayUtil;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

/**
 * @author Olaf Kock
 */
public class HealthcheckItem {

	public HealthcheckItem(
		boolean success, String[] additionalDocumentation, String link,
		String messageKey, Object... messageParameters) {

		_success = success;
		_link = link;
		_messageKey = messageKey;
		_messageParameters = messageParameters;

		_additionalDocumentation = Collections.unmodifiableCollection(
			Arrays.asList(additionalDocumentation));
	}

	public HealthcheckItem(
		boolean success, String link, String messageKey,
		Object... messageParameters) {

		_success = success;
		_link = link;
		_messageKey = messageKey;
		_messageParameters = messageParameters;

		_additionalDocumentation = Collections.emptyList();
	}

	/**
	 * A collection of links to Liferay Learn that document the current item's
	 * purpose, behavior or hints to avoid failure.
	 */
	public Collection<String> getAdditionalDocumentation() {
		return _additionalDocumentation;
	}

	/**
	 * A link(URL) that can contain further information on the tested condition
	 *
	 * @return a link URL
	 */
	public String getLink() {
		return _link;
	}

	/**
	 * A message key that describes what this healthcheck checks for
	 *
	 * @return human readable message
	 */
	public String getMessageKey() {
		return _messageKey;
	}

	/**
	 * Parameters that are used to look up the human readable message,
	 * with the messageKey.
	 * @return an array of parameters to be combined with the localized message
	 */
	public Object[] getMessageParameters() {
		return _messageParameters;
	}

	/**
	 * A machine readable key that uniquely identifies the current healthcheck
	 * and its result. It key can be used to ignore certain healthchecks,
	 * in case their test does not apply to a certain environment (example:
	 * Elasticsearch Sidecar is ok in local demo systems). Default content: The
	 * healthcheck's message localization key and all parameters, concatenated in
	 * a way to be usable as:
	 *
	 * @return the machine readable encoding for this healthcheck
	 */
	public String getSourceKey() {
		Object[] keys = new Object[_messageParameters.length + 1];

		ArrayUtil.combine(new Object[] {_messageKey}, _messageParameters, keys);

		return StringUtil.merge(ArrayUtil.toStringArray(keys), "-");
	}

	/**
	 * signals if the healthcheck result is healthy or not
	 *
	 * @return true if healthy
	 */
	public boolean isSuccess() {
		return _success;
	}

	private final Collection<String> _additionalDocumentation;
	private final String _link;
	private final String _messageKey;
	private final Object[] _messageParameters;
	private final boolean _success;

}
