/*=====================================================================================*/
/*Project : 		階層式貝氏分類
/*執行功能：	主程式，讀取Test Data , 建立分層的分類器並辨識計算準確率
/*關聯檔案：	H1_Model.java, H2_Model.java
/*=====================================================================================*/

package Classfication;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

public class Classfier {
	int feature_len=16;		// Total number of features   //
	int feature_ind=3;		// The index of the Start of feature //
	int Tyfeature_len=7;	// Total number of features   //
	int Tyfeature_ind=0;	// The index of the Start of feature //
	int DTfeature_len=7;	// Total number of features   //
	int DTfeature_ind=6;	// The index of the Start of feature //
	int STfeature_len=6;	// Total number of features   //
	int STfeature_ind=0;	// The index of the Start of feature //
	
	/*		The Testing Data List		*/
	private ArrayList<ArrayList<Double>> Testfeatures= new ArrayList<ArrayList<Double>>();
	private ArrayList<String> Testtype = new ArrayList<String>();
	private ArrayList<String> Testloc = new ArrayList<String>();
	private ArrayList<String> Testclass = new ArrayList<String>();
	/*		The Testing Data List of Dynamic or Static Activity		*/
	private ArrayList<ArrayList<Double>> DTfeatures= new ArrayList<ArrayList<Double>>();
	private ArrayList<String> DTclass = new ArrayList<String>();
	private ArrayList<String> DTloc = new ArrayList<String>();
	private ArrayList<ArrayList<Double>> STfeatures= new ArrayList<ArrayList<Double>>();
	private ArrayList<String> STclass = new ArrayList<String>();
	
	H1_Model h1;
	H2_Model h2_d;
	H2_Model h2_s;
	
	public Classfier(String path){
		super();
		LoadData(path);		// Load the Testing Data	//
		h1 = new H1_Model("Data/trainingAll_ori.csv");			//Built Type Classifier Model//
		h2_d = new H2_Model("Data/trainingAll_ori_dynamic.csv",9,7);	//Built Dynamic Classifier Model//
		h2_s = new H2_Model("Data/trainingAll_ori_static.csv",6,3);		//Built Static Classifier Model//
		HA1_classfy();	// Start Testing
	}
	
	public void clear(){
		Testfeatures.clear();
		Testtype.clear();
		Testloc.clear();
		Testclass.clear();
		DTfeatures.clear();
		DTclass.clear();
		DTloc.clear();
		STfeatures.clear();
		STclass.clear();
	}
	
	/*		讀取Testing Data 並轉成List		*/
	public void LoadData(String path){
		FileReader  in = null;
		Scanner sc;
		try {
			in=new FileReader(path);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		sc=new Scanner(in);
		String line;
		line=sc.nextLine();
		String[] an=line.split(",");
		for(int i=0;i<feature_len;i++){
			Testfeatures.add(new ArrayList<Double>());
		}
		while(sc.hasNext()){
			line=sc.nextLine();
			//System.out.println(line);
			String[] lsc=line.split(",");
			/*		把Data按照格式，還有Feature的數量，存到各自的List中		*/
			for(int i=0;i<feature_len;i++){
				double instance = Double.parseDouble(lsc[i+feature_ind]);
				Testfeatures.get(i).add(instance);
			}
			Testtype.add(lsc[0]);
			Testloc.add(lsc[1]);
			Testclass.add(lsc[2]);
		}
		try {
			in.close();
		} catch (IOException e) {
		// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public Double HA1_classfy(){
		/*		Initialize the List		*/
		for(int i=0;i<DTfeature_len;i++){
			DTfeatures.add(new ArrayList<Double>());
		}
		for(int i=0;i<STfeature_len;i++){
			STfeatures.add(new ArrayList<Double>());
		}
		
		double avg=0;
		/*		先判斷Type 並把Static與Dynamic的Test Data 分開		*/
		for(int i=0;i<Testtype.size();i++){
			ArrayList<Double> TestData = new ArrayList<Double>();
			for(int j=0;j<Testfeatures.size();j++){
				TestData.add(Testfeatures.get(j).get(i));
			}
			double r1=h1.Classify(TestData);
			String result=h1.getclass((int)r1);
			if(Testtype.get(i).equals(result))
				avg++;
			/*		依照判斷結果把每個instance放入Dynamic的Test List		*/
			if(result.equals("dynamic")){
				for(int k=0;k<DTfeature_len;k++){
					DTfeatures.get(k).add(Testfeatures.get(k+DTfeature_ind).get(i));
				}
				DTclass.add(Testclass.get(i));
				DTloc.add(Testloc.get(i));
			}
			/*		依照判斷結果把每個instance放入Static的Test List		*/
			else if(result.equals("static")){
				for(int k=0;k<STfeature_len;k++){
					STfeatures.get(k).add(Testfeatures.get(k+STfeature_ind).get(i));
				}
				STclass.add(Testclass.get(i));
			}
		}
		System.out.println("Type Acc = "+avg/Testtype.size());
		int all_avg=0;
		/*	第二階層分別判斷動態與靜態的動作	*/
		all_avg+=HA2_D_classfy();
		all_avg+=HA2_S_classfy();
		System.out.println("All_Acc = "+(double)all_avg/Testtype.size()+"\n");
		
		return (double)all_avg/Testtype.size();
	}
	
	/*		辨識動態類別		*/
	public int HA2_D_classfy(){
		int avg=0;
		for(int i=0;i<DTclass.size();i++){
			ArrayList<Double> TestData = new ArrayList<Double>();
			for(int j=0;j<DTfeatures.size();j++){
				TestData.add(DTfeatures.get(j).get(i));
			}
			double r1=h2_d.Classify(TestData);
			String result=h2_d.getclass((int)r1);
			
			if(DTclass.get(i).equals(result))
				avg++;
//			else
//				System.out.println(DTloc.get(i)+" = "+result);
		}
		System.out.println("Dynamic Acc = "+(double)avg/DTclass.size());
		return avg;
	}
	
	/*		辨識靜態類別		*/
	public int HA2_S_classfy(){
		int avg=0;
		for(int i=0;i<STclass.size();i++){
			ArrayList<Double> TestData = new ArrayList<Double>();
			for(int j=0;j<STfeatures.size();j++){
				TestData.add(STfeatures.get(j).get(i));
			}
			double r1=h2_s.Classify(TestData);
			String result=h2_s.getclass((int)r1);
			
			if(STclass.get(i).equals(result))
				avg++;
		}
		System.out.println("Static Acc = "+(double)avg/STclass.size());
		return avg;
	}
	
	public static void main(String[] args) {
		new Classfier("Data/training_ori.csv");
	}
}
