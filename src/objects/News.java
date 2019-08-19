package objects;

import java.sql.Timestamp;

public class News {
	public Integer NewsId;
	public String Text;
	public Timestamp Timestamp;
	
	public void setNewsId (Integer NewsId) {
		this.NewsId = NewsId;
	}

	public void setText (String Text) {
		this.Text = Text;
	}
	public void setTimestamp (Timestamp Timestamp) {
		this.Timestamp = Timestamp;
	}
}
