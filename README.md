# Healthcheck

This is a modernized implementation of various healthcheck modules.

Most attention has been given to implement the backend, and there's a very simplistic frontend

This project currently consists of:

* Breaking Changes implementations
    * e.g. with a copy from Liferay's portal-impl code (e.g. VerifyProperties, which currently runs only during upgrades)
* Best Practice implementations
    * e.g. "Do not use default user accounts & passwords"
* Operational implementations    
    * e.g. checks for available memory and redirection configuration
    * optionally detecting if a backup has been restored in a different system, based on the used hostnames
    * The optional checks depend on a filter that intercepts a low default number of first requests after 
    server-start and is deactivated afterwards (for performance reasons) 

## How to build

This is a standard Liferay Workspace. Clone, build and deploy

This repository is used as the backend for publishing an app on Liferay Marketplace, 
so you'll be able to have it work without building it in any way.

## Reference

* [LPD-253](https://liferay.atlassian.net/browse/LPD-253)

## Limitations

* Very basic permission checking (Commerce Health Checks implement this in a better way, for example in CommerceHealthStatusDisplayContext). Health Check UI is only available for Company Administrators.
* UI is veeeeery barebones right now 
* Only few native health checks implemented: Please contribute more, in code or just ideas
* Speaking of instances: Not much thought has been given to scenarios with multiple instances that might have different - instance specific - configuration.

## Ideas for more health checks

### Operational

* Validate that AVIF and WEBP image formats are fully handled (LPD-23363)

## Ideas for the UI

The UI is - explicitly - _very_ ugly. To make its ugliness even more explicit, it's minimally interactive and 100% built with `<table>`.

![UI Screenshot](healthcheck-ui.png) 

You can ignore some healthchecks (successful or unsuccessful). They'll still be executed, but the result will not be shown.
 
Collecting a few ideas that could go into UI features:

* Get rid of the <table> based layout to begin with
* Run healthchecks in background (scheduled) - this enables long running processes to run as well (see remark about locale though) 
* Filter/Sort results by category/result
* Activate/Deactivate certain categories (e.g. ignore demo-related healthchecks with released security) or pick individual checks to run and blacklist completely.

## Visualize Results on a site, instead of a Control Panel app

If you want to show the result of healthchecks to administrative users, you can do this with a fragment like the following.
In this case, it'll signal if healthchecks didn't run since server-restart, or if they had failed (and non-ignored) checks.
This sample fragment will render empty if it can't find any healthcheck or the components introduced.

	<div class="fragment_6101">
	[#if (healthcheckFailures > -2)!false]
	  [#if healthcheckFailures = -1]
	  	<div class="has-content">
		    <p class="text-danger token-border rounded-xxl">
	        This system features some healthchecks as a self-test mechanism.
	        Please
				  <a href="/group/guest/~/control_panel/manage?p_p_id=com_liferay_portal_health_web_portlet_HealthcheckWebPortlet">
	  				check if the system requires maintenance
	        </a>.
		    </p>
		    <p>
				  (This marker disappears when there are no more problems)
	    	</p>
		  </div>
	  [#elseif healthcheckFailures != 0]
		<div class="has-content">
			<p class="text-warning token-border token-sample rounded-xxl">
				Healthchecks signal ${healthcheckFailures} problem(s). Please 
				<a href="/group/guest/~/control_panel/manage?p_p_id=com_liferay_portal_health_web_portlet_HealthcheckWebPortlet">
					run them again
				</a>
				and fix (or ignore) them explicitly
			</p>
			<p>
				(This marker disappears when there are no more problems)
			</p>
		  </div>
	  [#else]
		<!-- No output, so that this fragment stays on a page in a template system -->
		<!-- without disturbing the page, but becomes active, when necessary -->
	  [/#if]
	[#else]
		<p class="text-danger has-content">
	 	  Healthcheck Status unknown. You may need to install an additional plugin,
			or check for the reason why it didn't deploy.
		</p>
	[/#if]
	</div>

CSS suggestion for the fragment:

	.fragment_6101 .has-content {
		border-color: var(--danger);
		border-style:dashed;
		padding: var(--spacer-2, 0.5rem) !important;
		margin-bottom: var(--spacer-5, 0.5rem) !important;
		font-size:1.5rem;
	}

