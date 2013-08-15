package Helpers;

public class User {

    private String userDisplayName;
    private String userSimilarity;
    private String userTagSet;

    public User(String un, String us, String ts) {
        this.userDisplayName = un.toUpperCase();
        this.userSimilarity = us;
        this.setUserTagSet(ts);
    }

    public String getUserDisplayName() {
        return userDisplayName;
    }

    public void setUserDisplayName(String userDisplayName) {
        this.userDisplayName = userDisplayName;
    }

    public String getUserSimilarity() {
        return userSimilarity;
    }

    public void setUserSimilarity(String userSimilarity) {
        this.userSimilarity = userSimilarity;
    }

	public String getUserTagSet() {
		return userTagSet;
	}

	public void setUserTagSet(String userTagSet) {
		this.userTagSet = userTagSet;
	}

}
