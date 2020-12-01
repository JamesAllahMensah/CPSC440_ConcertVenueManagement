# CPSC440_ConcertVenueManagement
Concert Venue Management Full Stack Application for CPSC 440

High Level:

The Concert Venue Management Web App is a full stack application that allows administrators at different concerts the opportunity to manage their business on a standalone platform. The backend of the application utilizes SQL and Java Servlet Pages, hosted via the Apache Tomcat Server and the front end was made simply with HTML,CSS, Javascript and JQuery.

Back End:

The backend of the application includes a SQL server, managed with Microsoft’s SQL Studio Management Server, and Java, developed in the Eclipse Integrated Development Environment. In terms of technical specifics, the backend application workflow was as follows. First we used embedded SQL code to create the database tables and column names, which we then loaded with sample data as records. Once all the data was in the database, we configured the Apache Tomcat Server which is used to send GET and POSTs requests from the back end to the front end vice versa. After properly configuring our environment, we connected to our SQL server using JDBC methods, Java Database Connectivity, then wrote several individual methods to either insert, update, read, or delete rows from tables within our database based on the requested data. Once we received the requested data from the front end, we simply passed them in as parameters, which identified what action was taking place, then passed the response back into the front end.

Front End:

The front end of the application is composed of HTML, CSS, Javascript, & JQuery. The webpage structure of the forms and management menus was developed using HTML, Hypertext Markup Language, and was styled using CSS, Cascading Style Sheets. During the development of the forms, to load the dropdown components with the current database information, we used Javascript to grab the values of the forms, and Jquery to send the data into the backend using the built in AJAX, Asynchronous Javascript and XML, methods where we then were returned a delimited string of data that we then split using javascript and loaded into the dropdown components. Once the forms were complete, we used javascript to look at the current inputs to identify if they were valid or not. If they weren’t, an alert box is shown in the user’s browser and they cannot submit their data until they resolve the identified issues. When they pressed on the submit button, the inputs were sent into the backend using AJAX, where they received a response back in real time. If the response contains an error, an error message is displayed on the screen. If not, a success message.



