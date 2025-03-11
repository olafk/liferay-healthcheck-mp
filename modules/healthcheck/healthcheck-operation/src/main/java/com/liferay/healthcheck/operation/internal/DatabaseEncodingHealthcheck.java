/**
 * SPDX-FileCopyrightText: (c) 2025 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.healthcheck.operation.internal;

import com.liferay.healthcheck.Healthcheck;
import com.liferay.healthcheck.HealthcheckItem;
import com.liferay.portal.kernel.dao.db.DB;
import com.liferay.portal.kernel.dao.db.DBManagerUtil;
import com.liferay.portal.kernel.dao.db.DBType;
import com.liferay.portal.kernel.dao.jdbc.DataAccess;
import com.liferay.portal.kernel.exception.PortalException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.LinkedList;

import org.osgi.service.component.annotations.Component;

@Component(
	configurationPid = "com.liferay.healthcheck.operation.internal.configuration.HealthcheckOperationalSystemConfiguration",
	service = Healthcheck.class
)
public class DatabaseEncodingHealthcheck implements Healthcheck {
	private static final String MSG = "healthcheck-database-encoding";
	private static final String MSG_SCHEMA_UNDETECTED = "healthcheck-database-schema-undetected";
	private static final String MSG_HSQL = "healthcheck-database-hsql-for-demo";
	private static final String MSG_DB_UNDETECTED="healthcheck-database-undetected";
	private static final String MSG_BETA="healthcheck-database-encoding-encoding-beta";
	private static final String[] ADDITIONAL_DOCUMENTATION = {
			"https://learn.liferay.com/w/dxp/installation-and-upgrades/installing-liferay/configuring-a-database"
	};
	
	@Override
	public Collection<HealthcheckItem> check(long companyId) throws Exception {
		Collection<HealthcheckItem> result = new LinkedList<HealthcheckItem>();
		String feedbackMail = "liferay-healthcheck_olafkock.de".replaceAll("_", "@"); // lame spam protection ;)
		
		DB db = DBManagerUtil.getDB();
		try(Connection connection = DataAccess.getConnection()) {
			DBType dbType = db.getDBType();
			String encoding = "undetected";
			boolean correctEncoding = false;
			String connectionURL = connection.getMetaData().getURL();; 
			String message = MSG;
			
			if(DBType.MYSQL.equals(dbType) || DBType.MARIADB.equals(dbType)) {
				String schema = extractMysqlSchema(connectionURL);
				if(schema != null) {
					PreparedStatement stmt = connection.prepareStatement(
							"SELECT default_character_set_name " +
							"FROM information_schema.SCHEMATA " + 
							"WHERE schema_name = ?");
					stmt.setString(1, schema);
					ResultSet rs = stmt.executeQuery();
					if(rs.next()) {
						encoding = rs.getString(1);
						if(encoding != null && encoding.toLowerCase().startsWith("utf8")) {
							correctEncoding = true;
						}
					}
				} else {
					result.add(new HealthcheckItem(false, ADDITIONAL_DOCUMENTATION, null, 
							MSG_SCHEMA_UNDETECTED, connectionURL));
					return result;
				}
			} else if(DBType.ORACLE.equals(dbType)) {
				message = MSG_BETA;
				PreparedStatement stmt = connection.prepareStatement(
						"SELECT NLS_CHARACTERSET, NLS_NCHAR_CHARACTERSET FROM v$nls_parameters");
				ResultSet rs = stmt.executeQuery();
				if(rs.next()) {
					String charset = rs.getString("NLS_CHARACTERSET");
					String ncharset = rs.getString("NLS_NCHAR_CHARACTERSET");
					if(charset != null && 
					   ncharset != null &&
					   charset.toLowerCase().indexOf("utf8")>-1 &&
					   ncharset.toLowerCase().indexOf("utf8")>-1) {
						correctEncoding = true;
						encoding = charset + "/" + ncharset;
					}
				}
			} else if(DBType.POSTGRESQL.equals(dbType)) {
				// https://stackoverflow.com/questions/6454146/getting-the-encoding-of-a-postgres-database
				encoding = "undetected";
				connectionURL = connection.getMetaData().getURL();
				String schema = extractMysqlSchema(connectionURL);
				if(schema != null) {
					PreparedStatement stmt = connection.prepareStatement(
							"SELECT character_set_name FROM information_schema.character_sets");
					ResultSet rs = stmt.executeQuery();
					if(rs.next()) {
						encoding = rs.getString("character_set_name");
						if(encoding != null && encoding.toLowerCase().startsWith("utf8")) {
							correctEncoding = true;
						}
					}
				} else {
					result.add(new HealthcheckItem(false, ADDITIONAL_DOCUMENTATION, null, 
							MSG_SCHEMA_UNDETECTED, connectionURL));
				}
			} else if(DBType.DB2.equals(dbType)) {
				// TODO
			} else if(DBType.SQLSERVER.equals(dbType)) {
				message = MSG_BETA;
			} else if(DBType.HYPERSONIC.equals(dbType)) {
				result.add(new HealthcheckItem(false, ADDITIONAL_DOCUMENTATION, null, MSG_HSQL));
				return result;
			} else {
				result.add(new HealthcheckItem(false, ADDITIONAL_DOCUMENTATION, null, 
						MSG_DB_UNDETECTED, feedbackMail));
				return result;
			}
			result.add(new HealthcheckItem(correctEncoding, ADDITIONAL_DOCUMENTATION, null,
					message, encoding, dbType, connectionURL, feedbackMail));
		}
		return result;
	}

	private String extractMysqlSchema(String connectionURL) {
		// assume something like jdbc:mariadb://localhost/lportal?andsomethingelse
		// very crude deciphering seems good enough (TM), 
		// the initial regexp-try looked like write-only code...
		int pos = connectionURL.indexOf("//");
		if(pos>=0) {
			pos = connectionURL.indexOf("/", pos+2);
			if(pos > -1) {
				String result = connectionURL.substring(pos+1);
				pos = result.indexOf("?");
				if(pos>-1) {
					result = result.substring(0, pos);
				}
				return result;
			}
		}
		return null;
	}

	@Override
	public String getCategory() {
		return "healthcheck-category-operation";
	}
}
