package entity;

public class Pair {
    private int i;
    private String s;

    public Pair(int i, String s) {
        this.i = i;
        this.s = s;
    }

    public int getInt() {
        return i;
    }
    public  void SetInt(){
        i=i+1;
    }
    public String getString() {
        return s;
    }
    public void SetString(String name){
        s = name;
    }
}