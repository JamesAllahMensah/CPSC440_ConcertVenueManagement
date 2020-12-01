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

@WebServlet("/performerServlet")
public class performerServlet extends HttpServlet {

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
		String age = request.getParameter("age");
		String sex = request.getParameter("sex");
		String origPerformer = request.getParameter("performer");
		String band = request.getParameter("band");
		
		boolean updated = false;
		if (action.equals("Add Performer")) {
			try {
				updated = addPerformer(name, age, sex, band);
			} catch (ClassNotFoundException e) {
				updated = false;
			}
			if (updated) {
				out.print("Performer Successfully Added! Please return to the Performer Management Page!");
			}
			else {
				out.print("Error Removing Performer. Please return to the Performer Management Page.");
			}
		}
		else if (action.equals("Edit Performer")) {
			try {
				updated = editPerformer(name, origPerformer, band);
			} catch (ClassNotFoundException e) {
				updated = false;
			}
			if (updated) {
				out.print("Perfomer Successfully Edited! Please return to the Performer Management Page!");
			}
			else {
				out.print("Error Editing Performer! Please return to the Performer Management Page!");
			}
		}
		else {
			try {
				updated = removePerformer(origPerformer);
			} catch (ClassNotFoundException e) {
				updated = false;
			}
			if (updated) {
				out.print("Perfomer Successfully Removed! Please return to the Performer Management Page!");
			}
			else {
				out.print("Error Removing Performer! Please return to the Performer Management Page!");
			}
		}
		
	}
	
	/**
	 * Adds performer into the database
	 * @param name
	 * @param age
	 * @param sex
	 * @param band
	 * @return
	 * @throws ClassNotFoundException
	 */
	public static boolean addPerformer(String name, String age, String sex, String band) throws ClassNotFoundException {
		Random random = new Random();
		String bandID = getBandID(band);
		String eventID = getEventID(bandID);
		String performerID = String.format("%04d", random.nextInt(10000));
		
		try {
			Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
			String connectionUrl = "jdbc:sqlserver://DESKTOP-RI159U3:1433;databaseName=NDI";
			Connection connection = DriverManager.getConnection(connectionUrl, USER, PASSWORD);
			String SQL = "INSERT INTO Performer Values (?,?,?,?,?,?)";
			PreparedStatement statement = connection.prepareStatement(SQL);
			statement.setString(1, performerID);
			statement.setString(2, name);
			statement.setString(3, age);
			statement.setString(4, sex);
			statement.setString(5, bandID);
			statement.setString(6, eventID);

			int insertedRows = statement.executeUpdate();
			return true;

		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	/**
	 * Updates values associated with an event in the database
	 * @param name
	 * @param performer
	 * @param band
	 * @return
	 * @throws ClassNotFoundException
	 */
	public static boolean editPerformer(String name, String performer, String band) throws ClassNotFoundException {
		String bandID = getBandID(band);
		try {
			Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
			String connectionUrl = "jdbc:sqlserver://DESKTOP-RI159U3:1433;databaseName=NDI";
			Connection connection = DriverManager.getConnection(connectionUrl, USER, PASSWORD);
			String SQL = "UPDATE Performer SET PerformerName = ?, BAND_BandID = ? WHERE PerformerName = ?";
			PreparedStatement statement = connection.prepareStatement(SQL);
			statement.setString(1, name);
			statement.setString(2, bandID);
			statement.setString(3, performer);

			int insertedRows = statement.executeUpdate();
			return true;

		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	
	/**
	 * Returns an event ID given a band ID
	 * @param bandID
	 * @return
	 * @throws ClassNotFoundException
	 */
	public static String getEventID(String bandID) throws ClassNotFoundException {
		String eventID;
		try {
			Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
			String connectionUrl = "jdbc:sqlserver://DESKTOP-RI159U3:1433;databaseName=NDI";
			Connection connection = DriverManager.getConnection(connectionUrl, USER, PASSWORD);
			String SQL = "SELECT * FROM Band WHERE BandID = ?";
			PreparedStatement statement = connection.prepareStatement(SQL);
			statement.setString(1, bandID);
			ResultSet rs = statement.executeQuery();

			boolean res = rs.next();
			eventID = rs.getString("EVENT_EventID");
			return eventID;
			

		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * Returns a band ID given a band
	 * @param band
	 * @return
	 * @throws ClassNotFoundException
	 */
	public static String getBandID(String band) throws ClassNotFoundException {
		String bandID;
		try {
			Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
			String connectionUrl = "jdbc:sqlserver://DESKTOP-RI159U3:1433;databaseName=NDI";
			Connection connection = DriverManager.getConnection(connectionUrl, USER, PASSWORD);
			String SQL = "SELECT * FROM Band WHERE BandName = ?";
			PreparedStatement statement = connection.prepareStatement(SQL);
			statement.setString(1, band);
			ResultSet rs = statement.executeQuery();

			boolean res = rs.next();
			bandID = rs.getString("BandID");
			return bandID;
			

		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	
	/**
	 * Removes performer from the database given a performer name
	 * @param performer
	 * @return
	 * @throws ClassNotFoundException
	 */
	public static boolean removePerformer(String performer) throws ClassNotFoundException {
		try {
			Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
			String connectionUrl = "jdbc:sqlserver://DESKTOP-RI159U3:1433;databaseName=NDI";
			Connection connection = DriverManager.getConnection(connectionUrl, USER, PASSWORD);
			String SQL = "DELETE FROM Performer WHERE PerformerName = ?";
			PreparedStatement statement = connection.prepareStatement(SQL);
			statement.setString(1, performer);
			
			int insertedRows = statement.executeUpdate();
			return true;

		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	/**
	 * Removes band from the database given a band name
	 * @param bandName
	 * @return
	 * @throws ClassNotFoundException
	 */
	public static boolean removeBand(String bandName) throws ClassNotFoundException{
		try {
			Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
			String connectionUrl = "jdbc:sqlserver://DESKTOP-RI159U3:1433;databaseName=NDI";
			Connection connection = DriverManager.getConnection(connectionUrl, USER, PASSWORD);
			String SQL = "DELETE FROM Band WHERE BandName = ?";
			PreparedStatement statement = connection.prepareStatement(SQL);
			statement.setString(1, bandName);
			
			int insertedRows = statement.executeUpdate();
			return true;

		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}

	

}
