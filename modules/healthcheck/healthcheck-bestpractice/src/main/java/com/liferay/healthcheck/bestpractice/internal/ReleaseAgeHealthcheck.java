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
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;

/**
 * Not proud about this code - it's a bit of a mess, but covers a lot of configurable
 * preferences, combined with the necessity to dynamically load newer release dates
 * from the internet... It doesn't help that DXP is handled differently 
 * (releases vs patches) than CE, and that some of the data found in the release 
 * descriptions is inconsistent (e.g. "Portal" vs "PORTAL")...
 * 
 * More elegance would be welcome.
 * 
 * TODO refactor to make the checks more maintainable.
 */


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
			result.add(new HealthcheckItem(false, (String)null, "cant-find-meta-info-for-the-currently-running-release-x", ReleaseInfo.getVersionDisplayName()));
			return result;
		}

		if(ReleaseInfo.isDXP()) {
			// DXP
			LinkedList<ReleaseInformation> patchLevelVersionInformation = getPatchLevelVersionInformation(currentReleaseInformation.getProduct(), currentReleaseInformation.getProductGroupVersion());
			_log.debug("found " + patchLevelVersionInformation.size() + " entries for this product group (" + currentReleaseInformation.getProductGroupVersion() + ")");
			_log.debug("This version's release date: " + currentReleaseInformation.getReleaseDate() + " (" + currentReleaseInformation.getProductVersion() + ")");
			ReleaseInformation latestVersion = patchLevelVersionInformation.iterator().next();
			_log.debug("Latest version's release date: " + latestVersion.getReleaseDate() + " (" + latestVersion.getProductVersion() + ")");

			int patchesBehind = 0; // patches in DXP, full releases in CE
			ReleaseInformation immediateNextPatch = null;
			for (Iterator<ReleaseInformation> iterator = patchLevelVersionInformation.iterator(); iterator.hasNext();) {
				ReleaseInformation releaseInformation = (ReleaseInformation) iterator.next();
				if(releaseInformation.getProductVersion().equals(currentReleaseInformation.getProductVersion())) {
					break;
				}
				immediateNextPatch = releaseInformation;
				patchesBehind++;
			}

			if(patchesBehind > 0) {
				// Check for acceptable number of patches behind the latest
				if(_config.maxPatchesToSkip() >= 0) {
					result.add(
							new HealthcheckItem(
									patchesBehind <= _config.maxPatchesToSkip(), 
									_LINK, 
									"x-newer-patches-are-available-for-x-warn-from-x-on-you-are-on-x-latest-is-x", 
									patchesBehind, currentReleaseInformation.getProductGroupVersion(), _config.maxPatchesToSkip(), currentReleaseInformation.getProductVersion(), latestVersion.getProductVersion()));
				}
				
				// Check for acceptable time behind a newer patch release is available
				if(immediateNextPatch != null && _config.ageInWeeksBeforeUpdateInstalled() >= 0) {
					long weeksSinceNextUpdateAvailable = ChronoUnit.WEEKS.between(immediateNextPatch.getReleaseDate(), LocalDate.now());
					_log.debug("Immediate next release: " + immediateNextPatch.getProductVersion() + " available since " + immediateNextPatch.getReleaseDate());
					_log.debug("Latest release: " + latestVersion.getProductVersion() + " available since " + latestVersion.getReleaseDate());
					result.add(
						new HealthcheckItem(
							weeksSinceNextUpdateAvailable <= _config.ageInWeeksBeforeUpdateInstalled(), 
							_LINK, "a-newer-patch-release-is-available-for-x-weeks-now-you-will-be-warned-from-x-weeks-on-you-are-on-x-latest-is-x",  
							weeksSinceNextUpdateAvailable, 
							_config.ageInWeeksBeforeUpdateInstalled(), currentReleaseInformation.getProductVersion(), latestVersion.getProductVersion())
					);
				}
			}

			// Check for acceptable premium support period left
			// TODO: Allow configuration of purchased Extended Premium Support periods for specific versions
			if(_config.weeksWarningBeforePremiumSupportEnds() > -1000) {
				ReleaseInformation firstRelease = patchLevelVersionInformation.descendingIterator().next();
				int premiumSupportYears = 0;
				if(firstRelease.getProductVersion().toUpperCase().indexOf("LTS") > 0) {
					premiumSupportYears = 3 + _config.yearsAdditionalPremiumSupport();
				} else if(firstRelease.getProductVersion().toUpperCase().indexOf("Q") > 0) {
					premiumSupportYears = 1;
				}
				LocalDate endOfPremiumSupport = firstRelease.getReleaseDate().plusYears(premiumSupportYears);
				long weeksUntilEndOfPremiumSupport = ChronoUnit.WEEKS.between(LocalDate.now(), endOfPremiumSupport);
	
				result.add(new HealthcheckItem(
						weeksUntilEndOfPremiumSupport > _config.weeksWarningBeforePremiumSupportEnds(), 
						_LINK, "for-this-release-you-have-x-weeks-of-premium-support-left-including-x-years-extended", weeksUntilEndOfPremiumSupport, _config.yearsAdditionalPremiumSupport()));
			}
			
			// Figure out if there is a newer quarterly release, and if it's LTS if the preferences are
			// set to LTS-only.

			Set<String> productGroups = new TreeSet<String>();
			for (ReleaseInformation releaseInformation : getReleaseInfos()) {
				productGroups.add(releaseInformation.getProductGroupVersion());
			}
			LinkedList<ReleaseInformation> firstReleases = new LinkedList<ReleaseInformation>();
			for (String productGroup : productGroups) {
				LinkedList<ReleaseInformation> allPatches = getPatchLevelVersionInformation(currentReleaseInformation.getProduct(), productGroup);
				firstReleases.add(allPatches.descendingIterator().next());
			}
			firstReleases.sort(Comparator.comparing(ReleaseInformation::getReleaseDate).reversed());
			
			ReleaseInformation newerRelease = null;
			for (ReleaseInformation firstRelease : firstReleases) {
				if(currentReleaseInformation.getProductGroupVersion().equals(firstRelease.getProductGroupVersion())) {
					// we found the current release - no need to go further into the past.
					break;
				}
				if(_config.onlyUpdateToLTS() && firstRelease.getProductVersion().indexOf("LTS") > 0) {
					newerRelease = firstRelease;
					break;
				} else if(!_config.onlyUpdateToLTS()) {
					newerRelease = firstRelease;
					break;
				}
			}
			if(newerRelease==null) {
				result.add(new HealthcheckItem(true, _LINK, "you-are-on-the-latest-quarterly-release-x-according-to-your-lts-preference", currentReleaseInformation.getProductGroupVersion()));
			} else {
				long weeksAvailable = ChronoUnit.WEEKS.between(newerRelease.getReleaseDate(), LocalDate.now());
				result.add(new HealthcheckItem(false, _LINK, "a-newer-quarterly-release-x-is-available-for-x-weeks", newerRelease.getProductGroupVersion(), weeksAvailable));
			}
		} else {
			// Community Edition
			LinkedList<ReleaseInformation> releaseVersionInformation = getPatchLevelVersionInformation(currentReleaseInformation.getProduct(), currentReleaseInformation.getProductGroupVersion());
			_log.debug("found " + releaseVersionInformation.size() + " entries for this product group (" + currentReleaseInformation.getProduct() + " " + currentReleaseInformation.getProductGroupVersion() + ")");
			_log.debug("This version's release date: " + currentReleaseInformation.getReleaseDate() + " (" + currentReleaseInformation.getProductVersion() + ")");
			ReleaseInformation latestVersion = releaseVersionInformation.iterator().next();
			_log.debug("Latest version's release date: " + latestVersion.getReleaseDate() + " (" + latestVersion.getProductVersion() + ")");

			int releasesBehind = 0; 
			for (Iterator<ReleaseInformation> iterator = releaseVersionInformation.iterator(); iterator.hasNext();) {
				ReleaseInformation releaseInformation = (ReleaseInformation) iterator.next();
				if(releaseInformation.getProductVersion().equals(currentReleaseInformation.getProductVersion())) {
					break;
				}
				releasesBehind++;
			}
			
			if(releasesBehind == 0) {
				result.add(new HealthcheckItem(true, _LINK,
						"you-are-on-the-latest-release-x-released-on-x",
						latestVersion.getProductVersion(), latestVersion.getReleaseDate()));
			} else {
				
				result.add(new HealthcheckItem(false, _LINK,
					"a-newer-release-is-available-for-x-weeks-the-latest-release-is-x",
					releasesBehind, latestVersion.getProductVersion()));
			}
		}
		
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
		Collection<ReleaseInformation> releaseInfos = getReleaseInfos();
		String searchString;
		if(ReleaseInfo.isDXP()) {
			searchString = "DXP " + ReleaseInfo.getVersionDisplayName();
		} else {
			searchString = "PORTAL " + ReleaseInfo.getVersionDisplayName().replace("CE GA", "GA"); // ToDo: Check "Portal " magic string
		}
		for (ReleaseInformation releaseInformation : releaseInfos) {
			if(releaseInformation.getProductVersion().equals(searchString)) {
				return releaseInformation;
			}
		}
		return null;
	}

	private LinkedList<ReleaseInformation> getPatchLevelVersionInformation(String product, String productGroupVersion) throws IOException {
		LinkedList<ReleaseInformation> result = new LinkedList<ReleaseInformation>();
		Collection<ReleaseInformation> releaseInfos = getReleaseInfos();
		
		for (ReleaseInformation releaseInformation : releaseInfos) {
			if(releaseInformation.getProduct().equals(product) && releaseInformation.getProductGroupVersion().equals(productGroupVersion)) {
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
	
	private Collection<ReleaseInformation> getReleaseInfos() throws IOException {
		if(ChronoUnit.HOURS.between(lastFetch, LocalDateTime.now()) > 24 ) {
			_log.debug("fetching new release-information");
			_releaseInfos = ReleaseResolver.retrieveReleases();
			lastFetch = LocalDateTime.now();
		} else {
			_log.debug("skipping fetch of new release info as the current information is less than a day old");
		}
		return _releaseInfos;
	}
	
	// only access through getReleaseInfo, even locally, so that daily reloading of newer releases can be handled.
	private Collection<ReleaseInformation> _releaseInfos;

	// will fetch releaseinfos daily, so start with an age older than that - ensuring that releases will be fetched
	// from internet when first accessed.
	private static LocalDateTime lastFetch = LocalDateTime.now().minusMonths(1);
	
	private static final String _CFG = HealthcheckBestPracticeConfiguration.class.getName();
	
	private static final String _LINK = StringBundler.concat("/group/control_panel/manage?p_p_id=",
			ConfigurationAdminPortletKeys.SYSTEM_SETTINGS, "&_",
			ConfigurationAdminPortletKeys.SYSTEM_SETTINGS,
			"_mvcRenderCommandName=%2Fconfiguration_admin%2Fedit_configuration&_",
			ConfigurationAdminPortletKeys.SYSTEM_SETTINGS, "_factoryPid=", _CFG,
			"&_", ConfigurationAdminPortletKeys.SYSTEM_SETTINGS, "_pid=", _CFG);

	public static final Log _log = LogFactoryUtil.getLog(ReleaseAgeHealthcheck.class);
}
