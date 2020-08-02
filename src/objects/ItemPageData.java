package objects;

import java.util.List;

public class ItemPageData {
	public List<Property> Categories;
	public List<Language> Languages;
	public List<CompletionStatus> CompletionStatus;
	public List<FieldMapping> FieldMappings;
	public List<Item> ItemImages;
	
	public List<Property> getCategories() {
		return Categories;
	}
	public void setCategories(List<Property> categories) {
		Categories = categories;
	}
	public List<Language> getLanguages() {
		return Languages;
	}
	public void setLanguages(List<Language> languages) {
		Languages = languages;
	}
	public List<CompletionStatus> getCompletionStatus() {
		return CompletionStatus;
	}
	public void setCompletionStatus(List<CompletionStatus> completionStatus) {
		CompletionStatus = completionStatus;
	}
	public List<FieldMapping> getFieldMappings() {
		return FieldMappings;
	}
	public void setFieldMappings(List<FieldMapping> fieldMappings) {
		FieldMappings = fieldMappings;
	}
}
