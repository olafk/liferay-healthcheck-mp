package com.liferay.healthcheck.bestpractice.internal.configuration;

import aQute.bnd.annotation.metatype.Meta;

import com.liferay.portal.configuration.metatype.annotations.ExtendedObjectClassDefinition;

/**
 * @author Olaf Kock
 */
@ExtendedObjectClassDefinition(
	category = "healthcheck",
	scope = ExtendedObjectClassDefinition.Scope.COMPANY
)
@Meta.OCD(
	description = "healthcheck-bestpractice-configuration-description",
	id = "com.liferay.healthcheck.bestpractice.internal.configuration.HealthcheckBestPracticeConfiguration",
	localization = "content/Language",
	name = "healthcheck-bestpractice-configuration-name"
)
public interface HealthcheckBestPracticeConfiguration {

	@Meta.AD(
		description = "healthcheck-bestpractice-password-hashing-algorithm-whitelist-description",
		name = "healthcheck-bestpractice-password-hashing-algorithm-whitelist-name",
		required = false
	)
	public String[] passwordHashingAlgorithmWhitelist();

	@Meta.AD(
		deflt = "100",
		description = "healthcheck-bestpractice-max-users-tested-for-hashing-algorithm-whitelist-description",
		name = "healthcheck-bestpractice-max-users-tested-for-hashing-algorithm-whitelist-name",
		required = false
	)
	public long maxUsersTestedForHashingAlgorithm();
	
	@Meta.AD(
		deflt = "3",
		description = "healthcheck-bestpractice-age-in-weeks-before-update-installed-description",
		name = "healthcheck-bestpractice-age-in-weeks-before-update-installed-name",
		required = false
	)
	public long ageInWeeksBeforeUpdateInstalled();
		
	@Meta.AD(
		deflt = "2",
		description = "healthcheck-bestpractice-max-patches-to-skip-description",
		name = "healthcheck-bestpractice-max-patches-to-skip-name",
		required = false
	)
	public long maxPatchesToSkip();
	
	@Meta.AD(
		deflt = "15",
		description = "healthcheck-bestpractice-weeks-warning-before-premium-support-ends-description",
		name = "healthcheck-bestpractice-weeks-warning-before-premium-support-ends-name",
		required = false
	)
	public long weeksWarningBeforePremiumSupportEnds();
	
	@Meta.AD(
		deflt = "false",
		description = "healthcheck-bestpractice-only-update-to-lts-description",
		name = "healthcheck-bestpractice-only-update-to-lts-name",
		required = false
	)
	public boolean onlyUpdateToLTS();
		
	
}