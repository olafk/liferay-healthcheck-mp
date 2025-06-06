package com.liferay.healthcheck.bestpractice.internal;

import com.liferay.healthcheck.Healthcheck;
import com.liferay.healthcheck.HealthcheckItem;
import com.liferay.image.ImageMagick;
import com.liferay.portal.kernel.settings.CompanyServiceSettingsLocator;
import com.liferay.portal.kernel.settings.FallbackKeysSettingsUtil;
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

@Component(service=Healthcheck.class)
public class ModernImageFormatHealthcheck implements Healthcheck {

	@Override
	public Collection<HealthcheckItem> check(long companyId) throws Exception {
		LinkedList<HealthcheckItem> result = new LinkedList<>();
		if(_imageMagick.isEnabled()) {
			result.add(new HealthcheckItem(true, getIMCPLink(), _MSG_IMAGEMAGICK_ENABLED));
			byte[] scaledImage = _imageMagick.scale(SINGLE_PIXEL_PNG, "image/png", 1, 1);
			if(scaledImage != null) {
				result.add(new HealthcheckItem(true, getIMCPLink(), "ImageMagick can be executed"));
				String settingsId = "com.liferay.adaptive.media.image.internal.configuration.AMImageMagickConfiguration";
				SettingsLocator settingsLocator = new CompanyServiceSettingsLocator(companyId, settingsId, settingsId);
				TypedSettings settings = new TypedSettings(FallbackKeysSettingsUtil.getSettings(settingsLocator));
				String[] mimeTypes = settings.getValues("mimeTypes");
				List<String> mimeTypeList = Arrays.asList(mimeTypes);

				result.add(new HealthcheckItem(mimeTypeList.contains("image/avif"), _ADAPTIVE_MEDIA_LINKS, getMimeLink(), _MSG_MIME_CONFIGURABLE, "image/avif"));
				result.add(new HealthcheckItem(mimeTypeList.contains("image/webp"), _ADAPTIVE_MEDIA_LINKS, getMimeLink(), _MSG_MIME_CONFIGURABLE, "image/webp"));
			} else {
				result.add(new HealthcheckItem(false, getIMCPLink(), _MSG_IM_ACTIVE_BUT_NOT_EXECUTABLE));
			}
		} else {
			result.add(new HealthcheckItem(false, getIMCPLink(), _MSG_IMAGEMAGICK_DISABLED));
		}
		return result;
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
	private String getMimeLink() {
		return new StringBundler("/group/control_panel/manage?p_p_id=")
				.append(_SYSTEM_CONFIG)
				.append("&p_p_lifecycle=0&p_p_state=maximized&p_p_mode=view&_")
				.append(_SYSTEM_CONFIG)
				.append("_mvcRenderCommandName=%2Fconfiguration_admin%2Fedit_configuration&_")
				.append(_SYSTEM_CONFIG)
				.append("_factoryPid=")
				.append(_IM_CONFIG)
				.append("&_")
				.append(_SYSTEM_CONFIG)
				.append("_pid=")
				.append(_IM_CONFIG).toString();
	}

	@Reference
	ImageMagick _imageMagick;
	
	private static final String _MSG_IMAGEMAGICK_DISABLED = "imagemagick-is-disabled";
	private static final String _MSG_IMAGEMAGICK_ENABLED = "imagemagick-is-enabled";
	private static final String _MSG_IM_ACTIVE_BUT_NOT_EXECUTABLE = "imagemagick-enabled-but-cant-be-executed";
	private static final String _MSG_MIME_CONFIGURABLE = "mime-type-x-can-be-configured-in-system-settings";
	private static final String _SERVER_ADMIN = "com_liferay_server_admin_web_portlet_ServerAdminPortlet";
	private static final String _SYSTEM_CONFIG = "com_liferay_configuration_admin_web_portlet_SystemSettingsPortlet";
	private static final String _IM_CONFIG = "com.liferay.adaptive.media.image.internal.configuration.AMImageMagickConfiguration";
	private static final String[] _ADAPTIVE_MEDIA_LINKS = { "https://learn.liferay.com/w/dxp/content-authoring-and-management/documents-and-media/publishing-and-sharing/using-adaptive-media/adaptive-media-configuration-reference" };
//	private static byte[] SINGLE_PIXEL_PNG = new byte[]{(byte) 0x89,0x50,0x4e,0x47,0x0d,0x0a,0x1a,0x0a,0x00,0x00,0x00,0x0d,0x49,0x48,0x44,0x52,
//			0x00,0x00,0x00,0x01,0x00,0x00,0x00,0x01,0x08,0x02,0x00,0x00,0x00,(byte) 0x90,0x77,0x53,
//			(byte) 0xde,0x00,0x00,0x00,0x0c,0x49,0x44,0x41,0x54,0x08,(byte) 0xd7,0x63,0x60,0x60,0x60,0x00,
//			0x00,0x00,0x04,0x00,0x01,0x27,0x34,0x27,0x0a,0x00,0x00,0x00,0x00,0x49,0x45,0x4e,
//			0x44,(byte) 0xae,0x42,0x60,(byte) 0x82};
	
	private static byte[] SINGLE_PIXEL_PNG = HexFormat.of().parseHex("89504e470d0a1a0a0000000d4948445200000001000000010802000000907753de0000000c4944415408d7636060600000000400012734270a0000000049454e44ae426082");
	
}
