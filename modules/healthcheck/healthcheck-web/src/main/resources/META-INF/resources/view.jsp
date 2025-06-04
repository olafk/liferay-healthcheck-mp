<%--
/**
 * SPDX-FileCopyrightText: (c) 2024 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */
--%>

<%@ include file="/init.jsp" %>
<%!

private String shorten(String url) {
	try {
		// any incoming URL not in the expected format 
		// (e.g. triggering StringIndexOutOfBoundsException or any other) 
		// will be handled in the catch-block. Good enough...
		String page = url.substring(url.lastIndexOf('/'));
		if(page.indexOf('?')>-1) page = page.substring(0, page.indexOf('?'));
		if(page.indexOf('#')>-1) page = page.substring(0, page.indexOf('#'));
		
		int index = url.indexOf('/');
		for(int i=0; i<2 && index >= 0; i++) {
			index = url.indexOf('/', index+1);
		}
		if(index < 0 || url.indexOf('/', index+1) == -1) {
			// no more than 4 slashes in URL: return unchanged
			return url;
		} else {
			// more than 4 slashes in URL: Shorten
			return url.substring(0, index+1) + "..." + page;
		}
	} catch(Exception e) {
		return url;
	}
}

%>
<div class="container-fluid container-fluid-max-xl sheet" style="">
	<p>
		<b><liferay-ui:message key="healthcheck-web.caption" /></b>
	</p>

	<%
	List<LocalizedHealthcheckItem> checks = (List<LocalizedHealthcheckItem>)renderRequest.getAttribute("localizedHealthchecks");
	int numberOfIgnoredHealthchecks = (int)renderRequest.getAttribute("numberOfIgnoredHealthchecks");
	Set<String> theIgnoredChecks = (Set<String>)renderRequest.getAttribute("ignoredHealthchecks");
	%>

	<div class="align-items-lg-start align-items-md-start align-items-sm-start align-items-start flex-lg-row flex-md-row flex-row flex-sm-row row">
		<div class="col col-12 col-lg-3 col-md-4 col-sm-12"></div>
		<div class="col col-6 col-lg-2 col-md-2 col-sm-4" style="text-align: center;">
			<div style="font-size: 48px;">
				<clay:icon
					symbol="exclamation-circle"
				/>

				<br />
				<%= (int)renderRequest.getAttribute("numberOfFailedHealthchecks") %>
			</div>

			<liferay-ui:message key="failed" />
		</div>

		<div class="col col-6 col-lg-2 col-md-2 col-sm-4" style="text-align: center;">
			<div style="font-size: 48px;">
				<clay:icon
					symbol="check-circle"
				/>

				<br />
				<%= (int)renderRequest.getAttribute("numberOfSucceededHealthchecks") %>
			</div>

			<liferay-ui:message key="succeeded-checks" />
		</div>

		<div class="col col-6 col-lg-2 col-md-2 col-sm-4" style="text-align: center;">
			<div style="font-size: 48px;">
				<clay:icon
					symbol="folder"
				/>

				<br />
				<%= (int)renderRequest.getAttribute("numberOfIgnoredHealthchecks") %>
			</div>

			<liferay-ui:message key="ignored" />
		</div>

		<div class="col col-12 col-lg-3 col-md-4 col-sm-12"></div>
	</div>

	<table>

		<%
		for (LocalizedHealthcheckItem check : checks) {
			String style = check.isSuccess() ? "" : "font-weight:bold;";

			if (theIgnoredChecks.contains(check.getSourceKey())) {
				style += "opacity:0.5;";
			}

			String symbol = check.isSuccess() ? "check-circle" : "exclamation-circle";
		%>

		<tr style="border: 1px solid grey; <%= style %>">
			<td style="min-width: 3em; text-align: center;"><clay:icon
					symbol="<%= symbol %>" /></td>
			<td><%= check.getCategory() %></td>
			<td style="overflow-wrap: anywhere;">
				<%= check.getMessage() %>
				<c:if test="<%= check.getAdditionalDocumentation().size() > 0 %>">
				<ul>
					<%
					for(String url: check.getAdditionalDocumentation()) {
					%>
						<li>
							<a href="<%=url%>"><%=shorten(url)%></a> 
						</li>
					<% } %>
				</ul>
				</c:if>
			</td>
			<td style="padding: 2px; word-wrap: normal;">

				<%
				if (check.getLink() != null) {
					out.write("(<a href=\"" + check.getLink() + "\" target=\"_blank\">hint</a>)");
				}
				else {
					out.write("(no&nbsp;hint)");
				}
				%>

			</td>
			<td style="padding: 2px;"><aui:button-row>

			<c:choose>
				<c:when test="<%= theIgnoredChecks.contains(check.getSourceKey()) %>">

						<portlet:actionURL name="unignoreMessage" var="unignoreAction">
							<portlet:param name="unignore" value="<%= check.getSourceKey() %>" />
						</portlet:actionURL>

						<aui:button onClick="<%= unignoreAction %>" value="unignore[command]" />

				</c:when>
				<c:otherwise>

						<portlet:actionURL name="ignoreMessage" var="ignoreAction">
							<portlet:param name="ignore" value="<%= check.getSourceKey() %>" />
						</portlet:actionURL>

						<aui:button onClick="<%= ignoreAction %>" value="ignore[command]" />

				</c:otherwise>
			</c:choose>

				</aui:button-row>
			</td>
		</tr>

		<%
		}
		%>

	</table>

	<c:if test="<%= numberOfIgnoredHealthchecks > 0 %>">

	<aui:button-row>
		<div style="margin-top: 2rem;">
			<portlet:actionURL name="resetIgnore" var="resetIgnoreAction" />

			<aui:button onClick="<%= resetIgnoreAction %>" value="reset-ignore" />

			<portlet:renderURL var="showIgnored">
				<portlet:param name="showIgnored" value="true" />
			</portlet:renderURL>

			<aui:button onClick="<%= showIgnored %>" value="show-ignored-checks" />
		</div>
	</aui:button-row>
	</c:if>
</div>
