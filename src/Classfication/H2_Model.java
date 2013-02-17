/*=====================================================================================*/
/*Project : 		階層式貝氏分類
/*執行功能：	第二層分類器，利用合適的Feature來分類動作類別
/*=====================================================================================*/

package Classfication;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;



public class H2_Model {
	int feature_len=0;		// The Number of features   //
	int feature_ind=0;		// The index of the Start of feature //
	private ArrayList<ArrayList<Double>> features= new ArrayList<ArrayList<Double>>();
	private ArrayList<String> F_class = new ArrayList<String>();	// the class of each instance
	private ArrayList<String> _class = new ArrayList<String>();		// the class type of all instance 
	private double[] max=new double[feature_len],min=new double[feature_len];
	private int[] cn={0,0,0,0,0,0,0,0,0,0,0};
	private double[] interval=new double[feature_len];		//記錄各feature離散化的Threshold
	private double[][] model=null;
	private int interval_num=10;
	
	public H2_Model(String path, int len, int ind){
		super();
		feature_len=len;
		feature_ind=ind;
		LoadData(path);		// 讀取train Data //
		transferClass();	// 離散化 //
		constructModel();	// 建立Model //
	}
	
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
		/*		初始化List		*/
		for(int i=0;i<feature_len;i++){
			ArrayList<Double> tmp = new ArrayList<Double>();
			features.add(tmp);
			max[i]=-65535;
			min[i]=65535;
		}
		while(sc.hasNext()){
			line=sc.nextLine();
			//System.out.println(line);
			String[] lsc=line.split(",");
			for(int i=0;i<feature_len;i++){
				double instance = Double.parseDouble(lsc[i+feature_ind]);
				features.get(i).add(instance);
				if(max[i]<instance)
					max[i]=instance;
				if(min[i]>instance)
					min[i]=instance;
			}
			F_class.add(lsc[2]);
			/*	檢查類別標籤是否重複	*/
			int tmp = _class.indexOf(lsc[2]);
			if(tmp==-1){
				_class.add(lsc[2]);
				cn[_class.indexOf(lsc[2])]++;
			}else{
				cn[_class.indexOf(lsc[2])]++;
			}
		}
		try {
			in.close();
		} catch (IOException e) {
		// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/*		將各feature離散化，分成數個區間		*/
	public void transferClass(){
		for(int i=0;i<max.length;i++){
			interval[i]=(max[i]-min[i])/interval_num;
		}
		for(int i=0;i<F_class.size();i++){
			for(int j=0;j<features.size();j++){
				features.get(j).set(i, intervalDecision(interval[j],max[j],min[j],features.get(j).get(i)));
			}
		}
		
	}
	
	/*		建立貝氏分類模型		*/
	public void constructModel(){
		model = new double[interval_num*_class.size()][features.size()+1];
		for(int i=0;i<interval_num*_class.size()*(features.size()+1);i++){
			if(i%(features.size()+1)==feature_len){
				model[i/(features.size()+1)][i%(features.size()+1)]=(double)cn[(i/(features.size()+1))/interval_num]/(double)F_class.size();
			}else{
				model[i/(features.size()+1)][i%(features.size()+1)]=0;
			}
		}
		for(int i=0;i<F_class.size();i++){
			int _classIndex=_class.indexOf(F_class.get(i));
			for(int j=0;j<features.size();j++){
				int loc=(int) (_classIndex*interval_num+features.get(j).get(i));
				model[loc][j]+=(double)1/(double)cn[_classIndex];
			}
		}
		
	}
	
	/*		將feature的值與threshold丟入，得到離散化的數字		*/
	public double intervalDecision(double interval, double max, double min, double data){
		if(data > max-interval)
			return 0;
		else if(data > max-2*interval)
			return 1;
		else if(data > max-3*interval)
			return 2;
		else if(data > max-4*interval)
			return 3;
		else if(data > max-5*interval)
			return 4;
		else if(data > max-6*interval)
			return 5;
		else if(data > max-7*interval)
			return 6;
		else if(data > max-8*interval)
			return 7;
		else if(data > max-9*interval)
			return 8;
		else
			return 9;
	}
	
	/*		利用分類模型，計算Test instance 的機率並回復結果		*/
	public double Classify(ArrayList<Double> Data){
		ArrayList<Double> TestData=new ArrayList<Double>();
		for(int j=0;j<Data.size();j++){
			TestData.add(intervalDecision(interval[j],max[j],min[j],Data.get(j)));
//			TestData.set(j, intervalDecision(interval[j],max[j],min[j],TestData.get(j)));
		}
		double[] result= new double[model.length/interval_num];
		for(int j=0;j<model.length/interval_num;j++){
			result[j]=model[j*interval_num][model[0].length-1];
			for(int k=0;k<model[0].length-1;k++){
				result[j]*=model[(int) (j*interval_num+TestData.get(k))][k];
			}
		}
		int key = 0;
		double max_result=result[0];
		for(int j=1;j<result.length;j++){
			if(max_result<result[j]){
				key=j;
				max_result=result[j];
			}
		}
		return key;
	}
	
	/*		取得分類模型		*/
	public double[][] getModel(){
		return model;
	}
	
	/*		取得各feature的離散化threshold		*/
	public double[][] getloctable(){
		return new double[][]{max,min,interval};
	}
	
	/*		取得類別名稱		*/
	public String getclass(int key){
		return _class.get(key);
	}
	
}
