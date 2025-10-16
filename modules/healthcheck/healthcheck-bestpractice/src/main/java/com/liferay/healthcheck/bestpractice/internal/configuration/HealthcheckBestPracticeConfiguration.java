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
	public int ageInWeeksBeforeUpdateInstalled();
		
	@Meta.AD(
		deflt = "2",
		description = "healthcheck-bestpractice-max-patches-to-skip-description",
		name = "healthcheck-bestpractice-max-patches-to-skip-name",
		required = false
	)
	public int maxPatchesToSkip();
	
	@Meta.AD(
		deflt = "15",
		description = "healthcheck-bestpractice-weeks-warning-before-premium-support-ends-description",
		name = "healthcheck-bestpractice-weeks-warning-before-premium-support-ends-name",
		required = false
	)
	public int weeksWarningBeforePremiumSupportEnds();
	
	@Meta.AD(
			deflt = "0",
			description = "healthcheck-bestpractice-extended-premium-support-subscribed-description",
			name = "healthcheck-bestpractice-extended-premium-support-subscribed-name",
			required = false
	)
	public int yearsAdditionalPremiumSupport();
	
	@Meta.AD(
		deflt = "false",
		description = "healthcheck-bestpractice-only-update-to-lts-description",
		name = "healthcheck-bestpractice-only-update-to-lts-name",
		required = false
	)
	public boolean onlyUpdateToLTS();
		
	@Meta.AD(
		deflt = "b50ad7a5d52f8bbde47aced8a84632d1 (video-url),9a302d020646f2cb4687453b8850928 (dropdown)",
		description = "healthcheck-bestpractice-fragment-js-whitelist-description",
		name = "healthcheck-bestpractice-fragment-js-whitelist-name",
		required = false
	)
	public String[] fragmentJsWhitelist();
	
}