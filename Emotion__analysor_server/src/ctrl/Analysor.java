package ctrl;

import input.vo.InputNounVO;
import input.vo.InputVO;
import input.vo.InputVerbVO;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import emotion.dao.EmotionDao;
import emotion.vo.EmoWordVO;

public class Analysor {

	public static final double ANGRY_PROB = 0.313;
	public static final double HAPPY_PROB = 0.193;
	public static final double LOVE_PROB = 0.135;
	public static final double SAD_PROB = 0.357;

	private int findEmo(double ang, double hap, double lov, double sad) {

		if ((ang > hap) && (ang > lov) && (ang > sad))
			return 1;

		if ((hap > ang) && (hap > lov) && (hap > sad))
			return 2;

		if ((lov > ang) && (lov > hap) && (lov > sad))
			return 3;

		if ((sad > ang) && (sad > hap) && (sad > lov))
			return 4;

		return -1;
	}

	public int numOfText(String data) throws IOException {
		int num = 0;
		File file = new File(data);
		BufferedReader in = new BufferedReader(new InputStreamReader(
				new FileInputStream(file), "euc-kr"));

		String str;
		while (true) {
			str = in.readLine();
			if (str == null)
				break;
			num++;
			System.out.println(num + " : " + str);
		}
		in.close();
		return num;
	}

	// 데이터 입력
	public ArrayList<String> DataInput(String data) throws Exception {
		File file = new File(data);
		BufferedReader in = new BufferedReader(new InputStreamReader(
				new FileInputStream(file), "euc-kr"));
		ArrayList<String> textList = new ArrayList<String>();
		String str;

		while (true) {
			str = in.readLine();
			if (str == null)
				break;
			textList.add(str);
		}

		in.close();

		return textList;
	}

	// InputVO 객체 초기화
	public ArrayList<InputVO> InitializeObj(String str) {
		ArrayList<InputVO> initData = new ArrayList<InputVO>();
		initData.add(new InputVO(str));
		return initData;
	}

	public ArrayList<ArrayList<InputNounVO>> setNounEmoCount(
			ArrayList<InputVO> voList) {
		EmotionDao dao = new EmotionDao();
		ArrayList<ArrayList<InputNounVO>> nounVO = new ArrayList<ArrayList<InputNounVO>>();

		ArrayList<InputNounVO> temp = null;
		for (int i = 0; i < voList.size(); i++) {
			temp = new ArrayList<InputNounVO>();
			temp = dao.emoNounCountRow(voList.get(i));
			nounVO.add(temp);
		}
		
		System.out.println("**** 추출된 명사의 감정  빈도수 측정 ****");
		for(int i=0; i<nounVO.size(); i++){
			for(int j=0; j<nounVO.get(i).size(); j++){
				System.out.print(nounVO.get(i).get(j).getWord() + "의 감정 빈도 : ");
				for(int k=0; k<nounVO.get(i).get(j).getEmoCount().size(); k++){
					System.out.print(nounVO.get(i).get(j).getEmoCount().get(k)+" ");
				}
			}
			System.out.println();
		}
		System.out.println();
		
		return nounVO;
	}

	public ArrayList<ArrayList<InputVerbVO>> setVerbEmoCount(
			ArrayList<InputVO> voList) {
		EmotionDao dao = new EmotionDao();
		ArrayList<ArrayList<InputVerbVO>> verbVO = new ArrayList<ArrayList<InputVerbVO>>();
		ArrayList<InputVerbVO> temp = null;
		for (int i = 0; i < voList.size(); i++) {
			temp = new ArrayList<InputVerbVO>();
			temp = dao.emoVerbCountRow(voList.get(i));
			verbVO.add(temp);
		}
		
		System.out.println("**** 추출된 동사의 감정  빈도수 측정 ****");
		for(int i=0; i<verbVO.size(); i++){
			for(int j=0; j<verbVO.get(i).size(); j++){
				System.out.print("<<"+verbVO.get(i).get(j).getWord()+">>" + "의 감정 빈도 : ");
				for(int k=0; k<verbVO.get(i).get(j).getEmoCount().size(); k++){
					System.out.print(verbVO.get(i).get(j).getEmoCount().get(k)+" ");
				}
				System.out.println();
			}
		}
		System.out.println();
		return verbVO;
	}

	// 나이브베이즈 계산
	public void classificationWord(
			ArrayList<ArrayList<InputNounVO>> nounVOList,
			ArrayList<ArrayList<InputVerbVO>> verbVOList,
			ArrayList<InputVO> inputVOList) {

		int totalWordCount;
		double nAngryProb = 1, nHappyProb = 1, nLoveProb = 1, nSadProb = 1;
		double vAngryProb = 1, vHappyProb = 1, vLoveProb = 1, vSadProb = 1;

		double[] emoArr = null;
		ArrayList<ArrayList<double[]>> emoNounCountList = new ArrayList<ArrayList<double[]>>();
		ArrayList<ArrayList<double[]>> emoVerbCountList = new ArrayList<ArrayList<double[]>>();

		ArrayList<double[]> nounTemp = null;
		ArrayList<double[]> verbTemp = null;

		int nounSize = nounVOList.size();
		for (int i = 0; i < nounSize; i++) {
			nounTemp = new ArrayList<double[]>();
			for (int j = 0; j < nounVOList.get(i).size(); j++) {
				totalWordCount = 0;
				emoArr = new double[5];
				emoArr[0] = 1;

				for (int k = 0; k < nounVOList.get(i).get(j).getEmoCount()
						.size(); k++) {
					int count = nounVOList.get(i).get(j).getEmoCount().get(k);
					totalWordCount += count;
					emoArr[k + 1] = count;
				}

				for (int a = 0; a < 4; a++) {
					emoArr[a + 1] = emoArr[a + 1] / totalWordCount;
				}

				nounTemp.add(emoArr);
			}
			emoNounCountList.add(nounTemp);
		}

		int verbSize = verbVOList.size();
		for (int i = 0; i < verbSize; i++) {
			verbTemp = new ArrayList<double[]>();
			for (int j = 0; j < verbVOList.get(i).size(); j++) {
				totalWordCount = 0;
				emoArr = new double[5];
				emoArr[0] = 1;

				for (int k = 0; k < verbVOList.get(i).get(j).getEmoCount()
						.size(); k++) {
					int count = verbVOList.get(i).get(j).getEmoCount().get(k);
					totalWordCount += count;
					emoArr[k + 1] = count;
				}

				for (int a = 0; a < 4; a++) {
					emoArr[a + 1] = emoArr[a + 1] / totalWordCount;
				}

				verbTemp.add(emoArr);
				emoVerbCountList.add(verbTemp);
			}
		}

		for (int q = 0; q < emoNounCountList.size(); q++) {
			int size = emoNounCountList.get(q).size();
			int textId = 0;

			for (int w = 0; w < size; w++) {
				textId = (int) emoNounCountList.get(q).get(w)[0];
				nAngryProb *= emoNounCountList.get(q).get(w)[1];
				nHappyProb *= emoNounCountList.get(q).get(w)[2];
				nLoveProb *= emoNounCountList.get(q).get(w)[3];
				nSadProb *= emoNounCountList.get(q).get(w)[4];
			}

			if (textId != 0) {
				ArrayList<Double> nounEmoProb = new ArrayList<Double>();
				nounEmoProb.add(nAngryProb);
				nounEmoProb.add(nHappyProb);
				nounEmoProb.add(nLoveProb);
				nounEmoProb.add(nSadProb);
				inputVOList.get(0).setNounEmoProb(nounEmoProb);
			}

		}

		for (int q = 0; q < emoVerbCountList.size(); q++) {
			int size = emoVerbCountList.get(q).size();
			int textId = 0;
			for (int w = 0; w < size; w++) {
				textId = (int) emoVerbCountList.get(q).get(w)[0];
				vAngryProb *= emoVerbCountList.get(q).get(w)[1];
				vHappyProb *= emoVerbCountList.get(q).get(w)[2];
				vLoveProb *= emoVerbCountList.get(q).get(w)[3];
				vSadProb *= emoVerbCountList.get(q).get(w)[4];
			}

			if (textId != 0) {
				ArrayList<Double> verbEmoProb = new ArrayList<Double>();
				verbEmoProb.add(vAngryProb);
				verbEmoProb.add(vHappyProb);
				verbEmoProb.add(vLoveProb);
				verbEmoProb.add(vSadProb);
				inputVOList.get(0).setNounEmoProb(verbEmoProb);
			}

		}

	}

	public void setEmotion(ArrayList<InputVO> inVOList,
			ArrayList<ArrayList<InputNounVO>> inNounVOList,
			ArrayList<ArrayList<InputVerbVO>> inVerbVOList) {

		double fAngryProb = 0, fHappyProb = 0, fLoveProb = 0, fSadProb = 0;
		int size = inVOList.size();
		int finalEmo = 0;

		ArrayList<Double> temp;
		ArrayList<Double> nounTemp;
		ArrayList<Double> verbTemp;

		for (int i = 0; i < size; i++) {

			if (inVOList.get(i).getNounEmoProb() == null) {

				temp = inVOList.get(i).getVerbEmoProb();
				if (temp != null) {
					fAngryProb = temp.get(0) * ANGRY_PROB;
					fHappyProb = temp.get(1) * HAPPY_PROB;
					fLoveProb = temp.get(2) * LOVE_PROB;
					fSadProb = temp.get(3) * SAD_PROB;
					
					System.out.println("**** 해당 문장의 감정 확률 ****");
					System.out.println("Angry : "+fAngryProb);
					System.out.println("Happy : "+fHappyProb);
					System.out.println("Love : "+fLoveProb);
					System.out.println("Sad : "+fSadProb);
					System.out.println("**********************");
					System.out.println();
					
					finalEmo = findEmo(fAngryProb, fHappyProb, fLoveProb,
							fSadProb);
					inVOList.get(i).setTextEmotion(finalEmo);

					int size2 = inVerbVOList.get(i).size();
					for (int j = 0; j < size2; j++) {
						inVerbVOList.get(i).get(j).setWordEmo(finalEmo);
					}
					fAngryProb = 0;
					fHappyProb = 0;
					fLoveProb = 0;
					fSadProb = 0;
					finalEmo = 0;
				}
			}

			else if (inVOList.get(i).getVerbEmoProb() == null) {
				temp = inVOList.get(i).getNounEmoProb();
				if (temp != null) {
					fAngryProb = temp.get(0) * ANGRY_PROB;
					fHappyProb = temp.get(1) * HAPPY_PROB;
					fLoveProb = temp.get(2) * LOVE_PROB;
					fSadProb = temp.get(3) * SAD_PROB;
					
					System.out.println("**** 해당 문장의 감정 확률 ****");
					System.out.println("Angry : "+fAngryProb);
					System.out.println("Happy : "+fHappyProb);
					System.out.println("Love : "+fLoveProb);
					System.out.println("Sad : "+fSadProb);
					System.out.println("**********************");
					System.out.println();
					

					finalEmo = findEmo(fAngryProb, fHappyProb, fLoveProb,
							fSadProb);
					inVOList.get(i).setTextEmotion(finalEmo);

					int size2 = inNounVOList.get(i).size();
					for (int j = 0; j < size2; j++) {
						inNounVOList.get(i).get(j).setWordEmo(finalEmo);
					}
					fAngryProb = 0;
					fHappyProb = 0;
					fLoveProb = 0;
					fSadProb = 0;
					finalEmo = 0;
				}
			}

			else {
				nounTemp = inVOList.get(i).getNounEmoProb();
				verbTemp = inVOList.get(i).getVerbEmoProb();

				if (nounTemp != null && verbTemp != null) {
					fAngryProb = (verbTemp.get(0) * ANGRY_PROB) * 0.7
							+ (nounTemp.get(0) * ANGRY_PROB) * 0.3;
					fHappyProb = (verbTemp.get(1) * ANGRY_PROB) * 0.7
							+ (nounTemp.get(1) * ANGRY_PROB) * 0.3;
					fLoveProb = (verbTemp.get(2) * ANGRY_PROB) * 0.7
							+ (nounTemp.get(2) * ANGRY_PROB) * 0.3;
					fSadProb = (verbTemp.get(3) * ANGRY_PROB) * 0.7
							+ (nounTemp.get(3) * ANGRY_PROB) * 0.3;
					
					System.out.println("**** 해당 문장의 감정 확률 ****");
					System.out.println("Angry : "+fAngryProb);
					System.out.println("Happy : "+fHappyProb);
					System.out.println("Love : "+fLoveProb);
					System.out.println("Sad : "+fSadProb);
					System.out.println("**********************");
					System.out.println();

					finalEmo = findEmo(fAngryProb, fHappyProb, fLoveProb,
							fSadProb);
					inVOList.get(i).setTextEmotion(finalEmo);

					int size2 = inVerbVOList.get(i).size();
					int size3 = inNounVOList.get(i).size();

					for (int j = 0; j < size2; j++) {
						inVerbVOList.get(i).get(j).setWordEmo(finalEmo);
					}

					for (int k = 0; k < size3; k++) {
						inNounVOList.get(i).get(k).setWordEmo(finalEmo);
					}
					fAngryProb = 0;
					fHappyProb = 0;
					fLoveProb = 0;
					fSadProb = 0;
					finalEmo = 0;
				}

			}
		}
	}

	public void textAnalysis(String data) {
		ArrayList<InputVO> voList = new ArrayList<InputVO>();
		ArrayList<InputVO> dbVOList = new ArrayList<InputVO>();
		ArrayList<EmoWordVO> textNounList = new ArrayList<EmoWordVO>();
		ArrayList<String> strList = new ArrayList<String>();
		int numOfText = 0;

		try {
			numOfText = this.numOfText(data);
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		for (int z = 0; z < numOfText; z++) {

			try {
				strList = this.DataInput(data);
			} catch (Exception e) {
				e.printStackTrace();
			}

			voList = this.InitializeObj(strList.get(z));
			dbVOList.add(voList.get(0));

			ArrayList<String> temp = null;
			EmoWordVO tempVO = null;

			int size2 = voList.size();
			for (int i = 0; i < size2; i++) {
				int noun = voList.get(i).getNounList().size();
				
				temp = new ArrayList<String>();
				for (int j = 0; j < noun; j++) {
					String str = voList.get(i).getNounList().get(j);
					temp.add(str);
				}

				if (temp != null) {
					tempVO = new EmoWordVO();
					tempVO.setNounList(temp);
				}

				textNounList.add(tempVO);
			}

			ArrayList<ArrayList<InputNounVO>> nounDataList = new ArrayList<ArrayList<InputNounVO>>();
			ArrayList<ArrayList<InputVerbVO>> verbDataList = new ArrayList<ArrayList<InputVerbVO>>();

			nounDataList = this.setNounEmoCount(voList);
			verbDataList = this.setVerbEmoCount(voList);
			
			this.classificationWord(nounDataList, verbDataList, voList);		
			this.setEmotion(voList, nounDataList, verbDataList);
			
			
		}

		int size = dbVOList.size();
		for (int i = 0; i < size; i++) {
			if (textNounList.get(i).getNounList() != null) {
				int size2 = textNounList.get(i).getNounList().size();
				for (int j = 0; j < size2; j++) {
					textNounList.get(i)
							.setEmp(dbVOList.get(i).getTextEmotion());
				}
			}
		}
		EmotionDao dao = new EmotionDao();
		dao.insertTextList(dbVOList);
		dao.insertNounList(textNounList);
		
		System.out.println("\n\n");
		for(int i=0; i<dbVOList.size(); i++){
			System.out.println("<<"+dbVOList.get(i).getInputText() +">>의 감정 : " + dbVOList.get(i).getTextEmotion());
		}
	}

	public void voiceAnalysis(String inStr) {

		ArrayList<InputVO> voList = new ArrayList<InputVO>();
		ArrayList<InputVO> dbVOList = new ArrayList<InputVO>();
		ArrayList<EmoWordVO> textNounList = new ArrayList<EmoWordVO>();
		ArrayList<String> strList = new ArrayList<String>();

		strList.add(inStr);

		voList = this.InitializeObj(strList.get(0));
		dbVOList.add(voList.get(0));

		ArrayList<String> temp = null;
		EmoWordVO tempVO = null;

		int size2 = voList.size();
		for (int i = 0; i < size2; i++) {
			int noun = voList.get(i).getNounList().size();

			temp = new ArrayList<String>();
			for (int j = 0; j < noun; j++) {
				String str = voList.get(i).getNounList().get(j);
				temp.add(str);
			}

			if (temp != null) {
				tempVO = new EmoWordVO();
				tempVO.setNounList(temp);
			}

			textNounList.add(tempVO);
		}

		ArrayList<ArrayList<InputNounVO>> nounDataList = new ArrayList<ArrayList<InputNounVO>>();
		ArrayList<ArrayList<InputVerbVO>> verbDataList = new ArrayList<ArrayList<InputVerbVO>>();

		nounDataList = this.setNounEmoCount(voList);
		verbDataList = this.setVerbEmoCount(voList);

		this.classificationWord(nounDataList, verbDataList, voList);
		this.setEmotion(voList, nounDataList, verbDataList);

		int size = dbVOList.size();
		for (int i = 0; i < size; i++) {
			if (textNounList.get(i).getNounList() != null) {
				int size3 = textNounList.get(i).getNounList().size();
				for (int j = 0; j < size3; j++) {
					textNounList.get(i)
							.setEmp(dbVOList.get(i).getTextEmotion());
				}
			}
		}

		EmotionDao dao = new EmotionDao();
		dao.insertTextList(dbVOList);
		dao.insertNounList(textNounList);
		
		
		
	}
	
	public ArrayList<ArrayList<String>> getTotalEmoWord(){
		EmotionDao dao = new EmotionDao();
		return dao.getEmoWordList();
	}
	
	public double[] getTotalProb(){
		EmotionDao dao = new EmotionDao();
		return dao.getTotalProb();
	}
}