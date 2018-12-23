package sugtao4423.icondialog;

public class IconItem{

	private String title;
	private int resource;

	public IconItem(String title, int resource){
		this.title = title;
		this.resource = resource;
	}

	public String getTitle(){
		return title;
	}

	public int getResource(){
		return resource;
	}
}
