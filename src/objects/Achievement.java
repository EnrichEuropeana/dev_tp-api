package objects;

public class Achievement {
	public Integer AchievementId;
	public String Name;
	public Integer ScoreTypeId1;
	public String Amount1;
	public Integer ScoreTypeId2;
	public String Amount2;
	public String Description;
	public Integer Level;
	
	
	public void setAchievementId(Integer achievementId) {
		AchievementId = achievementId;
	}
	public void setName(String name) {
		Name = name;
	}
	public void setScoreTypeId1(Integer scoreTypeId1) {
		ScoreTypeId1 = scoreTypeId1;
	}
	public void setAmount1(String amount1) {
		Amount1 = amount1;
	}
	public void setScoreTypeId2(Integer scoreTypeId2) {
		ScoreTypeId2 = scoreTypeId2;
	}
	public void setAmount2(String amount2) {
		Amount2 = amount2;
	}
	public void setDescription(String description) {
		Description = description;
	}
	public void setLevel(Integer level) {
		Level = level;
	}	
}
