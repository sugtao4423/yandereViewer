package sugtao4423.yandereviewer;

public class SearchItem{

	public static final int TAG = 0;
	public static final int HISTORY = 1;

	private String name;
	private int kind;

	public SearchItem(String name, int kind){
		this.name = name;
		this.kind = kind;
	}

	public String getName(){
		return name;
	}

	public int getKind(){
		return kind;
	}

	@Override
	public String toString(){
		return getName();
	}
}
