/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.healthcheck.bestpractice.internal.compat;

import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.util.OrderByComparator;

/**
 * @author Brian Wing Shun Chan
 */
public class UserEmailAddressComparator extends OrderByComparator<User> {

	public static final String ORDER_BY_ASC = "emailAddress ASC";

	public static final String ORDER_BY_DESC = "emailAddress DESC";

	public static final String[] ORDER_BY_FIELDS = {"emailAddress"};

	public static UserEmailAddressComparator getInstance(boolean ascending) {
		if (ascending) {
			return _INSTANCE_ASCENDING;
		}

		return _INSTANCE_DESCENDING;
	}

	@Override
	public int compare(User user1, User user2) {
		String emailAddress1 = user1.getEmailAddress();
		String emailAddress2 = user2.getEmailAddress();

		int value = emailAddress1.compareTo(emailAddress2);

		if (_ascending) {
			return value;
		}

		return -value;
	}

	@Override
	public String getOrderBy() {
		if (_ascending) {
			return ORDER_BY_ASC;
		}

		return ORDER_BY_DESC;
	}

	@Override
	public String[] getOrderByFields() {
		return ORDER_BY_FIELDS;
	}

	@Override
	public boolean isAscending() {
		return _ascending;
	}

	private UserEmailAddressComparator(boolean ascending) {
		_ascending = ascending;
	}

	private static final UserEmailAddressComparator _INSTANCE_ASCENDING =
		new UserEmailAddressComparator(true);

	private static final UserEmailAddressComparator _INSTANCE_DESCENDING =
		new UserEmailAddressComparator(false);

	private final boolean _ascending;

}