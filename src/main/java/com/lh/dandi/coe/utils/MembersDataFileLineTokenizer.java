package com.lh.dandi.coe.utils;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.file.transform.DefaultFieldSetFactory;
import org.springframework.batch.item.file.transform.FieldSet;
import org.springframework.batch.item.file.transform.FieldSetFactory;
import org.springframework.batch.item.file.transform.LineTokenizer;

public class MembersDataFileLineTokenizer implements LineTokenizer{
	
	private FieldSetFactory fieldSetFactory = new DefaultFieldSetFactory();
	
	public final static Logger logger = LoggerFactory.getLogger(MembersDataFileLineTokenizer.class.getName());	

	public FieldSet tokenize(String row) {
		
		final String tokenizer = ",\"";
		
		String[] fields = row.split(tokenizer);
		
		List<String> parsedFields = new ArrayList<String>();
		for (int i = 0; i < fields.length; i++) {
			parsedFields.add(fields[i]);
		}
		
		FieldSet fieldSet = null;
		try{
			fieldSet = fieldSetFactory.create(parsedFields.toArray(new String [0]));
		}catch(Exception e){
			logger.error("File parsing exception");
		}
		return fieldSet;
	}

}
