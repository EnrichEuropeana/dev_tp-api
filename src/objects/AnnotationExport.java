package objects;

import java.sql.Timestamp;
import java.util.List;

public class AnnotationExport {
	public Integer EuropeanaAnnotationId;
	public Integer AnnotationId;
	public String Text;
	public String TextNoTags;
	public Timestamp Timestamp;
	public Float X_Coord;
	public Float Y_Coord;
	public Float Width;
	public Float Height;
	public String Motivation;
	public Integer OrderIndex;
	public Integer TranscribathonItemId;
	public Integer TranscribathonStoryId;
	public String StoryUrl;
	public String StoryId;
	public String ImageLink;
	public List<Language> Languages;
	
	
	public void setEuropeanaAnnotationId(Integer europeanaAnnotationId) {
		EuropeanaAnnotationId = europeanaAnnotationId;
	}
	public void setAnnotationId(Integer annotationId) {
		AnnotationId = annotationId;
	}
	public void setText(String text) {
		Text = text;
	}
	public void setTextNoTags(String textNoTags) {
		TextNoTags = textNoTags;
	}
	public void setTimestamp(Timestamp timestamp) {
		Timestamp = timestamp;
	}
	public void setX_Coord(Float x_Coord) {
		X_Coord = x_Coord;
	}
	public void setY_Coord(Float y_Coord) {
		Y_Coord = y_Coord;
	}
	public void setWidth(Float width) {
		Width = width;
	}
	public void setHeight(Float height) {
		Height = height;
	}
	public void setMotivation(String motivation) {
		Motivation = motivation;
	}
	public void setOrderIndex(Integer orderIndex) {
		OrderIndex = orderIndex;
	}
	public void setTranscribathonItemId(Integer transcribathonItemId) {
		TranscribathonItemId = transcribathonItemId;
	}
	public void setTranscribathonStoryId(Integer transcribathonStoryId) {
		TranscribathonStoryId = transcribathonStoryId;
	}
	public void setStoryUrl(String storyUrl) {
		StoryUrl = storyUrl;
	}
	public void setStoryId(String storyId) {
		StoryId = storyId;
	}
	public void setImageLink(String imageLink) {
		ImageLink = imageLink;
	}
	public void setLanguages(List<Language> languages) {
		Languages = languages;
	}
}
