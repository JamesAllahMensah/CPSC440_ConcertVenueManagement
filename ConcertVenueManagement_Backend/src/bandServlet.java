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

@WebServlet("/bandServlet")
public class bandServlet extends HttpServlet {

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
		String performerList = request.getParameter("performers");
		String genre = request.getParameter("genre");
		String event = request.getParameter("event");
		String origBandName = request.getParameter("band");
		
		boolean updated = false;
		if (action.equals("Add Band")) {
			updated = addBandWithPerformer(name, genre, event, performerList);
			if (updated) {
				out.print("Band Successfully Added! Please return to the Band Management Page!");
			}
			else {
				out.print("Error Adding Band. Please return to the Band Management Page.");
			}
		}
		else if (action.equals("Get Band")) {
			try {
				String bandList = getBands();
				out.print(bandList);
			} catch (ClassNotFoundException e) {
				out.print("");
			}
		}
		else if (action.equals("Get Performers")) {
			try {
				String listPerformers = getPerformers();
				out.print(listPerformers);
			} catch (ClassNotFoundException e) {
				out.print("");
			}
		}
		else if (action.equals("Edit Band")) {
			updated = editEntireBand(origBandName, name, performerList, genre, event);
			if (updated) {
				out.print("Band Successfully Edited! Please return to the Band Management Page!");
			}
			else {
				out.print("Error Editing Band! Please return to the Band Management Page!");
			}
		}
		else {
			String listPerformers = "";
			try {
				listPerformers = getPerformersWithBand(origBandName);
			} catch (ClassNotFoundException e) {
				listPerformers = null;
			}
			updated = removeEntireBand(origBandName, listPerformers);
			if (updated) {
				out.print("Band & Performers Successfully Removed! Please return to the Band Management Page!");
			}
			else {
				out.print("Error Removing Band & Performers! Please return to the Band Management Page!");
			}
		}
		

	}
	
	/**
	 * Adds a band and updates all associated performers into the database
	 * @param name
	 * @param genre
	 * @param event
	 * @param performerList
	 * @return
	 */
	public static boolean addBandWithPerformer(String name, String genre, String event, String performerList) {
		ArrayList<String> performers = new ArrayList<String>();
		Scanner scnr = new Scanner(performerList);
		scnr.useDelimiter(";");
		while (scnr.hasNext()) {
			String performer = scnr.next();
			performers.add(performer.trim());
		}
		
		try {
			addBand(name, genre, event);
			if (!performerList.equals("None") && !performerList.equals("Select Performer(s)")) {
				for (String performer: performers) {
					addPerformerToBand(performer, name);
				}
			}
			return true;
		} catch (ClassNotFoundException e) {
			return false;
		}
	}
	
	/**
	 * Adds a band record into the table
	 * @param name
	 * @param genre
	 * @param event
	 * @return
	 * @throws ClassNotFoundException
	 */
	public static boolean addBand(String name, String genre, String event) throws ClassNotFoundException {
		Random random = new Random();
		String bandID = String.format("%04d", random.nextInt(10000));
		String eventID = getEventID(event);
		
		try {
			Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
			String connectionUrl = "jdbc:sqlserver://DESKTOP-RI159U3:1433;databaseName=NDI";
			Connection connection = DriverManager.getConnection(connectionUrl, USER, PASSWORD);
			String SQL = "INSERT INTO Band VALUES (?,?,?,?)";
			PreparedStatement statement = connection.prepareStatement(SQL);
			statement.setString(1, bandID);
			statement.setString(2, name);
			statement.setString(3, genre);
			statement.setString(4, eventID);
			
			int insertedRows = statement.executeUpdate();
			return true;

		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
		
	}
	
	/**
	 * Sets a performer to a band
	 * @param performer
	 * @param band
	 * @return
	 * @throws ClassNotFoundException
	 */
	public static boolean addPerformerToBand(String performer, String band) throws ClassNotFoundException {
		Random random = new Random();
		String bandID = getBandID(band);
		
		try {
			Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
			String connectionUrl = "jdbc:sqlserver://DESKTOP-RI159U3:1433;databaseName=NDI";
			Connection connection = DriverManager.getConnection(connectionUrl, USER, PASSWORD);
			String SQL = "UPDATE Performer SET BAND_BandID = ? WHERE PerformerName = ?";
			PreparedStatement statement = connection.prepareStatement(SQL);
			statement.setString(1, bandID);
			statement.setString(2, performer);

			int insertedRows = statement.executeUpdate();
			return true;

		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	/**
	 * Returns an event ID given the eventName
	 * @param event
	 * @return
	 * @throws ClassNotFoundException
	 */
	public static String getEventID(String event) throws ClassNotFoundException {
		String eventID;
		try {
			Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
			String connectionUrl = "jdbc:sqlserver://DESKTOP-RI159U3:1433;databaseName=NDI";
			Connection connection = DriverManager.getConnection(connectionUrl, USER, PASSWORD);
			String SQL = "SELECT * FROM Event WHERE EventName = ?";
			PreparedStatement statement = connection.prepareStatement(SQL);
			statement.setString(1, event);
			ResultSet rs = statement.executeQuery();

			boolean res = rs.next();
			eventID = rs.getString("EventID");
			return eventID;
			

		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * Returns a Band ID given a band
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
	 * Edits all the performers associated to the band as well as the band itself
	 * @param bandName
	 * @param name
	 * @param performerList
	 * @param genre
	 * @param event
	 * @return
	 */
	public static boolean editEntireBand(String bandName, String name, String performerList, String genre, String event) {
		ArrayList<String> performers = new ArrayList<String>();
		Scanner scnr = new Scanner(performerList);
		scnr.useDelimiter(";");
		while (scnr.hasNext()) {
			String performer = scnr.next();
			performers.add(performer.trim());
		}
		
		try {
			editBand(bandName, name, genre, event);
			if (!performerList.equals("None") && !performerList.equals("Select Performer(s)")) {
				for (String performer: performers) {
					addPerformerToBand(performer, name);
				}
			}
			return true;
		} catch (ClassNotFoundException e) {
			return false;
		}
		
	}
	
	/**
	 * Edits the inputted band record in the database given the band name
	 * @param bandName
	 * @param name
	 * @param genre
	 * @param event
	 * @return
	 * @throws ClassNotFoundException
	 */
	public static boolean editBand(String bandName, String name, String genre, String event) throws ClassNotFoundException {
		String eventID = getEventID(event);
		try {
			Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
			String connectionUrl = "jdbc:sqlserver://DESKTOP-RI159U3:1433;databaseName=NDI";
			Connection connection = DriverManager.getConnection(connectionUrl, USER, PASSWORD);
			String SQL = "UPDATE Band SET BandName = ?, Genre = ?, EVENT_EventID = ? WHERE BandName = ?";
			PreparedStatement statement = connection.prepareStatement(SQL);
			statement.setString(1, name);
			statement.setString(2, genre);
			statement.setString(3, eventID);
			statement.setString(4, bandName);
			
			int insertedRows = statement.executeUpdate();
			return true;

		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
		
	}
	
	/**
	 * Returns a semi colon delimited string of bands
	 * @return
	 * @throws ClassNotFoundException
	 */
	public static String getBands() throws ClassNotFoundException {
		ArrayList<String> bands = new ArrayList<String>();
		String concatBands = "";
		try {
			Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
			String connectionUrl = "jdbc:sqlserver://DESKTOP-RI159U3:1433;databaseName=NDI";
			Connection connection = DriverManager.getConnection(connectionUrl, USER, PASSWORD);
			String SQL = "SELECT * FROM Band";
			PreparedStatement statement = connection.prepareStatement(SQL);
			ResultSet rs = statement.executeQuery();
			String band;

			while (rs.next()) {
				band = rs.getString("BandName");
				bands.add(band);
			}
			if (bands.isEmpty()) {
				return null;
			}
			
			for (int i = 0; i < bands.size(); i++) {
				String currentBand = bands.get(i);
				concatBands += currentBand + ";";
			}
			concatBands = concatBands.substring(0, concatBands.length() - 1);
			return concatBands;

		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * Returns a semi colon delimited string of performers
	 * @return
	 * @throws ClassNotFoundException
	 */
	public static String getPerformers() throws ClassNotFoundException {
		ArrayList<String> performers = new ArrayList<String>();
		String concatPerformers = "";
		try {
			Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
			String connectionUrl = "jdbc:sqlserver://DESKTOP-RI159U3:1433;databaseName=NDI";
			Connection connection = DriverManager.getConnection(connectionUrl, USER, PASSWORD);
			String SQL = "SELECT * FROM Performer";
			PreparedStatement statement = connection.prepareStatement(SQL);
			ResultSet rs = statement.executeQuery();
			String performer;

			while (rs.next()) {
				performer = rs.getString("PerformerName");
				performers.add(performer);
			}
			if (performers.isEmpty()) {
				return null;
			}
			
			for (int i = 0; i < performers.size(); i++) {
				String currentPerformer = performers.get(i);
				concatPerformers += currentPerformer + ";";
			}
			concatPerformers = concatPerformers.substring(0, concatPerformers.length() - 1);
			return concatPerformers;

		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * Returns a semi colon delimited string of all performers associated with a band
	 * @param band
	 * @return
	 * @throws ClassNotFoundException
	 */
	public static String getPerformersWithBand(String band) throws ClassNotFoundException {
		String bandID = getBandID(band);
		ArrayList<String> performers = new ArrayList<String>();
		String concatPerformers = "";
		try {
			Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
			String connectionUrl = "jdbc:sqlserver://DESKTOP-RI159U3:1433;databaseName=NDI";
			Connection connection = DriverManager.getConnection(connectionUrl, USER, PASSWORD);
			String SQL = "SELECT * FROM Performer WHERE BAND_BandID = ?";
			PreparedStatement statement = connection.prepareStatement(SQL);
			statement.setString(1, bandID);
			ResultSet rs = statement.executeQuery();
			String performer;

			while (rs.next()) {
				performer = rs.getString("PerformerName");
				performers.add(performer);
			}
			if (performers.isEmpty()) {
				return null;
			}
			
			for (int i = 0; i < performers.size(); i++) {
				String currentPerformer = performers.get(i);
				concatPerformers += currentPerformer + ";";
			}
			concatPerformers = concatPerformers.substring(0, concatPerformers.length() - 1);
			return concatPerformers;

		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * Removes a band from and disassociates performers from that band
	 * @param band
	 * @param performerList
	 * @return
	 */
	public static boolean removeEntireBand(String band, String performerList) {
		if (performerList == null) {
			try {
				removeBand(band);
				return true;
			} catch (ClassNotFoundException e) {
				return false;
			}
		}
		if (performerList.length() == 0 || performerList.length() == 1) {
			try {
				removeBand(band);
				return true;
			} catch (ClassNotFoundException e) {
				return false;
			}
		}
		ArrayList<String> performers = new ArrayList<String>();
		Scanner scnr = new Scanner(performerList);
		scnr.useDelimiter(";");
		while (scnr.hasNext()) {
			String performer = scnr.next();
			performers.add(performer.trim());
		}
		
		for (String performer: performers) {
			try {
				removePerformer(band);
			} catch (ClassNotFoundException e) {
				return false;
			}
		}
		try {
			removeBand(band);
			return true;
		} catch (ClassNotFoundException e) {
			return false;
		}
	}
	
	/**
	 * Removes performers from band when band is removed
	 * @param bandName
	 * @return
	 * @throws ClassNotFoundException
	 */
	public static boolean removePerformer(String bandName) throws ClassNotFoundException {
		String bandID = getBandID(bandName);
		try {
			Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
			String connectionUrl = "jdbc:sqlserver://DESKTOP-RI159U3:1433;databaseName=NDI";
			Connection connection = DriverManager.getConnection(connectionUrl, USER, PASSWORD);
			String SQL = "DELETE FROM Performer WHERE BAND_BandID = ?";
			PreparedStatement statement = connection.prepareStatement(SQL);
			statement.setString(1, bandID);
			
			int insertedRows = statement.executeUpdate();
			return true;

		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	/**
	 * Removes band record from the database
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

	


	public static void main(String[] args) throws ClassNotFoundException {
		String band = getPerformers();
		System.out.println(band);
		

	}

}
