package com.trihydro.cvdatacontroller.controller;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.trihydro.library.model.WydotRsu;
import com.trihydro.library.model.WydotRsuTim;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import springfox.documentation.annotations.ApiIgnore;

@CrossOrigin
@RestController
@ApiIgnore
public class RsuController extends BaseController {

	@RequestMapping(value = "/rsus", method = RequestMethod.GET, headers = "Accept=application/json")
	public ResponseEntity<List<WydotRsu>> SelectAllRsus() {
		ArrayList<WydotRsu> rsus = new ArrayList<WydotRsu>();
		Connection connection = null;
		ResultSet rs = null;
		Statement statement = null;

		try {
			connection = GetConnectionPool();
			statement = connection.createStatement();

			// select all RSUs from RSU table
			rs = statement.executeQuery(
					"select * from rsu inner join rsu_vw on rsu.deviceid = rsu_vw.deviceid order by milepost asc");

			while (rs.next()) {
				WydotRsu rsu = new WydotRsu();
				rsu.setRsuId(rs.getInt("RSU_ID"));
				rsu.setRsuTarget(rs.getString("IPV4_ADDRESS"));
				rsu.setLatitude(rs.getDouble("LATITUDE"));
				rsu.setLongitude(rs.getDouble("LONGITUDE"));
				rsu.setRoute(rs.getString("ROUTE"));
				rsu.setMilepost(rs.getDouble("MILEPOST"));
				rsus.add(rsu);
			}

		} catch (SQLException e) {
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(rsus);
		} finally {
			try {
				// close prepared statement
				if (statement != null)
					statement.close();
				// return connection back to pool
				if (connection != null)
					connection.close();
				// close result set
				if (rs != null)
					rs.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}

		return ResponseEntity.ok(rsus);
	}

	@RequestMapping(value = "/selectActiveRSUs", method = RequestMethod.GET, headers = "Accept=application/json")
	public ResponseEntity<List<WydotRsu>> SelectActiveRsus() {
		List<WydotRsu> rsus = new ArrayList<WydotRsu>();
		Connection connection = null;
		ResultSet rs = null;
		Statement statement = null;

		try {
			connection = GetConnectionPool();
			statement = connection.createStatement();

			// select all RSUs that are labeled as 'Existing' in the WYDOT view
			rs = statement.executeQuery(
					"select rsu.*, rsu_vw.latitude, rsu_vw.longitude, rsu_vw.ipv4_address from rsu inner join rsu_vw on rsu.deviceid = rsu_vw.deviceid where rsu_vw.status = 'Existing'");

			while (rs.next()) {
				WydotRsu rsu = new WydotRsu();
				// rsu.setRsuId(rs.getInt("rsu_id"));
				rsu.setRsuTarget(rs.getString("IPV4_ADDRESS"));
				rsu.setLatitude(rs.getDouble("LATITUDE"));
				rsu.setLongitude(rs.getDouble("LONGITUDE"));
				rsus.add(rsu);
			}
		} catch (SQLException e) {
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(rsus);
		} finally {
			try {
				// close prepared statement
				if (statement != null)
					statement.close();
				// return connection back to pool
				if (connection != null)
					connection.close();
				// close result set
				if (rs != null)
					rs.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return ResponseEntity.ok(rsus);
	}

	@RequestMapping(method = RequestMethod.GET, value = "/selectRsusInBuffer/{direction}/{startingMilepost}/{endingMilepost}/{route}")
	public ResponseEntity<List<WydotRsu>> SelectRsusInBuffer(@PathVariable String direction,
			@PathVariable Double startingMilepost, @PathVariable Double endingMilepost, @PathVariable String route) {
		Connection connection = null;
		ResultSet rs = null;
		Statement statement = null;
		List<WydotRsu> rsus = new ArrayList<WydotRsu>();
		int buffer = 5;

		try {
			connection = GetConnectionPool();
			statement = connection.createStatement();

			if (direction.toLowerCase().equals("i")) {
				Double startBuffer = startingMilepost - buffer;
				String selectStatement = "select rsu.*, rsu_vw.latitude, rsu_vw.longitude, rsu_vw.ipv4_address from rsu ";
				selectStatement += "inner join rsu_vw on rsu.deviceid = rsu_vw.deviceid ";
				selectStatement += "where rsu_vw.status = 'Existing' and rsu_vw.milepost >= " + startBuffer;
				selectStatement += " and rsu_vw.milepost <= " + endingMilepost + " and rsu_vw.route = '" + route + "'";
				rs = statement.executeQuery(selectStatement);
			} else {
				Double startBuffer = endingMilepost + buffer;
				String selectStatement = "select rsu.*, rsu_vw.latitude, rsu_vw.longitude, rsu_vw.ipv4_address from rsu ";
				selectStatement += "inner join rsu_vw on rsu.deviceid = rsu_vw.deviceid ";
				selectStatement += "where rsu_vw.status = 'Existing' and rsu_vw.milepost >= " + startingMilepost;
				selectStatement += " and rsu_vw.milepost <= " + startBuffer + " and rsu_vw.route = '" + route + "'";
				rs = statement.executeQuery(selectStatement);
			}

			while (rs.next()) {
				WydotRsu rsu = new WydotRsu();
				rsu.setRsuId(rs.getInt("RSU_ID"));
				rsu.setRsuTarget(rs.getString("IPV4_ADDRESS"));
				rsu.setLatitude(rs.getDouble("LATITUDE"));
				rsu.setLongitude(rs.getDouble("LONGITUDE"));
				rsus.add(rsu);
			}
		} catch (SQLException e) {
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(rsus);
		} finally {
			try {
				// close prepared statement
				if (statement != null)
					statement.close();
				// return connection back to pool
				if (connection != null)
					connection.close();
				// close result set
				if (rs != null)
					rs.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return ResponseEntity.ok(rsus);
	}

	@RequestMapping(method = RequestMethod.GET, value = "/rsus-for-tim/{timId}")
	public ResponseEntity<List<WydotRsuTim>> GetFullRsusTimIsOn(@PathVariable Long timId) {
		List<WydotRsuTim> rsus = new ArrayList<WydotRsuTim>();
		Connection connection = null;
		ResultSet rs = null;
		Statement statement = null;

		try {
			connection = GetConnectionPool();
			statement = connection.createStatement();

			// select all RSUs that are labeled as 'Existing' in the WYDOT view
			rs = statement.executeQuery(
					"select rsu.*, tim_rsu.rsu_index, rsu_vw.latitude, rsu_vw.longitude, rsu_vw.ipv4_address from rsu inner join rsu_vw on rsu.deviceid = rsu_vw.deviceid inner join tim_rsu on tim_rsu.rsu_id = rsu.rsu_id where tim_rsu.tim_id = "
							+ timId);

			while (rs.next()) {
				WydotRsuTim rsu = new WydotRsuTim();
				rsu.setRsuTarget(rs.getString("IPV4_ADDRESS"));
				rsu.setLatitude(rs.getDouble("LATITUDE"));
				rsu.setLongitude(rs.getDouble("LONGITUDE"));
				rsu.setIndex(rs.getInt("RSU_INDEX"));
				rsu.setRsuUsername(rs.getString("UPDATE_USERNAME"));
				rsu.setRsuPassword(rs.getString("UPDATE_PASSWORD"));
				rsus.add(rsu);
			}
		} catch (SQLException e) {
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(rsus);
		} finally {
			try {
				// close prepared statement
				if (statement != null)
					statement.close();
				// return connection back to pool
				if (connection != null)
					connection.close();
				// close result set
				if (rs != null)
					rs.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return ResponseEntity.ok(rsus);
	}

	@RequestMapping(method = RequestMethod.GET, value = "/rsus-by-route/{route}")
	public ResponseEntity<ArrayList<WydotRsu>> SelectRsusByRoute(@PathVariable String route) {
		ArrayList<WydotRsu> rsus = new ArrayList<WydotRsu>();
		Connection connection = null;
		ResultSet rs = null;
		Statement statement = null;

		try {
			connection = GetConnectionPool();
			statement = connection.createStatement();

			// select all RSUs from RSU table
			rs = statement.executeQuery(
					"select * from rsu inner join rsu_vw on rsu.deviceid = rsu_vw.deviceid where rsu_vw.route like '%"
							+ route + "%' and rsu_vw.status = 'Existing' order by milepost asc");

			while (rs.next()) {
				WydotRsu rsu = new WydotRsu();
				rsu.setRsuId(rs.getInt("RSU_ID"));
				rsu.setRsuTarget(rs.getString("IPV4_ADDRESS"));
				rsu.setLatitude(rs.getDouble("LATITUDE"));
				rsu.setLongitude(rs.getDouble("LONGITUDE"));
				rsu.setRoute(rs.getString("ROUTE"));
				rsu.setMilepost(rs.getDouble("MILEPOST"));
				rsus.add(rsu);
			}
		} catch (SQLException e) {
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(rsus);
		} finally {
			try {
				// close prepared statement
				if (statement != null)
					statement.close();
				// return connection back to pool
				if (connection != null)
					connection.close();
				// close result set
				if (rs != null)
					rs.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return ResponseEntity.ok(rsus);
	}
}
