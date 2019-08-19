package objects;

import java.sql.Timestamp;

public class AnnotationExport {
	public Integer EuropeanaAnnotationId;
	public Integer AnnotationId;
	public String Text;
	public Timestamp Timestamp;
	public Float X_Coord;
	public Float Y_Coord;
	public Float Width;
	public Float Height;
	public String Motivation;
	public String ItemId;
	public String StoryUrl;
	public String StoryId;
	
	public void setEuropeanaAnnotationId(Integer europeanaAnnotationId) {
		EuropeanaAnnotationId = europeanaAnnotationId;
	}
	public void setAnnotationId(Integer annotationId) {
		AnnotationId = annotationId;
	}
	public void setText(String text) {
		Text = text;
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
	public void setItemId(String itemId) {
		ItemId = itemId;
	}
	public void setStoryUrl(String storyUrl) {
		StoryUrl = storyUrl;
	}
	public void setStoryId(String storyId) {
		StoryId = storyId;
	}
}
