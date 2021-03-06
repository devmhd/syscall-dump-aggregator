package com.devmhd;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class Main {
	
	static float maxgr = 0, mingr  = 10000;

	private static HashMap<String, Float> goodnessRatings;
	private static Graphable thVsACC, thVsTPR, thVsSPC, thVsPPV, thVsFmeasure;

	public static ArrayList<String> removeEmptyStrings(String[] array){
		ArrayList<String> list = new ArrayList<String>();
		for(String str : array)
			if(!str.isEmpty())
				list.add(str);

		return list;
	}

	public static HashMap<String,Integer> getFrequenciesFromFile(File file) throws NumberFormatException, IOException{
		HashMap<String,Integer> map = new HashMap<String,Integer>();

		FileInputStream fStream = new FileInputStream(file);
		BufferedReader reader = new BufferedReader(new InputStreamReader(fStream));

		String line;
		boolean readingNow = false;

		int count = 0;

		ArrayList<String> record;
		String syscallname;
		Integer n_calls;

		while ((line = reader.readLine()) != null)   {

			if(line.isEmpty()) continue;

			if(line.startsWith("-")){
				readingNow = ! readingNow;
				continue;
			}

			if(readingNow){

				record = removeEmptyStrings(line.split(" "));

				syscallname = record.get(record.size()-1);
				n_calls = Integer.parseInt(record.get(3));

				map.put(syscallname, n_calls);

				count++;
			}

		}

		reader.close();

		if(count==0) System.out.println("No syscall: " + file.getName());

		return map;

	}

	public static HashMap<String, Float> normalize(HashMap<String, Integer> map, int total){

		HashMap<String, Float> newMap = new HashMap<String, Float>();

		for(Entry<String, Integer> entry : map.entrySet())
		{
			newMap.put(entry.getKey(), (float)entry.getValue()/(float)total);
		}

		return newMap;
	}

	public static HashMap<String, Float> aggregateFolder(File folder, String outputFileName){

		HashMap<String,Integer> syscallFrequency = new HashMap<String,Integer>(), singleAppFreqs;

		int n_apps = 0;
		for(File dumpFile : folder.listFiles()){

			n_apps++;

			try {

				singleAppFreqs = getFrequenciesFromFile(dumpFile);

				for(Map.Entry<String, Integer> entry : singleAppFreqs.entrySet())
				{
					if(syscallFrequency.containsKey(entry.getKey())){
						syscallFrequency.put(entry.getKey(), syscallFrequency.get(entry.getKey())+ 1 );
					} else {
						syscallFrequency.put(entry.getKey(), new Integer(1));
					}
				}

			} catch (IOException e) {
				e.printStackTrace();
			}		
		}


		PrintWriter writer;
		try {
			writer = new PrintWriter(outputFileName, "UTF-8");
			writer.println("" + n_apps);

			for(Map.Entry<String, Integer> entry : syscallFrequency.entrySet())
			{
				String word = entry.getKey() + "," + entry.getValue();
				writer.println(word);
			}

			writer.close();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

		return normalize(syscallFrequency, n_apps);
	}

	public static void calculateGoodnessRatings(HashMap<String, Float> goodnessMap, HashMap<String, Float> badnessMap){

		goodnessRatings = new HashMap<String, Float>();

		for(Entry<String, Float> entry : goodnessMap.entrySet()){

			goodnessRatings.put(entry.getKey(), entry.getValue() - ((badnessMap.get(entry.getKey()) == null)?0:(badnessMap.get(entry.getKey()))));
		}
	}

	public static float getGoodnessRating(String syscall){

		if(goodnessRatings.containsKey(syscall)){
			return goodnessRatings.get(syscall);
		} else {
			return (float) 0;
		}
	}

	public static boolean isMalware(File traceFile, float threshold) throws IOException{

		FileInputStream fStream = new FileInputStream(traceFile);
		BufferedReader reader = new BufferedReader(new InputStreamReader(fStream));

		String line;
		boolean readingNow = false;

		int count = 0;

		ArrayList<String> record;
		String syscallname;
		Integer n_calls;

		float sum = 0;

		while ((line = reader.readLine()) != null)   {

			if(line.isEmpty()) continue;

			if(line.startsWith("-")){
				readingNow = ! readingNow;
				continue;
			}

			if(readingNow){

				record = removeEmptyStrings(line.split(" "));
				syscallname = record.get(record.size()-1);
				n_calls = Integer.parseInt(record.get(3));
				sum += (getGoodnessRating(syscallname) * (float)n_calls);
				count++;	
			}

		}
		
		if(sum > maxgr) maxgr = sum;
		if(sum < mingr) mingr = sum;
		reader.close();
		if(count==0) System.out.println("No syscall in classification: " + traceFile.getName());

		return sum < threshold;

	}

	public static void runWithThreshold(float threshold){
		
		try{

			//aggregate results 
			HashMap<String, Float> goodnessMap = aggregateFolder(new File("training-data/good"), "output-good.csv");
			HashMap<String, Float> badnessMap = aggregateFolder(new File("training-data/malware"), "output-malware.csv");

			calculateGoodnessRatings(goodnessMap, badnessMap);


			//validation
			float tp = 0, tn = 0, fp = 0, fn = 0; 

			File malValidationFolder = new File("validation-data/malware");
			File nmalValidationFolder = new File("validation-data/non-malware");

			for(File malwareTrace: malValidationFolder.listFiles()){
				

				if(isMalware(malwareTrace, threshold)) tp++;
				else fn++;
				
			}

			for(File nmalwareTrace: nmalValidationFolder.listFiles()){

				if(isMalware(nmalwareTrace, threshold)) fp++;
				else tn++;
					
			}
			
//			System.out.println( "tp " + tp*100/69 + " tn " + tn*100/69 + " fp " + fp*100/69 + " fn " + fn*100/69) ;

			thVsACC.add(new FloatPair(threshold, (tp+tn)/(tp+tn+fp+fn)));
			thVsTPR.add(new FloatPair(threshold, tp/(tp+fn)));
			thVsSPC.add(new FloatPair(threshold, tn/(fp+tn)));
			thVsPPV.add(new FloatPair(threshold, tp/(tp+fp)));
			thVsFmeasure.add(new FloatPair(threshold, 2*tp/(2*tp + fp + fn)));


		} catch (Exception e) {

			e.printStackTrace();
		}
	}


	public static void main(String[] args) {


		thVsACC = new Graphable();
		thVsTPR = new Graphable();
		thVsSPC = new Graphable();
		thVsPPV = new Graphable();
		thVsFmeasure = new Graphable();
		
//		runWithThreshold((float) 320);

		for(float threshold = -200; threshold <= 1500; threshold +=10){
			
			runWithThreshold(threshold);
		}

		thVsACC.outputCsv("thVsACC.csv");
		thVsTPR.outputCsv("thVsTPR.csv");
		thVsSPC.outputCsv("thVsSPC.csv");
		thVsPPV.outputCsv("thVsPPV.csv");
		thVsFmeasure.outputCsv("thVsFmeasure.csv");
		
		System.out.println("" + mingr + "   " + maxgr);

	}

}
