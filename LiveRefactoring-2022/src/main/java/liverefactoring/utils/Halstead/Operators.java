package liverefactoring.utils.Halstead;

import java.util.ArrayList;
public class Operators {
	public static Operators instance;
	public ArrayList<String> name;
	public ArrayList<String> count;
	private Operators(){
		this.name=new ArrayList<>();
		this.count=new ArrayList<>();
	}
	public static Operators getInstance(){
		if(instance==null){
			instance=new Operators();
		}
		return instance;
	}
	public void insert(String name){
		if(this.name.contains(name)){
			int count=Integer.parseInt(this.count.get(this.name.indexOf(name)));
			this.count.set(this.name.indexOf(name),""+(count+1));
		}
		else{
			this.name.add(name);
			this.count.add(""+1);
		}
	}
	public void insert(String name,int counter){
		if(this.name.contains(name)){
			counter+=Integer.parseInt(this.count.get(this.name.indexOf(name)));
			this.count.set(this.name.indexOf(name),""+(counter));
		}
		else{
			this.name.add(name);
			this.count.add(""+counter);
		}
	}
}
