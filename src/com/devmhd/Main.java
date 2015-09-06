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

public class Main {


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
	
	public static void aggregateFolder(File folder, String outputFileName){
		
		HashMap<String,Integer> syscallFrequency = new HashMap<String,Integer>(), singleAppFreqs;

		int n_apps = 0;

		for(File dumpFile : folder.listFiles()){

			n_apps++;

			try {

				singleAppFreqs = getFrequenciesFromFile(dumpFile);
				
				for(Map.Entry<String, Integer> entry : singleAppFreqs.entrySet())
				{
					if(syscallFrequency.containsKey(entry.getKey())){
			//			syscallFrequency.put(entry.getKey(), syscallFrequency.get(entry.getKey())+ entry.getValue() );
						syscallFrequency.put(entry.getKey(), syscallFrequency.get(entry.getKey())+ 1 );
					} else {

			//			syscallFrequency.put(entry.getKey(), new Integer(entry.getValue()));
						syscallFrequency.put(entry.getKey(), new Integer(1));

					}
				}

				//				
			} catch (IOException e) {
				// TODO Auto-generated catch block
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
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {

		aggregateFolder(new File("training-data/good"), "output-good.csv");
		aggregateFolder(new File("training-data/malware"), "output-malware.csv");


	}

}
