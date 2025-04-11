package com.liferay.healthcheck.bestpractice.internal.release;

import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.HttpUtil;

import java.io.IOException;
import java.io.StringReader;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Properties;

/**
 * {
    "product": "dxp",
    "productGroupVersion": "2025.q1",
    "productVersion": "DXP 2025.Q1.6 LTS",
    "promoted": "true",
    "releaseKey": "dxp-2025.q1.6-lts",
    "targetPlatformVersion": "2025.q1.6",
    "url": "https://releases-cdn.liferay.com/dxp/2025.q1.6-lts"
  }
 */
public class ReleaseInformation {
	public String product;
	public String productGroupVersion;
	public String productVersion;
	public String promoted;
	public String releaseKey;
	public String targetPlatformVersion;
	public String url;

	public LocalDate getReleaseDate() {
		if(releaseDate == null) {
			try {
				if(releaseDateString == null) {
					releaseDateString = oldReleases.get(productVersion);
				}
				if(releaseDateString == null) {
					String props = HttpUtil.URLtoString(url+"/release.properties");
					Properties properties = new Properties();
					properties.load(new StringReader(props));
					releaseDateString = properties.getProperty("release.date");
				}
				DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-M-d");
				releaseDate = LocalDate.parse(releaseDateString, formatter);
			} catch (IOException e) {
				_log.error(e);
			}
		}
		return releaseDate;
	}
	
	private String releaseDateString;
	private LocalDate releaseDate;
	
	private Log _log = LogFactoryUtil.getLog(ReleaseInformation.class);
	
	private static HashMap<String, String> oldReleases = new HashMap<String, String>();
	
	{
		oldReleases.put("DXP 2025.Q1.6 LTS", "2025-03-31");
		oldReleases.put("DXP 2025.Q1.5 LTS", "2025-03-24");
		oldReleases.put("DXP 2024.Q1.15", "2025-03-17");
		oldReleases.put("DXP 2025.Q1.4 LTS", "2025-03-17");
		oldReleases.put("DXP 2025.Q1.3 LTS", "2025-03-10");
		oldReleases.put("DXP 2025.Q1.2 LTS", "2025-03-03");
		oldReleases.put("DXP 2024.Q1.14", "2025-02-27");
		oldReleases.put("DXP 2025.Q1.1 LTS", "2025-02-24");
		oldReleases.put("DXP 2023.Q3.10", "2025-02-24");
		oldReleases.put("DXP 2025.Q1.0 LTS", "2025-02-18");
		oldReleases.put("DXP 2024.Q2.13", "2025-02-17");
		oldReleases.put("DXP 2024.Q4.7", "2025-02-10");
		oldReleases.put("DXP 2024.Q4.6", "2025-02-06");
		oldReleases.put("DXP 2024.Q1.13", "2025-01-31");
		oldReleases.put("DXP 2024.Q4.5", "2025-01-27");
		oldReleases.put("DXP 2023.Q4.10", "2025-01-22");
		oldReleases.put("DXP 2024.Q4.4", "2025-01-20");
		oldReleases.put("DXP 2024.Q4.3", "2025-01-13");
		oldReleases.put("DXP 2024.Q4.2", "2025-01-06");
		oldReleases.put("DXP 2024.Q4.1", "2024-12-16");
		oldReleases.put("DXP 2024.Q3.13", "2024-12-16");
		oldReleases.put("DXP 2024.Q4.0", "2024-12-09");
		oldReleases.put("DXP 2024.Q3.12", "2024-12-02");
		oldReleases.put("DXP 2024.Q3.11", "2024-11-28");
		oldReleases.put("DXP 2024.Q3.10", "2024-11-20");
		oldReleases.put("DXP 2024.Q3.9", "2024-11-11");
		oldReleases.put("DXP 2024.Q3.8", "2024-11-05");
		oldReleases.put("DXP 2024.Q3.7", "2024-10-28");
		oldReleases.put("DXP 2024.Q3.6", "2024-10-21");
		oldReleases.put("DXP 2024.Q3.5", "2024-10-14");
		oldReleases.put("DXP 2024.Q3.4", "2024-10-07");
		oldReleases.put("DXP 2024.Q3.3", "2024-09-30");
		oldReleases.put("DXP 2024.Q3.2", "2024-09-23");
		oldReleases.put("DXP 2024.Q3.1", "2024-09-16");
		oldReleases.put("DXP 2024.Q3.0", "2024-09-09");
		oldReleases.put("DXP 2024.Q2.12", "2024-09-02");
		oldReleases.put("DXP 2024.Q2.11", "2024-08-26");
		oldReleases.put("DXP 2024.Q2.10", "2024-08-21");
		oldReleases.put("DXP 2024.Q2.9", "2024-08-12");
		oldReleases.put("DXP 2024.Q2.8", "2024-08-05");
		oldReleases.put("DXP 2024.Q2.7", "2024-07-29");
		oldReleases.put("DXP 2024.Q2.6", "2024-07-22");
		oldReleases.put("DXP 2024.Q2.5", "2024-07-15");
		oldReleases.put("DXP 2024.Q2.4", "2024-07-08");
		oldReleases.put("DXP 2024.Q2.3", "2024-07-01");
		oldReleases.put("DXP 2024.Q2.2", "2024-06-24");
		oldReleases.put("DXP 2024.Q2.1", "2024-06-17");
		oldReleases.put("DXP 2024.Q2.0", "2024-06-07");
		oldReleases.put("DXP 2024.Q1.12", "2024-06-03");
		oldReleases.put("DXP 2024.Q1.11", "2024-05-27");
		oldReleases.put("DXP 2024.Q1.10", "2024-05-20");
		oldReleases.put("DXP 2024.Q1.9", "2024-05-13");
		oldReleases.put("DXP 2024.Q1.8", "2024-05-06");
		oldReleases.put("DXP 2024.Q1.7", "2024-04-30");
		oldReleases.put("DXP 2024.Q1.6", "2024-04-24");
		oldReleases.put("DXP 2024.Q1.5", "2024-04-08");
		oldReleases.put("DXP 2023.Q4.9", "2024-04-04");
		oldReleases.put("DXP 2024.Q1.4", "2024-04-02");
		oldReleases.put("DXP 2024.Q1.3", "2024-03-25");
		oldReleases.put("DXP 2024.Q1.2", "2024-03-18");
		oldReleases.put("DXP 2024.Q1.1", "2024-03-13");
		oldReleases.put("DXP 2023.Q4.8", "2024-03-04");
		oldReleases.put("DXP 2023.Q4.7", "2024-02-28");
		oldReleases.put("DXP 2023.Q4.6", "2024-02-21");
		oldReleases.put("DXP 2023.Q3.9", "2024-02-21");
		oldReleases.put("DXP 2023.Q4.5", "2024-02-07");
		oldReleases.put("DXP 2023.Q3.8", "2024-02-07");
		oldReleases.put("DXP 2023.Q4.4", "2024-01-30");
		oldReleases.put("DXP 2023.Q3.7", "2024-01-30");
		oldReleases.put("DXP 2023.Q4.3", "2024-01-25");
		oldReleases.put("DXP 2023.Q3.6", "2024-01-25");
		oldReleases.put("DXP 2023.Q3.5", "2024-01-18");
		oldReleases.put("DXP 2023.Q4.2", "2024-01-12");
		oldReleases.put("DXP 2023.Q4.1", "2024-01-10");
		oldReleases.put("DXP 2023.Q3.4", "2023-12-07");
		oldReleases.put("DXP 2023.Q4.0", "2023-12-06");
		oldReleases.put("DXP 2023.Q3.3", "2023-11-28");
		oldReleases.put("DXP 2023.Q3.2", "2023-10-31");
		oldReleases.put("DXP 2023.Q3.1", "2023-10-18");

		oldReleases.put("DXP 7.4.13 U112", "2024-02-26");
		oldReleases.put("DXP 7.4.13 U111", "2024-02-05");
		oldReleases.put("DXP 7.4.13 U110", "2024-01-23");
		oldReleases.put("DXP 7.4 U102", "2023-11-10");
		oldReleases.put("DXP 7.4 U101", "2023-11-03");
		oldReleases.put("DXP 7.4 U100", "2023-10-30");
		oldReleases.put("DXP 7.4 U99", "2023-10-20");
		oldReleases.put("DXP 7.4 U98", "2023-10-17");
		oldReleases.put("DXP 7.4 U97", "2023-10-10");
		oldReleases.put("DXP 7.4 U95", "2023-09-25");
		oldReleases.put("DXP 7.4 U94", "2023-09-15");
		oldReleases.put("DXP 7.4 U93", "2023-09-08");
		oldReleases.put("DXP 7.4 U92", "2023-09-01");
		oldReleases.put("DXP 7.4 U91", "2023-08-25");
		oldReleases.put("DXP 7.4 U90", "2023-08-18");
		oldReleases.put("DXP 7.4 U89", "2023-08-11");
		oldReleases.put("DXP 7.4 U88", "2023-08-04");
		oldReleases.put("DXP 7.4 U87", "2023-07-31");
		oldReleases.put("DXP 7.4 U86", "2023-07-25");
		oldReleases.put("DXP 7.4 U85", "2023-07-14");
		oldReleases.put("DXP 7.4 U84", "2023-07-07");
		oldReleases.put("DXP 7.4 U83", "2023-06-30");
		oldReleases.put("DXP 7.4 U81", "2023-06-16");
		oldReleases.put("DXP 7.4 U80", "2023-06-09");
		oldReleases.put("DXP 7.4 U79", "2023-06-02");
		oldReleases.put("DXP 7.4 U78", "2023-05-26");
		oldReleases.put("DXP 7.4 U77", "2023-05-19");
		oldReleases.put("DXP 7.4 U76", "2023-05-12");
		oldReleases.put("DXP 7.4 U75", "2023-05-05");
		oldReleases.put("DXP 7.4 U74", "2023-04-28");
		oldReleases.put("DXP 7.4 U73", "2023-04-21");
		oldReleases.put("DXP 7.4 U72", "2023-04-14");
		oldReleases.put("DXP 7.4 U71", "2023-04-07");
		oldReleases.put("DXP 7.4 U70", "2023-03-31");
		oldReleases.put("DXP 7.4 U69", "2023-03-24");
		oldReleases.put("DXP 7.4 U68", "2023-03-17");
		oldReleases.put("DXP 7.4 U67", "2023-03-10");
		oldReleases.put("DXP 7.4 U66", "2023-03-03");
		oldReleases.put("DXP 7.4 U65", "2023-02-24");
		oldReleases.put("DXP 7.4 U64", "2023-02-17");
		oldReleases.put("DXP 7.4 U63", "2023-02-10");
		oldReleases.put("DXP 7.4 U62", "2023-02-03");
		oldReleases.put("DXP 7.4 U61", "2023-01-27");
		oldReleases.put("DXP 7.4 U60", "2023-01-20");
		oldReleases.put("DXP 7.4 U59", "2023-01-13");
		oldReleases.put("DXP 7.4 U58", "2023-01-06");
		oldReleases.put("DXP 7.4 U57", "2022-12-29");
		oldReleases.put("DXP 7.4 U56", "2022-12-23");
		oldReleases.put("DXP 7.4 U55", "2022-12-16");
		oldReleases.put("DXP 7.4 U54", "2022-12-09");
		oldReleases.put("DXP 7.4 U53", "2022-12-02");
		oldReleases.put("DXP 7.4 U52", "2022-11-25");
		oldReleases.put("DXP 7.4 U51", "2022-11-18");
		oldReleases.put("DXP 7.4 U50", "2022-11-11");
		oldReleases.put("DXP 7.4 U49", "2022-11-04");
		oldReleases.put("DXP 7.4 U48", "2022-10-28");
		oldReleases.put("DXP 7.4 U47", "2022-10-21");
		oldReleases.put("DXP 7.4 U46", "2022-10-14");
		oldReleases.put("DXP 7.4 U45", "2022-10-07");
		oldReleases.put("DXP 7.4 U44", "2022-09-30");
		oldReleases.put("DXP 7.4 U43", "2022-09-23");
		oldReleases.put("DXP 7.4 U42", "2022-09-16");
		oldReleases.put("DXP 7.4 U41", "2022-09-09");
		oldReleases.put("DXP 7.4 U40", "2022-09-02");
		oldReleases.put("DXP 7.4 U39", "2022-08-26");
		oldReleases.put("DXP 7.4 U38", "2022-08-19");
		oldReleases.put("DXP 7.4 U37", "2022-08-12");
		oldReleases.put("DXP 7.4 U36", "2022-08-05");
		oldReleases.put("DXP 7.4 U35", "2022-07-29");
		oldReleases.put("DXP 7.4 U34", "2022-07-22");
		oldReleases.put("DXP 7.4 U33", "2022-07-15");
		oldReleases.put("DXP 7.4 U32", "2022-07-08");
		oldReleases.put("DXP 7.4 U31", "2022-07-01");
		oldReleases.put("DXP 7.4 U30", "2022-06-24");
		oldReleases.put("DXP 7.4 U29", "2022-06-17");
		oldReleases.put("DXP 7.4 U28", "2022-06-10");
		oldReleases.put("DXP 7.4 U27", "2022-06-03");
		oldReleases.put("DXP 7.4 U26", "2022-05-27");
		oldReleases.put("DXP 7.4 U25", "2022-05-20");
		oldReleases.put("DXP 7.4 U24", "2022-05-13");
		oldReleases.put("DXP 7.4 U23", "2022-05-06");
		oldReleases.put("DXP 7.4 U22", "2022-04-27");
		oldReleases.put("DXP 7.4 U21", "2022-04-22");
		oldReleases.put("DXP 7.4 U20", "2022-04-15");
		oldReleases.put("DXP 7.4 U19", "2022-04-08");
		oldReleases.put("DXP 7.4 U18", "2022-04-01");
		oldReleases.put("DXP 7.4 U17", "2022-03-25");
		oldReleases.put("DXP 7.4 U16", "2022-03-18");
		oldReleases.put("DXP 7.4 U15", "2022-03-12");
		oldReleases.put("DXP 7.4 U9", "2022-02-24");
		oldReleases.put("DXP 7.4 U8", "2022-02-18");
		oldReleases.put("DXP 7.4 U7", "2022-02-11");
		oldReleases.put("DXP 7.4 U6", "2022-02-04");
		oldReleases.put("DXP 7.4 U5", "2022-01-28");
		oldReleases.put("DXP 7.4 U4", "2022-01-19");
		oldReleases.put("DXP 7.4 U3", "2022-01-11");
		oldReleases.put("DXP 7.4 U2", "2021-12-29");
		oldReleases.put("DXP 7.4 U1", "2021-12-27");

		
		oldReleases.put("Portal 7.4.3.132 GA132", "2025-02-18");
		oldReleases.put("Portal 7.4.3.129 GA129", "2024-12-09");
		oldReleases.put("Portal 7.4.3.125 GA125", "2024-09-13");
		oldReleases.put("Portal 7.4.3.120 GA120", "2024-06-12");
		oldReleases.put("Portal 7.4 GA107", "2023-12-15");
		oldReleases.put("Portal 7.4 GA106", "2023-12-03");
		oldReleases.put("Portal 7.4 GA105", "2023-12-01");
		oldReleases.put("Portal 7.4 GA104", "2023-11-24");
		oldReleases.put("Portal 7.4 GA103", "2023-11-17");
		oldReleases.put("Portal 7.4 GA102", "2023-11-10");
		oldReleases.put("Portal 7.4 GA100", "2023-10-30");
		oldReleases.put("Portal 7.4 GA99", "2023-10-20");
		oldReleases.put("Portal 7.4 GA98", "2023-10-17");
		oldReleases.put("Portal 7.4 GA97", "2023-10-10");
		oldReleases.put("Portal 7.4 GA96", "2023-10-02");
		oldReleases.put("Portal 7.4 GA95", "2023-09-25");
		oldReleases.put("Portal 7.4 GA94", "2023-09-15");
		oldReleases.put("Portal 7.4 GA93", "2023-09-11");
		oldReleases.put("Portal 7.4 GA91", "2023-08-25");
		oldReleases.put("Portal 7.4 GA90", "2023-08-18");
		oldReleases.put("Portal 7.4 GA89", "2023-08-11");
		oldReleases.put("Portal 7.4 GA87", "2023-07-31");
		oldReleases.put("Portal 7.4 GA86", "2023-07-25");
		oldReleases.put("Portal 7.4 GA85", "2023-07-14");
		oldReleases.put("Portal 7.4 GA84", "2023-07-07");
		oldReleases.put("Portal 7.4 GA83", "2023-06-30");
		oldReleases.put("Portal 7.4 GA82", "2023-06-27");
		oldReleases.put("Portal 7.4 GA81", "2023-06-16");
		oldReleases.put("Portal 7.4 GA80", "2023-06-09");
		oldReleases.put("Portal 7.4 GA79", "2023-06-02");
		oldReleases.put("Portal 7.4 GA78", "2023-05-26");
		oldReleases.put("Portal 7.4 GA77", "2023-05-19");
		oldReleases.put("Portal 7.4 GA75", "2023-05-05");
		oldReleases.put("Portal 7.4 GA74", "2023-04-28");
		oldReleases.put("Portal 7.4 GA73", "2023-04-21");
		oldReleases.put("Portal 7.4 GA88", "2023-04-08");
		oldReleases.put("Portal 7.4 GA71", "2023-04-07");
		oldReleases.put("Portal 7.4 GA70", "2023-03-31");
		oldReleases.put("Portal 7.4 GA69", "2023-03-24");
		oldReleases.put("Portal 7.4 GA68", "2023-03-17");
		oldReleases.put("Portal 7.4 GA67", "2023-03-10");
		oldReleases.put("Portal 7.4 GA66", "2023-03-03");
		oldReleases.put("Portal 7.4 GA65", "2023-02-24");
		oldReleases.put("Portal 7.4 GA64", "2023-02-17");
		oldReleases.put("Portal 7.4 GA63", "2023-02-10");
		oldReleases.put("Portal 7.4 GA62", "2023-02-03");
		oldReleases.put("Portal 7.4 GA61", "2023-01-27");
		oldReleases.put("Portal 7.4 GA60", "2023-01-20");
		oldReleases.put("Portal 7.4 GA59", "2023-01-13");
		oldReleases.put("Portal 7.4 GA58", "2023-01-06");
		oldReleases.put("Portal 7.4 GA57", "2022-12-29");
		oldReleases.put("Portal 7.4 GA56", "2022-12-23");
		oldReleases.put("Portal 7.4 GA55", "2022-12-16");
		oldReleases.put("Portal 7.4 GA54", "2022-12-09");
		oldReleases.put("Portal 7.4 GA53", "2022-12-02");
		oldReleases.put("Portal 7.4 GA52", "2022-11-25");
		oldReleases.put("Portal 7.4 GA50", "2022-11-11");
		oldReleases.put("Portal 7.4 GA49", "2022-11-04");
		oldReleases.put("Portal 7.4 GA48", "2022-10-28");
		oldReleases.put("Portal 7.4 GA47", "2022-10-21");
		oldReleases.put("Portal 7.4 GA46", "2022-10-14");
		oldReleases.put("Portal 7.4 GA45", "2022-10-07");
		oldReleases.put("Portal 7.4 GA44", "2022-09-30");
		oldReleases.put("Portal 7.4 GA43", "2022-09-23");
		oldReleases.put("Portal 7.4 GA42", "2022-09-16");
		oldReleases.put("Portal 7.4 GA41", "2022-09-09");
		oldReleases.put("Portal 7.4 GA40", "2022-09-02");
		oldReleases.put("Portal 7.4 GA39", "2022-08-26");
		oldReleases.put("Portal 7.4 GA38", "2022-08-19");
		oldReleases.put("Portal 7.4 GA37", "2022-08-12");
		oldReleases.put("Portal 7.4 GA35", "2022-07-29");
		oldReleases.put("Portal 7.4 GA34", "2022-07-22");
		oldReleases.put("Portal 7.4 GA33", "2022-07-15");
		oldReleases.put("Portal 7.4 GA32", "2022-07-08");
		oldReleases.put("Portal 7.4 GA31", "2022-07-01");
		oldReleases.put("Portal 7.4 GA30", "2022-06-24");
		oldReleases.put("Portal 7.4 GA29", "2022-06-17");
		oldReleases.put("Portal 7.4 GA28", "2022-06-10");
		oldReleases.put("Portal 7.4 GA27", "2022-06-03");
		oldReleases.put("Portal 7.4 GA26", "2022-05-27");
		oldReleases.put("Portal 7.4 GA25", "2022-05-20");
		oldReleases.put("Portal 7.4 GA24", "2022-05-13");
		oldReleases.put("Portal 7.4 GA23", "2022-05-06");
		oldReleases.put("Portal 7.4 GA22", "2022-04-27");
		oldReleases.put("Portal 7.4 GA21", "2022-04-22");
		oldReleases.put("Portal 7.4 GA20", "2022-04-15");
		oldReleases.put("Portal 7.4 GA19", "2022-04-08");
		oldReleases.put("Portal 7.4 GA18", "2022-04-01");
		oldReleases.put("Portal 7.4 GA16", "2022-03-18");
		oldReleases.put("Portal 7.4 GA15", "2022-03-12");
		oldReleases.put("Portal 7.4 GA14", "2022-03-04");
		oldReleases.put("Portal 7.4 GA13", "2022-02-24");
		oldReleases.put("Portal 7.4 GA12", "2022-02-18");
		oldReleases.put("Portal 7.4 GA11", "2022-02-11");
		oldReleases.put("Portal 7.4 GA10", "2022-02-04");
		oldReleases.put("Portal 7.4 GA9", "2022-01-28");
		oldReleases.put("Portal 7.4 GA8", "2022-01-19");
		oldReleases.put("Portal 7.4 GA7", "2022-01-11");
		oldReleases.put("Portal 7.4 GA6", "2021-12-29");
		oldReleases.put("Portal 7.4 GA5", "2021-12-27");
		oldReleases.put("Portal 7.4 GA4", "2021-10-22");
		oldReleases.put("Portal 7.4 GA3", "2021-07-29");
		oldReleases.put("Portal 7.4 GA2", "2021-06-11");
		oldReleases.put("Portal 7.4 GA1", "2021-04-22");
	}
}
