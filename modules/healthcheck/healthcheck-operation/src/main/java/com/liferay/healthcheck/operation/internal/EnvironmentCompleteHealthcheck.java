/**
 * SPDX-FileCopyrightText: (c) 2025 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.healthcheck.operation.internal;

import com.liferay.application.list.PanelApp;
import com.liferay.healthcheck.Healthcheck;
import com.liferay.healthcheck.HealthcheckItem;

import java.util.Collection;
import java.util.LinkedList;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicyOption;

/**
 * Healthcheck for occasional PaaS Hickups that require service restart
 * Under certain circumstances, the PaaS SSA environment seems to come up incomplete
 * This healthcheck attempts to figure out the condition and warns if it's detected
 * So far, checking for the Product Management Control Panel UI has been shown as 
 * sufficient. Checking for (a large) number of unresolved modules would work as well,
 * but takes significantly longer time, so we'll stick with this extremely quick method.
 *   
 * @author Olaf Kock
 */

@Component( 
		service = Healthcheck.class 
)
public class EnvironmentCompleteHealthcheck implements Healthcheck {

	private static final String CP_DEFINITIONS_PANEL_APP = "com.liferay.commerce.product.definitions.web.internal.application.list.CPDefinitionsPanelApp";
	private static final String LINK = null;
	private static final String MSG = "healthcheck-environment-probably-completely-started";
	private static final String MSG_ERROR = "healthcheck-environment-probably-requires-restart";

	@Override
	public Collection<HealthcheckItem> check(long companyId) {
		LinkedList<HealthcheckItem> result = new LinkedList<>();

		result.add(new HealthcheckItem(_productsPanelApp > 0, LINK, _productsPanelApp > 0 ? MSG : MSG_ERROR));

		return result;
	}

	@Reference( 
			cardinality = ReferenceCardinality.MULTIPLE,
		    policyOption = ReferencePolicyOption.GREEDY,
		    unbind = "doUnRegister" 
	)
	void doRegister(PanelApp panelApp) {
		if(panelApp.getClass().getName().equals(CP_DEFINITIONS_PANEL_APP)) {
			_productsPanelApp++;
		}
	}
	
	void doUnRegister(PanelApp panelApp) {
		if(panelApp.getClass().getName().equals(CP_DEFINITIONS_PANEL_APP)) {
			_productsPanelApp--;
		}
	}
	
	@Override
	public String getCategory() {
		return "healthcheck-category-operation";
	}

	private int _productsPanelApp = 0;
}
