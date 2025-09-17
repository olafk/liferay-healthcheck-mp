package com.liferay.healthcheck.bestpractice.internal;

import com.liferay.fragment.contributor.FragmentCollectionContributorRegistry;
import com.liferay.fragment.model.FragmentCollection;
import com.liferay.fragment.model.FragmentEntry;
import com.liferay.fragment.service.FragmentCollectionLocalService;
import com.liferay.fragment.service.FragmentCollectionService;
import com.liferay.fragment.service.FragmentEntryLocalService;
import com.liferay.healthcheck.Healthcheck;
import com.liferay.healthcheck.HealthcheckItem;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.service.GroupLocalService;
import com.liferay.portal.kernel.util.StringUtil;

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
					int addCount = StringUtil.count(fragmentEntry.getJs(), "addEventListener");
					int removeCount = StringUtil.count(fragmentEntry.getJs(), "removeEventListener");
					if(addCount != removeCount && 
							fragmentEntry.getJs().indexOf("// @SuppressWarning EventListener " + addCount + " " + removeCount) < 0) {
						result.add(
								new HealthcheckItem(false, 
										ADDITIONAL_DOCUMENTATION, 
										buildLink(fragmentEntry, fragmentCollection, group), 
										MSG, 
										fragmentEntry.getName(), 
										fragmentCollection.getName(), 
										group.getName(), 
										addCount, 
										removeCount)
								);
					}
				}
			}
		}
		return result;
	}
	
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

	private static final String MSG = "fragment-x-in-fragment-set-x-in-group-x-has-x-addEventListener-and-x-removeEventListener";
	private static final String[] ADDITIONAL_DOCUMENTATION = {
				"https://liferay.dev/blogs/-/blogs/please-don-t-litter-eventlisteners",
				"https://liferay.atlassian.net/browse/LPD-65556"};
	
	private static final Log _log = LogFactoryUtil.getLog(FragmentSpaAwarenessHealthcheck.class);

}
