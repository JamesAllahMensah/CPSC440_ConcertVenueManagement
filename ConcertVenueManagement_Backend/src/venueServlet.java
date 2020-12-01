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

@WebServlet("/venueServlet")
public class venueServlet extends HttpServlet {

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
		String location = request.getParameter("location");
		String capacity = request.getParameter("capacity");
		String origVenueName = request.getParameter("venue");
		
		boolean updated = false;
		
		if (action.equals("Add Venue")) {
			try {
				updated = addVenue(name, location, capacity);
				if (updated) {
					out.print("Venue Successfully Added! Please return to the Venue Management Page!");
				}
				else {
					out.print("Error Adding Venue! Please return to the Venue Management Page!");
				}
			} catch (ClassNotFoundException e) {
				out.print("Error Adding Venue! Please return to the Venue Management Page!");
			}

		}
		else if (action.equals("Get Venues")) {
			String venueList = null;
			try {
				venueList = getVenues();
			}
			catch (ClassNotFoundException e) {
				venueList = null;
			}
			out.print(venueList);
		}
		else if (action.equals("Remove Venue")) {
			try {
				updated = deleteVenue(name);
				if (updated) {
					out.print("Venue Successfully Deleted! Please return to the Venue Management Page!");
				}
				else {
					out.print("Error Deleting Venue! Please return to the Venue Management Page!");
				}
			} catch (ClassNotFoundException e) {
				out.print("Error Deleting Venue! Please return to the Venue Management Page!");
			}
		}
		else {
			try {
				updated = editVenue(origVenueName, name, location, capacity);
				if (updated) {
					out.print("Venue Successfully Updated! Please return to the Venue Management Page!");
				}
				else {
					out.print("Error Updating Venue! Please return to the Venue Management Page!");
				}
			} catch (ClassNotFoundException e) {
				out.print("Error Updating Venue! Please return to the Venue Management Page!");
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
	 * Adds venue into the backend given newly inputted attributes
	 * @param name
	 * @param location
	 * @param capacity
	 * @return
	 * @throws ClassNotFoundException
	 */
	public static boolean addVenue(String name, String location, String capacity) throws ClassNotFoundException {
		Random random = new Random();
		String venueID = String.format("%04d", random.nextInt(10000));
		
		try {
			Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
			String connectionUrl = "jdbc:sqlserver://DESKTOP-RI159U3:1433;databaseName=NDI";
			Connection connection = DriverManager.getConnection(connectionUrl, USER, PASSWORD);
			String SQL = "INSERT INTO Venue VALUES (?,?,?,?)";
			PreparedStatement statement = connection.prepareStatement(SQL);
			statement.setString(1, venueID);
			statement.setString(2, name);
			statement.setString(3, location);
			statement.setString(4, capacity);
			
			int insertedRows = statement.executeUpdate();
			return true;

		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
		
	}
	
	/**
	 * Deletes venue from the database given associated venue name
	 * @param name
	 * @return
	 * @throws ClassNotFoundException
	 */
	public static boolean deleteVenue(String name) throws ClassNotFoundException {
		try {
			Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
			String connectionUrl = "jdbc:sqlserver://DESKTOP-RI159U3:1433;databaseName=NDI";
			Connection connection = DriverManager.getConnection(connectionUrl, USER, PASSWORD);
			String SQL = "DELETE FROM Venue WHERE VenueName = ?";
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
	 * Updates venue entry in the database with newly inputted values
	 * @param newVenueName
	 * @param name
	 * @param location
	 * @param capacity
	 * @return
	 * @throws ClassNotFoundException
	 */
	public static boolean editVenue(String newVenueName, String name, String location, String capacity) throws ClassNotFoundException {
		try {
			Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
			String connectionUrl = "jdbc:sqlserver://DESKTOP-RI159U3:1433;databaseName=NDI";
			Connection connection = DriverManager.getConnection(connectionUrl, USER, PASSWORD);
			String SQL = "UPDATE Venue SET VenueName = ?, Location = ?, Capacity = ? WHERE VenueName = ?";
			PreparedStatement statement = connection.prepareStatement(SQL);
			statement.setString(1, name);
			statement.setString(2, location);
			statement.setString(3, capacity);
			statement.setString(4, newVenueName);
			
			int insertedRows = statement.executeUpdate();
			return true;

		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
		
	}


}
