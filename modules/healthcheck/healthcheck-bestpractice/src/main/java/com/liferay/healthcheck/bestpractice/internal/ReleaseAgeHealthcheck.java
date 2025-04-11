package com.liferay.healthcheck.bestpractice.internal;

import com.liferay.configuration.admin.constants.ConfigurationAdminPortletKeys;
import com.liferay.healthcheck.Healthcheck;
import com.liferay.healthcheck.HealthcheckItem;
import com.liferay.healthcheck.bestpractice.internal.configuration.HealthcheckBestPracticeConfiguration;
import com.liferay.healthcheck.bestpractice.internal.release.ReleaseInformation;
import com.liferay.healthcheck.bestpractice.internal.release.ReleaseResolver;
import com.liferay.petra.string.StringBundler;
import com.liferay.portal.configuration.metatype.bnd.util.ConfigurableUtil;
import com.liferay.portal.configuration.module.configuration.ConfigurationProvider;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.ReleaseInfo;

import java.io.IOException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;

@Component(
		configurationPid = "com.liferay.healthcheck.bestpractice.internal.configuration.HealthcheckBestPracticeConfiguration",
		service = Healthcheck.class
		)
public class ReleaseAgeHealthcheck implements Healthcheck {

	@Override
	public Collection<HealthcheckItem> check(long companyId) throws Exception {
		LinkedList<HealthcheckItem> result = new LinkedList<HealthcheckItem>();
		ReleaseInformation currentReleaseInformation = getCurrentReleaseInformation();

		if(currentReleaseInformation == null) {
			result.add(new HealthcheckItem(false, null, "can't find meta info for the currently running release"));
		} else {
			result.add(new HealthcheckItem(true, null, "current releasedate: " + currentReleaseInformation.getReleaseDate() + " " + currentReleaseInformation.productVersion));
		}

		LinkedList<ReleaseInformation> productGroupVersionInformation = getProductGroupVersionInformation(currentReleaseInformation.productGroupVersion);
		_log.debug("found " + productGroupVersionInformation.size() + " entries for this product group (" + currentReleaseInformation.productGroupVersion + ")");
		_log.debug("This version's release date: " + currentReleaseInformation.getReleaseDate() + " (" + currentReleaseInformation.productVersion + ")");
		ReleaseInformation latestVersion = productGroupVersionInformation.iterator().next();
		_log.debug("Latest version's release date: " + latestVersion.getReleaseDate() + " (" + latestVersion.productVersion + ")");

		int releasesBack = 0;
		ReleaseInformation immediateNextRelease = null;
		for (Iterator<ReleaseInformation> iterator = productGroupVersionInformation.iterator(); iterator.hasNext();) {
			ReleaseInformation releaseInformation = (ReleaseInformation) iterator.next();
			if(releaseInformation.productVersion.equals(currentReleaseInformation.productVersion)) {
				break;
			}
			immediateNextRelease = releaseInformation;
			releasesBack++;
		}
		
		ReleaseInformation firstRelease = productGroupVersionInformation.descendingIterator().next();
		
		if(ReleaseInfo.isDXP()) {
			if(releasesBack > 0) { 
				if(_config.maxPatchesToSkip() >= 0) {
					result.add(
							new HealthcheckItem(
									releasesBack <= _config.maxPatchesToSkip(), 
									_LINK, 
									"x-newer-patches-are-avialable-warn-from-x-on-you-are-on-x-latest-is-x", 
									releasesBack, _config.maxPatchesToSkip(), currentReleaseInformation.productVersion, latestVersion.productVersion));
				}
				if(immediateNextRelease != null && _config.ageInWeeksBeforeUpdateInstalled() >= 0) {
					long weeksSinceNextUpdateAvailable = ChronoUnit.WEEKS.between(immediateNextRelease.getReleaseDate(), LocalDate.now());
					_log.debug("Immediate next release: " + immediateNextRelease.productVersion + " available since " + immediateNextRelease.getReleaseDate());
					_log.debug("Latest release: " + latestVersion.productVersion + " available since " + latestVersion.getReleaseDate());
					result.add(
						new HealthcheckItem(
							weeksSinceNextUpdateAvailable < _config.ageInWeeksBeforeUpdateInstalled(), 
							_LINK, "a-newer-patch-release-is-available-for-x-weeks-now-you-will-be-warned-from-x-weeks-on-you-are-on-x-latest-is-x",  
							weeksSinceNextUpdateAvailable, 
							_config.ageInWeeksBeforeUpdateInstalled(), currentReleaseInformation.productVersion, latestVersion.productVersion)
					);
				}
			}

			if(_config.weeksWarningBeforePremiumSupportEnds() >= 0) {
				int premiumSupportYears = 0;
				if(firstRelease.productVersion.toUpperCase().indexOf("LTS") > 0) {
					premiumSupportYears = 3;
				} else if(firstRelease.productVersion.toUpperCase().indexOf("Q") > 0) {
					premiumSupportYears = 1;
				}
				LocalDate endOfPremiumSupport = firstRelease.getReleaseDate().plusYears(premiumSupportYears);
				long weeksUntilEndOfPremiumSupport = ChronoUnit.WEEKS.between(LocalDate.now(), endOfPremiumSupport);
	
				result.add(new HealthcheckItem(
						weeksUntilEndOfPremiumSupport > _config.weeksWarningBeforePremiumSupportEnds(), 
						_LINK, "for-this-release-you-have-x-weeks-of-premium-support-left", weeksUntilEndOfPremiumSupport));
			}			
		} else {
				if(releasesBack == 0) {
					result.add(new HealthcheckItem(true, _LINK,
							"you-are-on-the-latest-release-x-released-on-x",
							latestVersion.productVersion, latestVersion.getReleaseDate()));
				} else {
					
					result.add(new HealthcheckItem(false, _LINK,
						"a-newer-release-is-available-for-x-weeks-the-latest-release-is-x",
						releasesBack, latestVersion.productVersion));
				}
		}
		
		// TODO: Check for age of current implementation
		// DXP: Handle Q1 LTS, Q2-4 separately
		// DXP: Make desired upgrade time to new patch configurable
		// DXP: Make desired path (LTS-only, all quarterlies) configurable
		// DXP: Make desired upgrade time to next release configurable
		// DXP: Make desired warning time before end of premium/limited support configurable
		// Portal: When new release is available, warn to update
		
		return result;
	}

	@Override
	public String getCategory() {
		return "healthcheck-category-bestpractice";
	}

	@Activate
	@Modified
	protected void activate(Map<String, Object> properties) {
		_config = ConfigurableUtil.createConfigurable(
				HealthcheckBestPracticeConfiguration.class, 
				properties);
	}

	// configuration update will actually be handled in the @Modified event,
	// which will only be triggered in case we have a @Reference to the
	// ConfigurationProvider

	@Reference
	protected ConfigurationProvider configurationProvider;

	private HealthcheckBestPracticeConfiguration _config;
	
	private ReleaseInformation getCurrentReleaseInformation() throws IOException {
		if(releaseInfos == null) {
			releaseInfos = ReleaseResolver.retrieveReleases();
		}
		String searchString;
		if(ReleaseInfo.isDXP()) {
			searchString = "DXP " + ReleaseInfo.getVersionDisplayName();
		} else {
			searchString = "Portal " + ReleaseInfo.getVersionDisplayName(); // ToDo: Check "Portal " magic string
		}
		for (ReleaseInformation releaseInformation : releaseInfos) {
			if(releaseInformation.productVersion.equals(searchString)) {
				return releaseInformation;
			}
		}
		return null;
	}
	
	private LinkedList<ReleaseInformation> getProductGroupVersionInformation(String productGroupVersion) {
		LinkedList<ReleaseInformation> result = new LinkedList<ReleaseInformation>();
		for (ReleaseInformation releaseInformation : releaseInfos) {
			if(releaseInformation.productGroupVersion.equals(productGroupVersion)) {
				result.add(releaseInformation);
			}
		}
		Collections.sort(result, new Comparator<ReleaseInformation>() {

			@Override
			public int compare(ReleaseInformation arg0, ReleaseInformation arg1) {
				return arg1.getReleaseDate().compareTo(arg0.getReleaseDate());
			}
		});
		return result;
	}
	
	private Collection<ReleaseInformation> releaseInfos;

	private static final String _CFG = HealthcheckBestPracticeConfiguration.class.getName();
	
	private static final String _LINK = StringBundler.concat("/group/control_panel/manage?p_p_id=",
			ConfigurationAdminPortletKeys.SYSTEM_SETTINGS, "&_",
			ConfigurationAdminPortletKeys.SYSTEM_SETTINGS,
			"_mvcRenderCommandName=%2Fconfiguration_admin%2Fedit_configuration&_",
			ConfigurationAdminPortletKeys.SYSTEM_SETTINGS, "_factoryPid=", _CFG,
			"&_", ConfigurationAdminPortletKeys.SYSTEM_SETTINGS, "_pid=", _CFG);

	public static final Log _log = LogFactoryUtil.getLog(ReleaseAgeHealthcheck.class);
}
