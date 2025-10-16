package com.liferay.healthcheck.bestpractice.internal;

import com.liferay.fragment.contributor.FragmentCollectionContributor;
import com.liferay.fragment.contributor.FragmentCollectionContributorRegistry;
import com.liferay.fragment.model.FragmentCollection;
import com.liferay.fragment.model.FragmentEntry;
import com.liferay.fragment.service.FragmentCollectionLocalService;
import com.liferay.fragment.service.FragmentCollectionService;
import com.liferay.fragment.service.FragmentEntryLocalService;
import com.liferay.healthcheck.Healthcheck;
import com.liferay.healthcheck.HealthcheckItem;
import com.liferay.healthcheck.bestpractice.internal.configuration.HealthcheckBestPracticeConfiguration;
import com.liferay.portal.configuration.module.configuration.ConfigurationProvider;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.module.configuration.ConfigurationException;
import com.liferay.portal.kernel.service.GroupLocalService;
import com.liferay.portal.kernel.util.ArrayUtil;
import com.liferay.portal.kernel.util.StringUtil;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

@Component
public class FragmentSpaAwarenessHealthcheck implements Healthcheck {

	@Override
	public Collection<HealthcheckItem> check(long companyId) throws Exception {
		List<HealthcheckItem> result = new LinkedList<HealthcheckItem>();
		List<Group> activeGroups = _groupLocalService.getActiveGroups(companyId, true);
		_log.info("Found " + activeGroups.size() + " active groups in company " + companyId);

		for (Group group : activeGroups) {
			List<FragmentCollection> fragmentCollections = _fragmentCollectionLocalService.getFragmentCollections(group.getGroupId(), 0, 640);
			for (FragmentCollection fragmentCollection : fragmentCollections) {
				List<FragmentEntry> fragmentEntries = fragmentEntryLocalService.getFragmentEntries(fragmentCollection.getPrimaryKey());
				for (FragmentEntry fragmentEntry : fragmentEntries) {
					int addCount = StringUtil.count(fragmentEntry.getJs(), "window.addEventListener");
					int removeCount = StringUtil.count(fragmentEntry.getJs(), "window.removeEventListener");
					if(addCount != removeCount && 
							fragmentEntry.getJs().indexOf("// @SuppressWarning EventListener " + addCount + " " + removeCount) < 0 &&
							! isWhitelisted(companyId, computeChecksum(fragmentEntry))) {
						result.add(
								new HealthcheckItem(false, 
										ADDITIONAL_DOCUMENTATION, 
										buildLink(fragmentEntry, fragmentCollection, group), 
										MSG, 
										fragmentEntry.getName(), 
										fragmentCollection.getName(), 
										group.getName(), 
										addCount, 
										removeCount, 
										computeChecksum(fragmentEntry))
								);
					}
				}
			}
		}
		List<FragmentCollectionContributor> fragmentCollectionContributors = _fragmentCollectionContributorRegistry.getFragmentCollectionContributors();
		for (FragmentCollectionContributor fragmentCollectionContributor : fragmentCollectionContributors) {
			for(FragmentEntry fragmentEntry : fragmentCollectionContributor.getFragmentEntries()) {
				int addCount = StringUtil.count(fragmentEntry.getJs(), "window.addEventListener");
				int removeCount = StringUtil.count(fragmentEntry.getJs(), "window.removeEventListener");
				if(addCount != removeCount && 
						fragmentEntry.getJs().indexOf("// @SuppressWarning EventListener " + addCount + " " + removeCount) < 0 &&
						! isWhitelisted(companyId, computeChecksum(fragmentEntry))) {
					result.add(
							new HealthcheckItem(false, 
									ADDITIONAL_DOCUMENTATION, 
									"/group/guest/~/control_panel/manage/-/fragments/fragment_collection/0/fragment_entry/by_key/"
									+ fragmentEntry.getFragmentEntryKey()
									+ "/edit"
									+ "?_com_liferay_fragment_web_portlet_FragmentPortlet_fragmentCollectionId=0"
									+ "&_com_liferay_fragment_web_portlet_FragmentPortlet_fragmentEntryKey=" + fragmentEntry.getFragmentEntryKey(), // null, // buildLink(fragmentEntry, fragmentCollection, group), 
									MSG, 
									fragmentEntry.getName(), 
									fragmentCollectionContributor.getName(), 
									"-",// group.getName(), 
									addCount, 
									removeCount, 
									computeChecksum(fragmentEntry))
							);
				}
				
			}
			
		}
		
		return result;
	}
	    //  localhost:8080/group/guest/~
		//  /control_panel/manage/-
	    //  /fragments/fragment_collection/0
	    //  /fragment_entry/by_key/INPUTS-checkbox/edit
	    //  ?_clfwp_FragmentPortlet_fragmentCollectionId=0
	    //  &_clfwp_FragmentPortlet_fragmentEntryKey=INPUTS-checkbox
	    //  & p_p_auth=NOJoFgEM
	private String buildLink(FragmentEntry fragmentEntry, FragmentCollection fragmentCollection, Group group) {
		String result = new StringBuilder("/group")
				.append(group.getFriendlyURL())
				.append("/~/control_panel/manage/-")
				.append("/fragments/fragment_collection/")
				.append(fragmentCollection.getFragmentCollectionId())
				.append("/fragment_entry/")
				.append(fragmentEntry.getPrimaryKey())
				.append("/edit").toString();

		return result.toString();
	}
	
	private boolean isWhitelisted(long companyId, String checksum) throws ConfigurationException {
		HealthcheckBestPracticeConfiguration companyConfiguration = _configurationProvider
				.getCompanyConfiguration(HealthcheckBestPracticeConfiguration.class, companyId);
		return ArrayUtil.contains(companyConfiguration.fragmentJsWhitelist(), checksum);
	}
	
	private String computeChecksum(FragmentEntry fragmentEntry) {
		String js = fragmentEntry.getJs();
		try {
			byte[] bytesOfMessage = js.getBytes("UTF-8");
			
			MessageDigest md = MessageDigest.getInstance("MD5");
			byte[] digest = md.digest(bytesOfMessage);
			String hexDigest = new BigInteger(1, digest).toString(16);
			return hexDigest + " (" + fragmentEntry.getName() + ")";
		} catch (UnsupportedEncodingException e) {
			return e.getClass().getName();
		} catch (NoSuchAlgorithmException e) {
			return e.getClass().getName();
		}	
	}
	
	@Override
	public String getCategory() {
		return "healthcheck-category-bestpractice";
	}
	
	@Reference
	private FragmentEntryLocalService fragmentEntryLocalService;
	
	@Reference
	private FragmentCollectionContributorRegistry
		_fragmentCollectionContributorRegistry;
	
	@Reference
	private FragmentCollectionLocalService 
		_fragmentCollectionLocalService;

	@Reference
	private FragmentCollectionService 
		_fragmentCollectionService;

	@Reference
	private GroupLocalService _groupLocalService;

	@Reference
	private ConfigurationProvider _configurationProvider;

	private static final String MSG = "fragment-x-in-fragment-set-x-in-group-x-has-x-addEventListener-and-x-removeEventListener";
	private static final String[] ADDITIONAL_DOCUMENTATION = {
				"https://liferay.dev/blogs/-/blogs/please-don-t-litter-eventlisteners",
				"https://liferay.atlassian.net/browse/LPD-65556"};
	
	private static final Log _log = LogFactoryUtil.getLog(FragmentSpaAwarenessHealthcheck.class);

}
