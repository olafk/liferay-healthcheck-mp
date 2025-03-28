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

}