import java.io.*;
import java.sql.*;
import java.util.ArrayList;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

@WebServlet("/LoginServlet")
public class LoginServlet extends HttpServlet {

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
		
		String email = request.getParameter("email").trim();
		String password = request.getParameter("password").trim();
		boolean isAdmin;
		
		try {
			isAdmin = authenticate(email, password);
		} catch (ClassNotFoundException e) {
			isAdmin = false;
		}
		
		if (isAdmin) {
			RequestDispatcher rs = request.getRequestDispatcher("admin_area.html");
            rs.forward(request, response);
		}
		else {
			out.println("Username or Password Incorrect. Please Try Again");
		}


	}
	
	/**
	 * Checks whether the user + password combination provides the adequate measure of CIA to allow users
	 * access to the administration database
	 * @param email
	 * @param password
	 * @return
	 * @throws ClassNotFoundException
	 */
	public static boolean authenticate(String email, String password) throws ClassNotFoundException {
		try {
			Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
			String connectionUrl = "jdbc:sqlserver://DESKTOP-RI159U3:1433;databaseName=NDI";
			Connection connection = DriverManager.getConnection(connectionUrl, USER, PASSWORD);
			String SQL = "SELECT * FROM Administrators WHERE Email = ? AND Password = ?";
			PreparedStatement statement = connection.prepareStatement(SQL);
			statement.setString(1, email);
			statement.setString(2, password);
			ResultSet rs = statement.executeQuery();

			if (!rs.next()){
				return false;
			}
			else {
				return true;
			}

		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}
	

}
