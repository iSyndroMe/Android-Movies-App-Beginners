package movie.moive3dx;

public class Movie {
	//of course there is mismatch fields here because the API working only with strings
	//for best practices define your own fields and later just convert the strings from the API to match your field.
	private long id;
	private String name;
	private String description;
	private String imdbID;
	//private String imgBitmap;
	private String year;
	private String rating;

	public Movie(String name, String description, String imdbID, String year,
			String rating) {

		this.setRating(rating);
		this.setYear(year);
		this.setImdbID(imdbID);
		this.setName(name);
		this.setDescription(description);
	}

	public Movie(String name, String description , String rating) {
		this.setName(name);
		this.setDescription(description);
		this.setRating(rating);
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getImdbID() {
		return imdbID;
	}

	public void setImdbID(String imdbID) {
		this.imdbID = imdbID;
	}

	public String getYear() {
		return year;
	}

	public void setYear(String year) {
		this.year = year;
	}

	public String getRating() {
		return rating;
	}

	public void setRating(String rating) {
		this.rating = rating;
	}

}
