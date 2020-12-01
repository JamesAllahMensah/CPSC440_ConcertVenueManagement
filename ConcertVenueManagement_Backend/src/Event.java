
public class Event {
	
	private String ID;
	private String startTime;
	private String endTime;
	private String numTicketsAvailable;
	private String genre;
	private String venue;
	private String name;
	private String date;
	
	public Event(String ID, String startTime, String endTime, String numTicketsAvailable,
			String genre, String venue, String name, String date) {
		this.ID = ID;
		this.startTime = startTime;
		this.endTime = endTime;
		this.numTicketsAvailable = numTicketsAvailable;
		this.genre = genre;
		this.venue = venue;
		this.name = name;
		this.date = date;
	}
	
	public String getVenueID() {
		return venue;
	}
	
	public String getName() {
		return name;
	}
	
	public String getVenue() {
		return venue;
	}
	
	public void setVenue(String venue) {
		this.venue = venue;
	}
	
	public void decrementTicket(String newTicketCount) {
		int newTicketCnt = Integer.parseInt(newTicketCount);
		newTicketCnt--;
		String strNewTicketCnt = String.valueOf(newTicketCnt);
		this.numTicketsAvailable = strNewTicketCnt;
	}
	
	public String toString() {
		return this.ID + ";" + this.startTime + ";" + this.endTime + ";" + this.numTicketsAvailable + ";" +
				this.genre + ";" + this.venue + ";" + this.name + ";" + this.date;
	}
	
	
	

}
