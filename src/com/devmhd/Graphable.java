package com.devmhd;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

public class Graphable {
	
	private ArrayList<FloatPair> list;
	
	public Graphable(){
		list = new ArrayList<FloatPair>();
	}
	
	public void add(FloatPair pair){
		
		System.out.println(pair.toString());
		list.add(pair);
	}
	
	public void outputCsv(String filename){
		
		PrintWriter writer;
		try {
			writer = new PrintWriter(filename, "UTF-8");
		


		for(FloatPair pair : list)
		{
			String word = pair.f1 + "," + pair.f2;
			writer.println(word);
		}

		writer.close();
		
		} catch (FileNotFoundException | UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}

}
