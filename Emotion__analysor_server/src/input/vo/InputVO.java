package input.vo;import java.util.ArrayList;import java.util.LinkedList;import kr.ac.kaist.swrc.jhannanum.comm.Eojeol;import kr.ac.kaist.swrc.jhannanum.comm.Sentence;import kr.ac.kaist.swrc.jhannanum.hannanum.Workflow;import kr.ac.kaist.swrc.jhannanum.hannanum.WorkflowFactory;public class InputVO {	private String inputText;		private ArrayList<String> nounList;	private ArrayList<String> verbList;		private ArrayList<Double> nounEmoProb;	private ArrayList<Double> verbEmoProb;		private int textEmotion;		public InputVO(){	}		public InputVO(String inputText){		this.inputText = inputText;		this.nounList = new ArrayList<String>();		this.verbList = new ArrayList<String>();		Workflow nounWorkflow = WorkflowFactory.getPredefinedWorkflow(WorkflowFactory.WORKFLOW_NOUN_EXTRACTOR);		Workflow verbWorkflow = WorkflowFactory.getPredefinedWorkflow(WorkflowFactory.WORKFLOW_HMM_POS_TAGGER);				//text로 부터 추출된 명사 초기화		try {			nounWorkflow.activateWorkflow(true);			nounWorkflow.analyze(this.inputText);			LinkedList<Sentence> resultList = nounWorkflow.getResultOfDocument(new Sentence(0,0,false));			for(Sentence s : resultList){				Eojeol[] eojeolArray = s.getEojeols();				for (int i = 0; i < eojeolArray.length; i++) {					if (eojeolArray[i].length > 0) {						String[] morphemes = eojeolArray[i].getMorphemes();						String str="";						for (int j = 0; j < morphemes.length; j++) {							str+=morphemes[j];												}						this.nounList.add(str);					}				}			}		} catch (Exception e) {			e.printStackTrace();		}		nounWorkflow.close();		//text로 부터 추출된 동사 초기화		try{			verbWorkflow.activateWorkflow(true);			verbWorkflow.analyze(this.inputText);			String temp = verbWorkflow.getResultOfDocument();						//System.out.println(temp);			int ncpa = temp.indexOf("ncpa");			int ncps = temp.indexOf("ncps");			int pvg = temp.indexOf("pvg");			int paa = temp.indexOf("paa");						String ncpaVerb="";			String ncpsVerb="";			String pvgVerb="";			String paaVerb="";						if(paa != -1){				int i=2;								while(true){					if(Character.isSpace(temp.charAt(paa-i)))						break;					i++;				}								for( ; i>2; i--){					paaVerb+=temp.charAt(paa-i+1);				}				//paaVerb+="다";				this.verbList.add(paaVerb);			}						if(ncpa != -1){				int i=2;								while(true){					if(Character.isSpace(temp.charAt(ncpa-i)))						break;					i++;				}								for( ; i>2; i--){					ncpaVerb+=temp.charAt(ncpa-i+1);				}				//ncpaVerb+="다";				this.verbList.add(ncpaVerb);			}						if(ncps != -1){				int i=2;								while(true){					if(Character.isSpace(temp.charAt(ncps-i)))						break;					i++;				}								for( ; i>2; i--){					ncpsVerb+=temp.charAt(ncps-i+1);				}				//ncpsVerb+="다";				this.verbList.add(ncpsVerb);			}						if(pvg != -1){				int i=2;								while(true){					if(Character.isSpace(temp.charAt(pvg-i)))						break;					i++;				}								for( ; i>2; i--){					pvgVerb+=temp.charAt(pvg-i+1);				}				//pvgVerb+="다";				this.verbList.add(pvgVerb);			}								}catch(Exception e){			e.printStackTrace();		}				System.out.println("**** 추출 명사 리스트 ****");		for(int i=0; i<nounList.size(); i++){			System.out.println(nounList.get(i));		}		System.out.println();		System.out.println("**** 추출 동사 리스트 ****");		for(int i=0; i<verbList.size(); i++){			System.out.println(verbList.get(i));		}		System.out.println();		verbWorkflow.close();	}	public String getInputText() {		return inputText;	}	public void setInputText(String inputText) {		this.inputText = inputText;	}	public ArrayList<String> getNounList() {		return nounList;	}	public void setNounList(ArrayList<String> nounList) {		this.nounList = nounList;	}	public ArrayList<String> getVerbList() {		return verbList;	}	public void setVerbList(ArrayList<String> verbList) {		this.verbList = verbList;	}	public int getTextEmotion() {		return textEmotion;	}	public void setTextEmotion(int textEmotion) {		this.textEmotion = textEmotion;	}	public ArrayList<Double> getNounEmoProb() {		return nounEmoProb;	}	public void setNounEmoProb(ArrayList<Double> nounEmoProb) {		this.nounEmoProb = nounEmoProb;	}	public ArrayList<Double> getVerbEmoProb() {		return verbEmoProb;	}	public void setVerbEmoProb(ArrayList<Double> verbEmoProb) {		this.verbEmoProb = verbEmoProb;	}			}