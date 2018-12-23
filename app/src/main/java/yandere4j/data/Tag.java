package yandere4j.data;

public class Tag implements Comparable<Tag>{

    private int id;
    private String name;
    private int count;
    private int type;
    private boolean ambiguous;

    public Tag(int id, String name, int count, int type, boolean ambiguous){
        this.id = id;
        this.name = name;
        this.count = count;
        this.type = type;
        this.ambiguous = ambiguous;
    }

    public int getId(){
        return id;
    }

    public String getName(){
        return name;
    }

    public int getCount(){
        return count;
    }

    public int getType(){
        return type;
    }

    public boolean getAmbiguous(){
        return ambiguous;
    }

    @Override
    public int compareTo(Tag another){
        return this.id - another.id;
    }

}
