package com;

import java.io.IOException;

import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

public class MyGenderFilter extends TokenFilter {
	
	MyGenderAttribute genderAttribute = addAttribute(MyGenderAttribute.class);
	CharTermAttribute charTermAttribute = addAttribute(CharTermAttribute.class);

	protected MyGenderFilter(TokenStream input) {
		super(input);
	}

	@Override
	public boolean incrementToken() throws IOException {
		if(!input.incrementToken()) return false;
		genderAttribute.setGender(determineGeder(charTermAttribute.toString()));
		return false;
	}
	
	protected MyGenderAttribute.Gender determineGeder(String term){
		if(term.equals("mr") || term.equalsIgnoreCase("mister")){
			return MyGenderAttribute.Gender.MALE;
		}
		else if(term.equals("mrs") || term.equalsIgnoreCase("misters")){
			return MyGenderAttribute.Gender.FEMALE;
		}
		else {
			return MyGenderAttribute.Gender.UNSPECIFIED;
		}
	}

}
