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

@WebServlet("/ratingServlet")
public class ratingServlet extends HttpServlet {

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
		
		String event = request.getParameter("event");
		String numTicketsSold = request.getParameter("num_tickets_sold");
		String crowdVolume = request.getParameter("crowd_volume");
		String performanceLength = request.getParameter("performance_length");
		String numSongsPerformed = request.getParameter("num_songs_performed");
		
		String numTicketsAvailable;
		try {
			numTicketsAvailable = getNumTicketsAvailable(event);
		} catch (ClassNotFoundException e1) {
			numTicketsAvailable = null;
		}

		
		boolean updated = false;
		String rating = null;
		try {
			updated= addRating(event, numTicketsSold, crowdVolume, performanceLength, numSongsPerformed);
			rating = calculateRating(numTicketsSold, numTicketsAvailable, crowdVolume, 
					performanceLength, numSongsPerformed);
			if (updated) {
				out.println("SCORE: " + rating);
			}
		} catch (ClassNotFoundException e) {
			updated = false;
		}
	
	}
	
	/**
	 * Returns the number of available tickets for each event
	 * @param event
	 * @return
	 * @throws ClassNotFoundException
	 */
	public static String getNumTicketsAvailable(String event) throws ClassNotFoundException {
		String numTickets;
		try {
			Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
			String connectionUrl = "jdbc:sqlserver://DESKTOP-RI159U3:1433;databaseName=NDI";
			Connection connection = DriverManager.getConnection(connectionUrl, USER, PASSWORD);
			String SQL = "SELECT * FROM Event WHERE EventName = ?";
			PreparedStatement statement = connection.prepareStatement(SQL);
			statement.setString(1, event);
			ResultSet rs = statement.executeQuery();

			boolean res = rs.next();
			numTickets = rs.getString("NumTicketsAvailable");
			return numTickets;
			

		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * Returns an event ID given an event
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
	 * Calculates a rating given the attributes as inputs to the formula
	 * @param numTicketsSold
	 * @param numTicketsAvailable
	 * @param crowdVolume
	 * @param performanceLength
	 * @param numSongsPerformed
	 * @return
	 */
	public static String calculateRating(String numTicketsSold, String numTicketsAvailable, String crowdVolume,
			String performanceLength, String numSongsPerformed) {
		if (numTicketsAvailable == null) {
			return null;
		}
		int ticketsSold = Integer.parseInt(numTicketsSold);
		int ticketsAvailable = Integer.parseInt(numTicketsAvailable);
		int crowdVol = Integer.parseInt(crowdVolume);
		double lengthPerformance = (double)(Integer.parseInt(performanceLength));
		double numSongs = (double)(Integer.parseInt(numSongsPerformed));
		
		double ticketScore = ((double)ticketsSold / (double)ticketsAvailable) * 100.0;
		double crowdVolumeScore = (double)crowdVol;
		double audienceScore = (numSongs / (lengthPerformance * 12.0)) * 100.0;
		double finalScore = (ticketScore + crowdVolumeScore + audienceScore) / 300.0;
		finalScore = finalScore * 10;
		String strScore = String.format("%.1f", finalScore);
		return strScore;
		
	}
	
	/**
	 * Adds the calculated rating to the event
	 * @param event
	 * @param numTicketsSold
	 * @param crowdVolume
	 * @param performanceLength
	 * @param numSongsPerformed
	 * @return
	 * @throws ClassNotFoundException
	 */
	public static boolean addRating(String event, String numTicketsSold, String crowdVolume,
			String performanceLength, String numSongsPerformed) throws ClassNotFoundException {
		Random random = new Random();
		String ratingID = String.format("%04d", random.nextInt(10000));
		String eventID = getEventID(event);
		String numTicketsAvailable = getNumTicketsAvailable(event);
		String calculatedScore = calculateRating(numTicketsSold, numTicketsAvailable, crowdVolume, 
				performanceLength, numSongsPerformed);
		System.out.println("Variables correctly set. Attempting SQL");
		
		try {
			Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
			String connectionUrl = "jdbc:sqlserver://DESKTOP-RI159U3:1433;databaseName=NDI";
			Connection connection = DriverManager.getConnection(connectionUrl, USER, PASSWORD);
			String SQL = "INSERT INTO Rating VALUES (?,?,?,?,?,?,?)";
			PreparedStatement statement = connection.prepareStatement(SQL);
			statement.setString(1, ratingID);
			statement.setString(2, numTicketsSold);
			statement.setString(3, crowdVolume);
			statement.setString(4, performanceLength);
			statement.setString(5, numSongsPerformed);
			statement.setString(6, eventID);
			statement.setString(7, calculatedScore);
			
			int insertedRows = statement.executeUpdate();
			return true;

		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
		
	}
	

}
