/**
 * SPDX-FileCopyrightText: (c) 2024 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.healthcheck.web.internal.portlet;

import java.util.Collection;

/**
 * @author Olaf Kock
 */
public class LocalizedHealthcheckItem {

	public LocalizedHealthcheckItem(
		boolean success, String category, String message, String link,
		String sourceKey, Collection<String> additionalDocumentation) {

		_success = success;
		_category = category;
		_message = message;
		_additionalDocumentation = additionalDocumentation;
		_link = link;
		_sourceKey = sourceKey;
	}

	public String getCategory() {
		return _category;
	}

	public Collection<String> getAdditionalDocumentation() {
		return _additionalDocumentation;
	}
	
	public String getLink() {
		return _link;
	}

	public String getMessage() {
		return _message;
	}

	public String getSourceKey() {
		return _sourceKey;
	}

	public boolean isSuccess() {
		return _success;
	}

	private final String _category;
	private final Collection<String> _additionalDocumentation;
	private final String _link;
	private final String _message;
	private final String _sourceKey;
	private final boolean _success;

}
