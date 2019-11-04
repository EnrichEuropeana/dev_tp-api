package objects;

import java.sql.Timestamp;
import java.util.List;

public class TranscriptionProfile {
	public Timestamp Timestamp;
	public Integer UserId;
	public Integer WP_UserId;
	public Integer ItemId;
	public String ItemImageLink;
	public String ItemTitle;
	public String CompletionStatus;
	public String ProjectUrl;
	public List<Score> Scores;
	
	public void setTimestamp(Timestamp timestamp) {
		Timestamp = timestamp;
	}
	public void setUserId(Integer userId) {
		UserId = userId;
	}
	public void setWP_UserId(Integer wP_UserId) {
		WP_UserId = wP_UserId;
	}
	public void setItemId(Integer itemId) {
		ItemId = itemId;
	}
	public void setItemImageLink(String itemImageLink) {
		ItemImageLink = itemImageLink;
	}
	public void setItemTitle(String itemTitle) {
		ItemTitle = itemTitle;
	}
	public void setCompletionStatus(String completionStatus) {
		CompletionStatus = completionStatus;
	}
	public void setProjectUrl(String projectUrl) {
		ProjectUrl = projectUrl;
	}
	public void setScores(List<Score> scores) {
		Scores = scores;
	}
}
