package com.liferay.healthcheck.operation.internal;

import com.liferay.healthcheck.Healthcheck;
import com.liferay.healthcheck.HealthcheckItem;
import com.liferay.object.model.ObjectDefinition;
import com.liferay.object.model.ObjectRelationship;
import com.liferay.object.service.ObjectDefinitionLocalService;
import com.liferay.object.service.ObjectRelationshipLocalService;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.util.ReleaseInfo;

import java.util.Collection;
import java.util.LinkedList;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * When DataSets were in Beta, they created relationships that are no longer valid and 
 * unsupported since the status changed to Release.
 * This healthcheck validates if the database is in an unsupported stage because it has been
 * upgraded from an older release that used to have the DataSet Beta flag activated.
 * Up and until 2024-Q4, DataSets were in Beta Status
 * Starting with 2025-Q1, DataSets were in Release Status
 */

@Component( service = Healthcheck.class )
public class DataSetBetaResidueHealthcheck implements Healthcheck {

	@Override
	public Collection<HealthcheckItem> check(long companyId) throws Exception {
		LinkedList<HealthcheckItem> result = new LinkedList<HealthcheckItem>();

		ObjectDefinition objectDefinition;
		try {
			objectDefinition = _objectDefinitionLocalService.getObjectDefinitionByExternalReferenceCode("L_DATA_SET", companyId);
		} catch (PortalException e) {
			result.add(new HealthcheckItem(true, ADDITIONAL_INFO, LINK, MSG_INACTIVE));
			return result;
		}
		
		ObjectRelationship relationshipReleased = null;
		ObjectRelationship relationshipBeta = null;
		try {
			relationshipReleased = _objectRelationshipLocalService.getObjectRelationshipByExternalReferenceCode("L_DATA_SET_TO_DATA_SET_ACTIONS", companyId, objectDefinition.getObjectDefinitionId());
		} catch (PortalException e) {
		}
		try {
			relationshipBeta = _objectRelationshipLocalService.getObjectRelationshipByExternalReferenceCode("L_DATA_SET_TO_CREATION_DATA_SET_ACTIONS", companyId, objectDefinition.getObjectDefinitionId());
		} catch (PortalException e) {
		}

		// Validation depends on the current release.
		long releaseYear = 2025;
		if(ReleaseInfo.isDXP()) {
			try {
				// let's be careful: If the VersionDisplayName changes in the future, we'll be defensive and default to 2025.
				// as of writing this particular healthcheck, no version prior to 2024 is supported.
				// Current output of ReleaseInfo.getVersionDisplayName() is something like "2025.Q2.2".
				releaseYear = Long.valueOf(ReleaseInfo.getVersionDisplayName().substring(0, 4));
			} catch (NumberFormatException e) {
			}
		} else {
			String versionDisplay = ReleaseInfo.getVersionDisplayName().toUpperCase();
			String[] releases2024 = {"GA120", "GA125", "GA129"};
			for (String release : releases2024) {
				if(versionDisplay.indexOf(release)>-1) {
					releaseYear = 2024;
					break;
				}
			}
			if(releaseYear == 2025 && versionDisplay.indexOf("GA132")==-1) {
				// TODO: Determine Release Year for unknown CE GA
				result.add(new HealthcheckItem(false, LINK, "DataSet Beta Data Detection: Can not determine release date for this CE release"));			
			}
		}
		
		if(relationshipBeta == null && relationshipReleased == null) {
			// Feature not activated at all. Fine
			result.add(
				new HealthcheckItem(
					true,
					ADDITIONAL_INFO,
					LINK, 
					MSG_INACTIVE));
		} else if(releaseYear <= 2024) {
			// feature still in beta: Expect Beta Relationships and no Release Relationships
			result.add(
				new HealthcheckItem(
					relationshipBeta != null && relationshipReleased == null,
					ADDITIONAL_INFO,
					LINK, 
					MSG_BETA_OK));
		} else {
			// feature is released. Database MUST NOT contain Beta data
			if(relationshipBeta != null) {
				result.add(
					new HealthcheckItem(
						false, 
						ADDITIONAL_INFO,
						LINK, 
						MSG_RELEASE_WITH_BETA));
			} else {
				result.add(
					new HealthcheckItem(
						true,
						null, 
						MSG_RELEASE_WITHOUT_BETA));
			}
		}
		
		return result;
	}

	@Override
	public String getCategory() {
		return "healthcheck-category-operation";
	}
	
	@Reference
	private ObjectDefinitionLocalService _objectDefinitionLocalService;

	@Reference
	private ObjectRelationshipLocalService _objectRelationshipLocalService;
	
	private static String[] ADDITIONAL_INFO = {"https://learn.liferay.com/l/33598172"};
	private static String LINK = "https://learn.liferay.com/l/33598172";
	private static String MSG_BETA_OK = "dataset-beta-ok";
	private static String MSG_RELEASE_WITH_BETA = "dataset-release-with-beta-fail";
	private static String MSG_RELEASE_WITHOUT_BETA = "dataset-release-without-beta-ok";
	private static String MSG_INACTIVE = "dataset-inactive-ok"; 
}
