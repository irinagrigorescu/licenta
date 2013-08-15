package Helpers;

import java.util.List;

public class Booth {

    private String title;
    private String logo; 
    private String description;
    private String similarity;
    private int noCheckedInPeople;
    private List<String> tags;

    public Booth(String logo, String title, String description, String similarity,
            int noCheckedInPeople, List<String> tags) {
        this.setLogo(logo);
    	this.title = title;
        this.description = description;
        this.similarity = similarity;
        this.noCheckedInPeople = noCheckedInPeople;
        this.tags = tags;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getSimilarity() {
        return similarity;
    }

    public void setSimilarity(String similarity) {
        this.similarity = similarity;
    }

    public int getNoCheckedInPeople() {
        return noCheckedInPeople;
    }

    public void setNoCheckedInPeople(int noCheckedInPeople) {
        this.noCheckedInPeople = noCheckedInPeople;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

	public String getLogo() {
		return logo;
	}

	public void setLogo(String logo) {
		this.logo = logo;
	}

}
