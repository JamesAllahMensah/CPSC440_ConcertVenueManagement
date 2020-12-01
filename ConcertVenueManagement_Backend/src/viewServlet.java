import java.io.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

@WebServlet("/viewServlet")
public class viewServlet extends HttpServlet {

	private static final String USER = "useraccount";
	private static final String PASSWORD = "password";

	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		doPost(request, response);
	}

	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {

		response.setContentType("text/html");
		PrintWriter out = response.getWriter();

		String sort = request.getParameter("sort");
		String genre = request.getParameter("genre");
		String venue = request.getParameter("venue");
		ArrayList<Event> events = null;
		
		try {
			events = getEvents(sort, genre, venue);
			for (Event e: events) {
				out.println(e.toString());
			}
		} catch (ClassNotFoundException e) {
			out.println(e);
		}
		

	}
	
	/**
	 * Returns an arraylist full of event objects
	 * @param sort
	 * @param genre
	 * @param venue
	 * @return
	 * @throws ClassNotFoundException
	 */
	public static ArrayList<Event> getEvents(String sort, String genre, String venue) throws ClassNotFoundException {
		ArrayList<Event> events = new ArrayList<Event>();
		ArrayList<String> rsInfo = null;
		String SQL = null;
		try {
			Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
			String connectionUrl = "jdbc:sqlserver://DESKTOP-RI159U3:1433;databaseName=NDI";
			Connection connection = DriverManager.getConnection(connectionUrl, USER, PASSWORD);
			SQL = getSQL(sort, genre, venue);
			
			Statement statement = connection.createStatement();
			ResultSet rs = statement.executeQuery(SQL);
			
			String id, startTime, endTime, numTicketsAvailable, eventGenre, venueID, venueName, name, date;

			while (rs.next()) {
				id = rs.getString("EventID");
				startTime = rs.getString("startTime");
				endTime = rs.getString("EndTime");
				numTicketsAvailable = rs.getString("NumTicketsAvailable");
				eventGenre = rs.getString("Genre");
				venueID = rs.getString("VENUE_VenueID");
				venueName = getVenue(venueID);
				name = rs.getString("EventName");
				date = rs.getString("Date");
				
				Event event = new Event(id, startTime, endTime, numTicketsAvailable, eventGenre, venueName, name, date);
				events.add(event);
			}
			
			return events;

		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * Returns the name of a venue given an ID
	 * @param venueID
	 * @return
	 * @throws ClassNotFoundException
	 */
	public static String getVenue(String venueID) throws ClassNotFoundException {
		ArrayList<String> venues = new ArrayList<String>();
		String venue = null;
		try {
			Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
			String connectionUrl = "jdbc:sqlserver://DESKTOP-RI159U3:1433;databaseName=NDI";
			Connection connection = DriverManager.getConnection(connectionUrl, USER, PASSWORD);
			String SQL = "SELECT * FROM Venue WHERE VenueID = ?";
			PreparedStatement statement = connection.prepareStatement(SQL);
			statement.setString(1, venueID);
			ResultSet rs = statement.executeQuery();

			boolean res = rs.next();
			venue = rs.getString("VenueName");
			return venue;

		} catch (SQLException e) {
			e.printStackTrace();
			return venue;
		}
	}
	
	/**
	 * Returns the ID of a venue given a Venue Name
	 * @param venue
	 * @return
	 * @throws ClassNotFoundException
	 */
	public static String getVenueID(String venue) throws ClassNotFoundException {
		ArrayList<String> venues = new ArrayList<String>();
		String venueID = null;
		try {
			Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
			String connectionUrl = "jdbc:sqlserver://DESKTOP-RI159U3:1433;databaseName=NDI";
			Connection connection = DriverManager.getConnection(connectionUrl, USER, PASSWORD);
			String SQL = "SELECT * FROM Venue WHERE VenueName = ?";
			PreparedStatement statement = connection.prepareStatement(SQL);
			statement.setString(1, venue);
			ResultSet rs = statement.executeQuery();

			boolean res = rs.next();
			venueID = rs.getString("venueID");
			return venueID;

		} catch (SQLException e) {
			e.printStackTrace();
			return venueID;
		}
	}
	
	/**
	 * Returns a SQL statement given filters and sort values
	 * @param sort
	 * @param genre
	 * @param venue
	 * @return
	 * @throws ClassNotFoundException
	 */
	public static String getSQL(String sort, String genre, String venue) throws ClassNotFoundException {
		String mainSQL = "SELECT * FROM Event";
		if (sort.equals("Default") && genre.equals("Any") && venue.equals("Any")) {
			return mainSQL;
		}

		if (!sort.equals("Default")) {
			if (!genre.equals("Any") || !venue.equals("Any")) {
				String venueID = getVenueID(venue);
				if (!genre.equals("Any")) {
					mainSQL += " WHERE Genre = \'" + genre + "\'";
				}
				if (!venue.equals("Any")) {
					mainSQL += " WHERE VENUE_VenueID = \'" + venueID + "\'";
				}

				switch (sort) {

				case ("Newest"):
					mainSQL += " ORDER BY Date";
				break;

				case ("Oldest"):
					mainSQL += " ORDER BY Date DESC";
				break;

				case ("A-Z"):
					mainSQL += " ORDER BY EventName";
				break;

				case ("Z-A"):
					mainSQL += " ORDER BY EventName DESC";
				break;
				}

				mainSQL = manipulateSQL(mainSQL);

			} else {
				switch (sort) {

				case ("Newest"):
					mainSQL += " ORDER BY Date";
					return mainSQL;

				case ("Oldest"):
					mainSQL += " ORDER BY Date DESC";
					return mainSQL;

				case ("A-Z"):
					mainSQL += " ORDER BY EventName";
					return mainSQL;

				case ("Z-A"):
					mainSQL += " ORDER BY EventName DESC";
					return mainSQL;

				}
			}

		} else {
			if (!genre.equals("Any") || !venue.equals("Any")) { // If sort and filter
				if (!genre.equals("Any")) {
					mainSQL += " WHERE Genre = \'" + genre + "\'";
				}
				if (!venue.equals("Any")) {
					String venueID = getVenueID(venue);
					mainSQL += " WHERE VENUE_VenueID = \'" + venueID + "\'";
				}

				mainSQL = manipulateSQL(mainSQL);

			}

		}

		return mainSQL;
	}
	
	/**
	 * Reformats the SQL statement to avoid errors
	 * @param statement
	 * @return
	 */
	public static String manipulateSQL(String statement) {
		String newStatement = "";
		int whereCnt = 0;
		String [] splitStatement = statement.split(" ");
		for (String s: splitStatement) {
			if (s.equals("WHERE")) {
				whereCnt++;
			}
		}
		
		if (whereCnt > 1) {
			ArrayList<Integer> indexes = new ArrayList<Integer>();
			for (int i = 0; i < splitStatement.length; i++) {
				String token = splitStatement[i];
				if (token.equals("WHERE")) {
					indexes.add(i);
				}
			}
			
			indexes.remove(0);
			
			for (Integer index: indexes) {
				splitStatement[index] = "AND";
			}
		}
		else {
			return statement;
		}
		
		for (String s: splitStatement) {
			newStatement += s + " ";
		}
		newStatement = newStatement.trim();
		
		return newStatement;
	}
	

}
