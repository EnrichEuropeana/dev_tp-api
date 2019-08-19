package objects;

public class Score {
	public Integer ScoreId;
	public Integer Amount;
	public Integer UserId;
	public Integer ScoreTypeId;
	public String ScoreType;
	public Float Rate;
	public Integer ItemId;
	public String Timestamp;
	
	
	public void setScoreId(Integer scoreId) {
		ScoreId = scoreId;
	}
	public void setAmount(Integer amount) {
		Amount = amount;
	}
	public void setUserId(Integer userId) {
		UserId = userId;
	}
	public void setScoreTypeId(Integer scoreTypeId) {
		ScoreTypeId = scoreTypeId;
	}
	public void setScoreType(String scoreType) {
		ScoreType = scoreType;
	}
	public void setRate(Float rate) {
		Rate = rate;
	}
	public void setItemId(Integer itemId) {
		ItemId = itemId;
	}
	public void setTimestamp(String timestamp) {
		Timestamp = timestamp;
	}
}
