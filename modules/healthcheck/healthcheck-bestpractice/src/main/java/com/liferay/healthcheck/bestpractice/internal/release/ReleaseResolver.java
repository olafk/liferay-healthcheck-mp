package com.liferay.healthcheck.bestpractice.internal.release;

import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.HttpUtil;

import java.io.IOException;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedList;

public class ReleaseResolver {
	public static Collection<ReleaseInformation> retrieveReleases() throws IOException {
		if(JSONFactoryUtil.getJSONFactory() == null) return null; // not yet initialized

		LinkedList<ReleaseInformation> result = new LinkedList<ReleaseInformation>();
		try {
			String json = null;
			try {
				// Note: getVersionDisplayName was introduced - according to LPS-144747 - in DXP 7.4 Update 3 / CE 7.4 GA7
				String location = "https://releases-cdn.liferay.com/releases.json";
				json = HttpUtil.URLtoString(location);
				log.info("read " + json.length() + " characters config from " + location);
			} catch (IOException e) {
				log.warn("Couldn't resolve up-to-date release content due to " + e.getMessage() + " " + e.getClass().getName());
			} catch (NullPointerException e) {
				return null; // not yet initialized
			}
			
			ReleaseInformation[] releaseInfos = JSONFactoryUtil.looseDeserialize(json, ReleaseInformation[].class);
			for (ReleaseInformation releaseInfo : releaseInfos) {
				if(releaseInfo.getProductVersion().length() > 0) {
					result.add(releaseInfo);
				}
			}
		} catch (Exception e) {
			log.error(e);
		}

		return result;
	}

	public static final Log log = LogFactoryUtil.getLog(ReleaseResolver.class);

	public static class ReleaseComparator implements Comparator<ReleaseInformation>{

		@Override
		public int compare(ReleaseInformation arg0, ReleaseInformation arg1) {
			return arg0.getReleaseDate().compareTo(arg1.getReleaseDate());
		}
		
	}
}
