package com.liferay.healthcheck.relaxed.internal.configuration;

import com.liferay.portal.configuration.metatype.annotations.ExtendedObjectClassDefinition;

import aQute.bnd.annotation.metatype.Meta;

/**
 * @author Olaf Kock
 */
@ExtendedObjectClassDefinition(
	category = "healthcheck",
	scope = ExtendedObjectClassDefinition.Scope.COMPANY
)
@Meta.OCD(
	description = "healthcheck-relaxed-configuration-description",
	id = "com.liferay.healthcheck.relaxed.internal.configuration.HealthcheckRelaxedConfiguration",
	localization = "content/Language",
	name = "healthcheck-relaxed-configuration-name"
)
public interface HealthcheckRelaxedConfiguration {
	@Meta.AD(
		description = "healthcheck-relaxed-instance-hostnames-description",
		name = "healthcheck-relaxed-instance-hostnames-name",
		deflt = "localhost",
		required = false
	)
	public String[] relaxedInstanceHostnames();
}
