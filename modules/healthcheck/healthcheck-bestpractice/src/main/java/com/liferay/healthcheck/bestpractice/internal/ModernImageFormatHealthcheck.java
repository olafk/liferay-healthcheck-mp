package com.liferay.healthcheck.bestpractice.internal;

import com.liferay.healthcheck.Healthcheck;
import com.liferay.healthcheck.HealthcheckItem;
import com.liferay.image.ImageMagick;
import com.liferay.portal.kernel.settings.CompanyServiceSettingsLocator;
import com.liferay.portal.kernel.settings.FallbackKeysSettingsUtil;
import com.liferay.portal.kernel.settings.SettingsException;
import com.liferay.portal.kernel.settings.SettingsLocator;
import com.liferay.portal.kernel.settings.TypedSettings;
import com.liferay.portal.kernel.util.StringBundler;

import java.util.Arrays;
import java.util.Collection;
import java.util.HexFormat;
import java.util.LinkedList;
import java.util.List;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

@Component(
		configurationPid = "com.liferay.adaptive.media.image.internal.configuration.AMImageConfiguration", 
		service=Healthcheck.class
)
public class ModernImageFormatHealthcheck implements Healthcheck {

	@Override
	public Collection<HealthcheckItem> check(long companyId) throws Exception {
		LinkedList<HealthcheckItem> result = new LinkedList<>();
		if(_imageMagick.isEnabled()) {
			result.add(new HealthcheckItem(true, getIMCPLink(), _MSG_IMAGEMAGICK_ENABLED));
			byte[] scaledImage = _imageMagick.scale(SINGLE_PIXEL_PNG, "image/png", 1, 1);
			if(scaledImage != null) {
				result.add(new HealthcheckItem(true, getIMCPLink(), "ImageMagick can be executed"));

				List<String> imMimeTypes = getSettings(companyId, "com.liferay.adaptive.media.image.internal.configuration.AMImageMagickConfiguration", "mimeTypes");
				List<String> amMimeTypes = getSettings(companyId, "com.liferay.adaptive.media.image.internal.configuration.AMImageConfiguration", "supportedMimeTypes");

				result.add(new HealthcheckItem(imMimeTypes.contains("image/avif"), _ADAPTIVE_MEDIA_LINKS, getMimeLink(_IM_CONFIG), _MSG_MIME_CONFIGURABLE_IM, "image/avif"));
				result.add(new HealthcheckItem(imMimeTypes.contains("image/webp"), _ADAPTIVE_MEDIA_LINKS, getMimeLink(_IM_CONFIG), _MSG_MIME_CONFIGURABLE_IM, "image/webp"));

				result.add(new HealthcheckItem(amMimeTypes.contains("image/avif"), _ADAPTIVE_MEDIA_LINKS, getMimeLink(_AM_CONFIG), _MSG_MIME_CONFIGURABLE, "image/avif"));
				result.add(new HealthcheckItem(amMimeTypes.contains("image/webp"), _ADAPTIVE_MEDIA_LINKS, getMimeLink(_AM_CONFIG), _MSG_MIME_CONFIGURABLE, "image/webp"));
			} else {
				result.add(new HealthcheckItem(false, _ADAPTIVE_MEDIA_LINKS, getIMCPLink(), _MSG_IM_ACTIVE_BUT_NOT_EXECUTABLE));
			}
		} else {
			result.add(new HealthcheckItem(false, _ADAPTIVE_MEDIA_LINKS, getIMCPLink(), _MSG_IMAGEMAGICK_DISABLED));
		}
		return result;
	}

	private List<String> getSettings(long companyId, String settingsId, String settingsKey) throws SettingsException {
		SettingsLocator settingsLocator = new CompanyServiceSettingsLocator(companyId, settingsId, settingsId);
		TypedSettings settings = new TypedSettings(FallbackKeysSettingsUtil.getSettings(settingsLocator));
		String[] mimeTypes = settings.getValues(settingsKey);
		return Arrays.asList(mimeTypes);
	}

	@Override
	public String getCategory() {
		return "healthcheck-category-bestpractice";
	}
	
	private String getIMCPLink() {
		return new StringBundler("/group/control_panel/manage?p_p_id=")
					.append(_SERVER_ADMIN)
					.append("&p_p_lifecycle=0&p_p_state=maximized&p_p_mode=view&_")
					.append(_SERVER_ADMIN)
					.append("_mvcRenderCommandName=%2Fserver_admin%2Fview&_")
					.append(_SERVER_ADMIN)
					.append("_tabs1=external-services").toString();
	}
	// ""
	private String getMimeLink(String forPid) {
		return new StringBundler("/group/control_panel/manage?p_p_id=")
				.append(_SYSTEM_CONFIG)
				.append("&p_p_lifecycle=0&p_p_state=maximized&p_p_mode=view&_")
				.append(_SYSTEM_CONFIG)
				.append("_mvcRenderCommandName=%2Fconfiguration_admin%2Fedit_configuration&_")
				.append(_SYSTEM_CONFIG)
				.append("_factoryPid=")
				.append(forPid)
				.append("&_")
				.append(_SYSTEM_CONFIG)
				.append("_pid=")
				.append(forPid).toString();
	}

	@Reference
	ImageMagick _imageMagick;
	
	private static final String _MSG_IMAGEMAGICK_DISABLED = "imagemagick-is-disabled";
	private static final String _MSG_IMAGEMAGICK_ENABLED = "imagemagick-is-enabled";
	private static final String _MSG_IM_ACTIVE_BUT_NOT_EXECUTABLE = "imagemagick-enabled-but-cant-be-executed";
	private static final String _MSG_MIME_CONFIGURABLE = "mime-type-x-can-be-configured-in-system-settings";
	private static final String _MSG_MIME_CONFIGURABLE_IM = "mime-type-x-can-be-configured-in-system-settings-for-imagemagick";
	private static final String _SERVER_ADMIN = "com_liferay_server_admin_web_portlet_ServerAdminPortlet";
	private static final String _SYSTEM_CONFIG = "com_liferay_configuration_admin_web_portlet_SystemSettingsPortlet";
	private static final String _IM_CONFIG = "com.liferay.adaptive.media.image.internal.configuration.AMImageMagickConfiguration";
	private static final String _AM_CONFIG = "com.liferay.adaptive.media.image.internal.configuration.AMImageConfiguration";
	private static final String[] _ADAPTIVE_MEDIA_LINKS = { 
			"https://learn.liferay.com/w/dxp/content-authoring-and-management/documents-and-media/publishing-and-sharing/using-adaptive-media/adaptive-media-configuration-reference",
			"https://learn.liferay.com/w/dxp/security-and-administration/administration/using-the-server-administration-panel/configuring-external-services"
			};
	private static final byte[] SINGLE_PIXEL_PNG = HexFormat.of().parseHex(
			"89504e470d0a1a0a0000000d4948445200000001000000010802000000907753de0000000c4944415408d7636060600000000400012734270a0000000049454e44ae426082"
			);
	
}
