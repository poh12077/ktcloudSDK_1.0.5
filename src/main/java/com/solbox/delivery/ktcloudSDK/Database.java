/*
package com.solbox.delivery.ktcloudSDK;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Database {

	static ServerInformation read(String vmId) throws Exception {
		final String driver = "org.mariadb.jdbc.Driver";
		final String DB_IP = "localhost";
		final String DB_PORT = "3307";
		final String DB_NAME = "ktcloud";
		final String DB_URL = "jdbc:mariadb://" + DB_IP + ":" + DB_PORT + "/" + DB_NAME;

		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;

		Class.forName(driver);
		conn = DriverManager.getConnection(DB_URL, "root", "1");
		if (conn != null) {
			System.out.println("DB connection success");
		}

		String sql = "select * from server";
		pstmt = conn.prepareStatement(sql);
		rs = pstmt.executeQuery();
		while (rs.next()) {
			if (vmId.equals(rs.getString("vmId")) ) {
				break;
			}
		}
		ServerInformation serverInformation = new ServerInformation(vmId, rs.getString("volumeID"), rs.getString("publicIP_ID"), rs.getString("staticNAT_ID"), rs.getString("projectID"));
		
		sql = "delete from server where vmid='"+vmId+"';";
		pstmt = conn.prepareStatement(sql);
		rs = pstmt.executeQuery();
		
		if (rs != null) {
			rs.close();
		}
		if (pstmt != null) {
			pstmt.close();
		}

		if (conn != null && !conn.isClosed()) {
			conn.close();
		}

		return serverInformation;
	}

	static void write(ServerInformation serverInformation) {
		final String driver = "org.mariadb.jdbc.Driver";
		final String DB_IP = "localhost";
		final String DB_PORT = "3307";
		final String DB_NAME = "ktcloud";
		final String DB_URL = "jdbc:mariadb://" + DB_IP + ":" + DB_PORT + "/" + DB_NAME;

		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;

		try {
			Class.forName(driver);
			conn = DriverManager.getConnection(DB_URL, "root", "1");
			if (conn != null) {
				System.out.println("DB connection success");
			}

		} catch (ClassNotFoundException e) {
			System.out.println("driver load fail");
			e.printStackTrace();
		} catch (SQLException e) {
			System.out.println("DB connection fail");
			e.printStackTrace();
		}

		try {
			String vmId = serverInformation.getVmId();
			String volumeId = serverInformation.getVolumeId();
			String publicIP_ID = serverInformation.getPublicIpId();
			String staticNAT_ID = serverInformation.getStaticNatId();
			String projectID = serverInformation.getProjectId();

			String info = "(\"" + vmId + "\",\"" + volumeId + "\",\"" + publicIP_ID + "\",\"" + staticNAT_ID + "\",\""
					+ projectID + "\");";
			String sql = "insert into server values " + info;
			pstmt = conn.prepareStatement(sql);
			rs = pstmt.executeQuery();

			
		} catch (SQLException e) {
			System.out.println("failed: " + e);
		} finally {
			try {
				if (rs != null) {
					rs.close();
				}
				if (pstmt != null) {
					pstmt.close();
				}

				if (conn != null && !conn.isClosed()) {
					conn.close();
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}

	}

}
*/
