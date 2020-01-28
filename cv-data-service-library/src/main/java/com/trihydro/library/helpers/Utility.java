package com.trihydro.library.helpers;

import static java.lang.Math.toIntExact;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import com.trihydro.library.model.WydotRsu;
import com.trihydro.library.service.RsuService;

public class Utility {
	public static Gson gson = new Gson();

	public static void logWithDate(String msg) {
		Date date = new Date();
		System.out.println(date + " " + msg);
	}

	public static int getMinutesDurationBetweenTwoDates(String startDateTime, String endDateTime) {

		int duration = getMinutesDurationWithSimpleDateFormat(startDateTime, endDateTime);
		if (duration == -1) {
			duration = getMinutesDurationWithZonedDateTime(startDateTime, endDateTime);
		}
		if (duration == -1) {
			System.out.println(
					"Failed to parse dates when getting minutes between: " + startDateTime + " and " + endDateTime);

		}
		return duration;
	}

	/**
	 * Attempt to get duration in minutes between two dates parsed as ZonedDateTime.
	 * If this fails, return -1
	 * 
	 * @param startDateTime
	 * @param endDateTime
	 * @return The duration in minutes between the two given dates. If parsing
	 *         fails, returns -1
	 */
	private static int getMinutesDurationWithZonedDateTime(String startDateTime, String endDateTime) {
		try {
			ZonedDateTime zdtStart = ZonedDateTime.parse(startDateTime);
			ZonedDateTime zdtEnd = ZonedDateTime.parse(endDateTime);

			java.time.Duration dateDuration = java.time.Duration.between(zdtStart, zdtEnd);
			long durationTime = Math.abs(dateDuration.toMinutes());

			return toIntExact(durationTime);
		} catch (DateTimeParseException exception) {
			return -1;
		}
	}

	/**
	 * Attempt to get duration in minutes between two dates parsed in
	 * SimpleDateFormat("dd-MMM-yy HH.MM.SS"). If parsing fails, returns -1
	 * 
	 * @param startDateTime
	 * @param endDateTime
	 * @return The duration in minutes between the two given dates. If parsing
	 *         fails, returns -1
	 */
	private static int getMinutesDurationWithSimpleDateFormat(String startDateTime, String endDateTime) {
		try {
			SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MMM-yy HH.MM.SS");
			Date startDate = simpleDateFormat.parse(startDateTime);
			Date endDate = simpleDateFormat.parse(endDateTime);

			long duration = (endDate.getTime() - startDate.getTime()) / 60000; // milliseconds to minutes is 1/60000
			return toIntExact(duration);
		} catch (Exception ex) {
			return -1;
		}
	}

	/**
	 * Returns the value presented by the ResultSet at key if available. If the
	 * value presented is null, defaults to 1 (rather than short default of 0). Used
	 * for our purposes to default to 1 for various values.
	 * 
	 * @param rs  The ResultSet to pull data from
	 * @param key The key value to use to get data from the ResultSet
	 * @return Short value defaulted to 1 if not found
	 */
	public static short GetShortValueFromResultSet(ResultSet rs, String key) {
		try {
			String value = rs.getString(key);
			if (value != null) {
				return new Short(value);
			}
		} catch (SQLException ex) {
			System.out.println("Error attempting to get short value '" + key + "' from ResultSet");
		}
		return (short) 1;
	}

	public static int getDirection(Double bearing) {

		int direction = 0;

		if (bearing >= 0 && bearing <= 22.5)
			direction = 1;
		else if (bearing > 22.5 && bearing <= 45)
			direction = 2;
		else if (bearing > 45 && bearing <= 67.5)
			direction = 4;
		else if (bearing > 67.5 && bearing <= 90)
			direction = 8;
		else if (bearing > 90 && bearing <= 112.5)
			direction = 16;
		else if (bearing > 112.5 && bearing <= 135)
			direction = 32;
		else if (bearing > 135 && bearing <= 157.5)
			direction = 64;
		else if (bearing > 157.5 && bearing <= 180)
			direction = 128;
		else if (bearing > 180 && bearing <= 202.5)
			direction = 256;
		else if (bearing > 202.5 && bearing <= 225)
			direction = 512;
		else if (bearing > 225 && bearing <= 247.5)
			direction = 1024;
		else if (bearing > 247.5 && bearing <= 270)
			direction = 2048;
		else if (bearing > 270 && bearing <= 292.5)
			direction = 4096;
		else if (bearing > 292.5 && bearing <= 315)
			direction = 8192;
		else if (bearing > 315 && bearing <= 337.5)
			direction = 16384;
		else if (bearing > 337.5 && bearing <= 360)
			direction = 32768;

		return direction;
	}

	private static List<WydotRsu> getRsusByRoute(String route) {
		List<WydotRsu> rsus = RsuService.selectRsusByRoute(route);
		for (WydotRsu rsu : rsus) {
			rsu.setRsuRetries(3);
			rsu.setRsuTimeout(5000);
		}
		return rsus;
	}

	public static List<WydotRsu> getRsusInBuffer(String direction, Double lowerMilepost, Double higherMilepost,
			String route) {

		List<WydotRsu> rsus = new ArrayList<>();
		Comparator<WydotRsu> compMilepost = (l1, l2) -> Double.compare(l1.getMilepost(), l2.getMilepost());
		WydotRsu entryRsu = null;
		// WydotRsu rsuHigher;

		// if there are no rsus on this route
		List<WydotRsu> mainRsus = getRsusByRoute(route);
		if (mainRsus.size() == 0) {
			Utility.logWithDate("No RSUs found for route " + route);
			return rsus;
		} else {
			Utility.logWithDate("Found the following RSUs for route " + route + ": ");
			System.out.println(gson.toJson(mainRsus));
		}

		if (direction.equals("eastbound")) {

			// get rsus at mileposts less than your milepost
			List<WydotRsu> rsusLower = mainRsus.stream().filter(x -> x.getMilepost() < lowerMilepost)
					.collect(Collectors.toList());

			if (rsusLower.size() == 0) {
				// if no rsus found farther west than lowerMilepost
				// example: higherMilepost = 12, lowerMilepost = 2, no RSUs at mileposts < 2
				// find milepost furthest west than milepost of TIM location
				rsusLower = mainRsus.stream().filter(x -> x.getMilepost() < higherMilepost)
						.collect(Collectors.toList());

				// example: RSU at milepost 7.5 found
				entryRsu = rsusLower.stream().min(compMilepost).get();

				if ((lowerMilepost - entryRsu.getMilepost()) > 20) {
					// don't send to RSU if its further that X amount of miles away
					entryRsu = null;
				}
			}
			// else find milepost closest to lowerMilepost
			else {
				// get max from that list
				entryRsu = rsusLower.stream().max(compMilepost).get();
			}

		} else { // westbound

			// get rsus at mileposts greater than your milepost
			List<WydotRsu> rsusHigher = mainRsus.stream().filter(x -> x.getMilepost() > higherMilepost)
					.collect(Collectors.toList());

			if (rsusHigher.size() == 0) {
				Utility.logWithDate("No RSUs found higher than 'high' milepost " + higherMilepost);
				rsusHigher = mainRsus.stream().filter(x -> x.getMilepost() > lowerMilepost)
						.collect(Collectors.toList());

				if (rsusHigher.size() == 0) {
					Utility.logWithDate("No RSUs found higher than 'low' milepost: " + lowerMilepost);
				}

				// get min from that list
				entryRsu = rsusHigher.stream().max(compMilepost).get();
			} else {
				entryRsu = rsusHigher.stream().min(compMilepost).get();
			}

			if (entryRsu != null && (entryRsu.getMilepost() - higherMilepost) > 20) {
				// don't send to RSU if its further than 20 miles away
				Utility.logWithDate("Entry RSU is > 20 miles from the affected area, removing it from the list");
				entryRsu = null;
			}
		}

		rsus = mainRsus.stream().filter(x -> x.getMilepost() >= lowerMilepost && x.getMilepost() <= higherMilepost)
				.collect(Collectors.toList());

		if (entryRsu != null)
			rsus.add(entryRsu);

		return rsus;
	}

	/**
	 * Creates a connection with authentication via an apikey and returning JSON.
	 * Used to send HTTP requests to the SDX api
	 * 
	 * @param method The HTTP method to use (GET,POST,PUT,DELETE)
	 * @param url    The URL to send the request to
	 * @param apiKey The apikey value to apply in the header
	 * @return
	 * @throws IOException
	 */
	public static HttpURLConnection getSdxUrlConnection(String method, URL url, String apiKey) throws IOException {
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setRequestMethod(method);
		conn.setRequestProperty("Accept", "application/json");
		conn.setRequestProperty("apikey", apiKey);

		return conn;
	}
}