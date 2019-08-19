package objects;

public class TranscriptionLanguage {
	public Integer TranscriptionId; 
	public Integer LanguageId;
	public String Name;
	public String NameEnglish;
	public String ShortName;
	public String Code;
	
	public void setTranscriptionId(Integer transcriptionId) {
		TranscriptionId = transcriptionId;
	}
	public void setLanguageId(Integer languageId) {
		LanguageId = languageId;
	}
	public void setName(String name) {
		Name = name;
	}
	public void setNameEnglish(String nameEnglish) {
		NameEnglish = nameEnglish;
	}
	public void setShortName(String shortName) {
		ShortName = shortName;
	}
	public void setCode(String code) {
		Code = code;
	} 
}
