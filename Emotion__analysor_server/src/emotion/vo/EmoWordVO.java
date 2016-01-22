package emotion.vo;

import java.util.ArrayList;

public class EmoWordVO {
	
	private ArrayList<String> nounList;
	private int emo;
	
	public EmoWordVO(){
		
	}
	
	public EmoWordVO(ArrayList<String> nounList, int emo) {
		super();
		this.nounList = nounList;
		this.emo = emo;
	}
	public int getEmo() {
		return emo;
	}
	public void setEmp(int emo) {
		this.emo = emo;
	}
	public ArrayList<String> getNounList() {
		return nounList;
	}
	public void setNounList(ArrayList<String> nounList) {
		this.nounList = nounList;
	}
	
	
	
	
	
}
