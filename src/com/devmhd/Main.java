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

	private static HashMap<String, Float> goodnessRatings;

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
	
	public static void calculateGoodnessRatings(HashMap<String, Float> goodnessMap, HashMap<String, Float> badnnessMap){
		
		goodnessRatings = new HashMap<String, Float>();
		
		for(Entry<String, Float> entry : goodnessMap.entrySet()){
			
			goodnessRatings.put(entry.getKey(), entry.getValue() - badnnessMap.get(entry.getKey()));
			
		}
		
		
	}

	
	public static void main(String[] args) {

		
		//aggregate results 
		HashMap<String, Float> goodnessMap = aggregateFolder(new File("training-data/good"), "output-good.csv");
		HashMap<String, Float> badnessMap = aggregateFolder(new File("training-data/malware"), "output-malware.csv");
		
		
		
		
		
		
		
		
		


	}

}
