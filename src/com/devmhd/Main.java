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
	
	
	private HashMap<String,Integer> syscallFrequency = new HashMap<String,Integer>();

	
	public static ArrayList<String> removeEmptyStrings(String[] array){
		ArrayList<String> list = new ArrayList<String>();
		for(String str : array)
			if(!str.isEmpty())
				list.add(str);
		
		return list;
	}
	
	public static void main(String[] args) {
		
		
		HashMap<String,Integer> syscallFrequency = new HashMap<String,Integer>();
		
		
		FileInputStream fStream;
		BufferedReader reader;
		ArrayList<String> record;
		String syscallname;
		Integer n_calls;
		
		File trainingFolder = new File("training-data");
		
		for(File dumpFile : trainingFolder.listFiles()){
			
			try {
				
				fStream = new FileInputStream(dumpFile);
				reader = new BufferedReader(new InputStreamReader(fStream));
				
				String line;
				boolean readingNow = false;
				
				int count = 0;

				while ((line = reader.readLine()) != null)   {

					if(line.isEmpty()) continue;
					
					if(line.startsWith("-")){
						readingNow = ! readingNow;
						continue;
					}
					
					if(readingNow){
						
		//				System.out.println(line);
						record = removeEmptyStrings(line.split(" "));
						
						syscallname = record.get(record.size()-1);
						n_calls = Integer.parseInt(record.get(3));
						
						
						if(syscallFrequency.containsKey(syscallname)){
							syscallFrequency.put(syscallname, syscallFrequency.get(syscallname)+ n_calls );
						} else {
							
							syscallFrequency.put(syscallname, new Integer(n_calls));
													
						}
						
						
					count++;	
						
					}
					
					


				}
				
				System.out.println("" + count + " " + dumpFile.getName());
				
				
				
				
				
				
				
				
				
				
				
				
				
//				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
//			
//			
//			
//			
//			
		}
		
		
		PrintWriter writer;
		try {
			writer = new PrintWriter("output.csv", "UTF-8");
			
			
			
			
			
			
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

}
