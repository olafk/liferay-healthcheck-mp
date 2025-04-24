package com.liferay.healthcheck.operation.internal.auxiliary;

import com.liferay.portal.kernel.language.UTF8Control;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.module.framework.ModuleServiceLifecycle;

import java.util.Enumeration;
import java.util.ResourceBundle;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Liferay
 */
@Component(
	immediate = true, property = "language.id=en_US",
	service = ResourceBundle.class
)
public class CustomResourceBundle extends ResourceBundle {

	@Activate
	public void activate() {
		_log.debug("Healthcheck Resource Bundle Deployed!");
	}

	@Override
	public Enumeration<String> getKeys() {
		return _resourceBundle.getKeys();
	}

	@Override
	protected Object handleGetObject(String key) {
		return _resourceBundle.getObject(key);
	}

	private static final Log _log = LogFactoryUtil.getLog(
		CustomResourceBundle.class);

	@Reference(target = ModuleServiceLifecycle.PORTAL_INITIALIZED)
	private ModuleServiceLifecycle _dataInitialized;

	private final ResourceBundle _resourceBundle = ResourceBundle.getBundle(
		"content.Language", UTF8Control.INSTANCE);

}