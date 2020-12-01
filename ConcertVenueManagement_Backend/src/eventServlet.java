import java.io.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.Random;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

@WebServlet("/eventServlet")
public class eventServlet extends HttpServlet {

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
		
		String action = request.getParameter("action").trim();
		String name = request.getParameter("name");
		String newEventName = request.getParameter("new_event_name");
		String startTime = request.getParameter("start_time");
		String endTime = request.getParameter("end_time");
		String numTicketsAvailable = request.getParameter("tickets_available");
		String genre = request.getParameter("genre");
		String venue = request.getParameter("venue");
		String date = request.getParameter("date");
		boolean updated = false;
		
		if (action.equals("Get Venues")) {
			String venueList = null;
			try {
				venueList = getVenues();
			} catch (ClassNotFoundException e) {
				venueList = null;
			}
			out.print(venueList);
		}
		else if (action.equals("Get Events")) {
			String eventList = null;
			try {
				eventList = getEvents();
			} catch (ClassNotFoundException e) {
				eventList = null;
			}
			out.print(eventList);
		}
		else if (action.equals("Schedule Event")) {
			try {
				updated = scheduleEvent(name, startTime, endTime, numTicketsAvailable, genre,
						venue, date);
				if (updated) {
					out.print("Event Successfully Added! Please return to the Event Management Page!");
				}
				else {
					out.print("Error Adding Event. Please return to the Event Management Page.");
				}
				
			} catch (ClassNotFoundException e) {
				out.print("Error Adding Event. Please return to the Event Management Page.");
			}
			
		}
		else if (action.equals("Cancel Event")) {
			try {
				updated = cancelEvent(name);
				if (updated) {
					out.print("Event Successfully Deleted! Please return to the Event Management Page!");
				}
				else {
					out.print("Error Deleting Event. Please return to the Event Management Page.");
				}
			} catch (ClassNotFoundException e) {
				out.print("Error Deleting Event. Please return to the Event Management Page.");
			}
		}
		else {
			try {
				updated = editEvent(newEventName, name, startTime, endTime, numTicketsAvailable,
						genre, venue, date);
				if (updated) {
					out.print("Event Successfully Updated! Please return to the Event Management Page!");
				}
				else {
					out.print("Error Updating Event. Please return to the Event Management Page.");
				}
			} catch (ClassNotFoundException e) {
				out.print("Error Updating Event. Please return to the Event Management Page.");
			}
		}

	}
	
	/**
	 * Returns a semi colon delimited string of venues
	 * @return
	 * @throws ClassNotFoundException
	 */
	public static String getVenues() throws ClassNotFoundException {
		ArrayList<String> venues = new ArrayList<String>();
		String concatVenues = "";
		try {
			Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
			String connectionUrl = "jdbc:sqlserver://DESKTOP-RI159U3:1433;databaseName=NDI";
			Connection connection = DriverManager.getConnection(connectionUrl, USER, PASSWORD);
			String SQL = "SELECT * FROM VENUE";
			PreparedStatement statement = connection.prepareStatement(SQL);
			ResultSet rs = statement.executeQuery();
			String venue;

			while (rs.next()) {
				venue = rs.getString("VenueName");
				venues.add(venue);
			}
			if (venues.isEmpty()) {
				return null;
			}
			
			for (int i = 0; i < venues.size(); i++) {
				String currentVenue = venues.get(i);
				concatVenues += currentVenue + ";";
			}
			concatVenues = concatVenues.substring(0, concatVenues.length() - 1);
			return concatVenues;

		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * Returns a semi colon delimited string of events
	 * @return
	 * @throws ClassNotFoundException
	 */
	public static String getEvents() throws ClassNotFoundException {
		ArrayList<String> events = new ArrayList<String>();
		String concatEvents = "";
		try {
			Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
			String connectionUrl = "jdbc:sqlserver://DESKTOP-RI159U3:1433;databaseName=NDI";
			Connection connection = DriverManager.getConnection(connectionUrl, USER, PASSWORD);
			String SQL = "SELECT * FROM Event";
			PreparedStatement statement = connection.prepareStatement(SQL);
			ResultSet rs = statement.executeQuery();
			String event;

			while (rs.next()) {
				event = rs.getString("EventName");
				events.add(event);
			}
			if (events.isEmpty()) {
				return null;
			}
			
			for (int i = 0; i < events.size(); i++) {
				String currentVenue = events.get(i);
				concatEvents += currentVenue + ";";
			}
			concatEvents = concatEvents.substring(0, concatEvents.length() - 1);
			return concatEvents;

		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * Adds an event into the database
	 * @param name
	 * @param startTime
	 * @param endTime
	 * @param numTicketsAvailable
	 * @param genre
	 * @param venue
	 * @param date
	 * @return
	 * @throws ClassNotFoundException
	 */
	public static boolean scheduleEvent(String name, String startTime, String endTime, String numTicketsAvailable,
			String genre, String venue, String date) throws ClassNotFoundException {
		Random random = new Random();
		String id = String.format("%04d", random.nextInt(10000));
		String venueID = getVenueID(venue);
		try {
			Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
			String connectionUrl = "jdbc:sqlserver://DESKTOP-RI159U3:1433;databaseName=NDI";
			Connection connection = DriverManager.getConnection(connectionUrl, USER, PASSWORD);
			String SQL = "INSERT INTO Event VALUES (?,?,?,?,?,?,?,?)";
			PreparedStatement statement = connection.prepareStatement(SQL);
			statement.setString(1, id);
			statement.setString(2, startTime);
			statement.setString(3, endTime);
			statement.setString(4, numTicketsAvailable);
			statement.setString(5, genre);
			statement.setString(6, venueID);
			statement.setString(7, name);
			statement.setString(8, date);
			
			int insertedRows = statement.executeUpdate();
			return true;

		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
		
	}
	
	/**
	 * Removes an event within the database given an event name
	 * @param name
	 * @return
	 * @throws ClassNotFoundException
	 */
	public static boolean cancelEvent(String name) throws ClassNotFoundException {
		try {
			Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
			String connectionUrl = "jdbc:sqlserver://DESKTOP-RI159U3:1433;databaseName=NDI";
			Connection connection = DriverManager.getConnection(connectionUrl, USER, PASSWORD);
			String SQL = "DELETE FROM EVENT WHERE EventName = ?";
			PreparedStatement statement = connection.prepareStatement(SQL);
			statement.setString(1, name);
			
			int insertedRows = statement.executeUpdate();
			return true;

		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	/**
	 * Updates the event record given new values
	 * @param newEventName
	 * @param oldEventName
	 * @param startTime
	 * @param endTime
	 * @param numTicketsAvailable
	 * @param genre
	 * @param venue
	 * @param date
	 * @return
	 * @throws ClassNotFoundException
	 */
	public static boolean editEvent(String newEventName, String oldEventName, String startTime, String endTime, String numTicketsAvailable,
			String genre, String venue, String date) throws ClassNotFoundException {
		String venueID = getVenueID(venue);
		try {
			Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
			String connectionUrl = "jdbc:sqlserver://DESKTOP-RI159U3:1433;databaseName=NDI";
			Connection connection = DriverManager.getConnection(connectionUrl, USER, PASSWORD);
			String SQL = "UPDATE Event SET EventName = ?, StartTime = ?, EndTime = ?, NumTicketsAvailable = ?"
					+ ", Genre = ?, VENUE_VenueID = ?, Date = ? WHERE EventName = ?";
			PreparedStatement statement = connection.prepareStatement(SQL);
			statement.setString(1, newEventName);
			statement.setString(2, startTime);
			statement.setString(3, endTime);
			statement.setString(4, numTicketsAvailable);
			statement.setString(5, genre);
			statement.setString(6, venueID);
			statement.setString(7, date);
			statement.setString(8, oldEventName);
			
			int insertedRows = statement.executeUpdate();
			return true;

		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
		
	}
	
	/**
	 * Returns a venueID given an event name
	 * @param venueName
	 * @return
	 * @throws ClassNotFoundException
	 */
	public static String getVenueID(String venueName) throws ClassNotFoundException {
		String venueID;
		try {
			Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
			String connectionUrl = "jdbc:sqlserver://DESKTOP-RI159U3:1433;databaseName=NDI";
			Connection connection = DriverManager.getConnection(connectionUrl, USER, PASSWORD);
			String SQL = "SELECT * FROM Venue WHERE VenueName = ?";
			PreparedStatement statement = connection.prepareStatement(SQL);
			statement.setString(1, venueName);
			ResultSet rs = statement.executeQuery();

			boolean res = rs.next();
			venueID = rs.getString("VenueID");
			return venueID;
			

		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}
	

}
