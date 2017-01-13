package com;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

public class MyTitleFilter extends TokenFilter {

	private Map<String, String> titleMap = new HashMap<String, String>();
	private CharTermAttribute charTermAttribute;
	
	protected MyTitleFilter(TokenStream input) {
		super(input);
		charTermAttribute = addAttribute(CharTermAttribute.class);
		titleMap.put("Dr", "doctor");
		titleMap.put("Mr", "mister");
		titleMap.put("Mrs", "miss");
	}

	@Override
	public boolean incrementToken() throws IOException {
		if(!input.incrementToken()){
			return false;
		}
		String small = charTermAttribute.toString();
		if(titleMap.containsKey(small)){
			charTermAttribute.setEmpty().append(titleMap.get(small));
		}
		return true;
	}

}
