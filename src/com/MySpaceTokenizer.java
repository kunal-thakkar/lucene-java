package com;

import java.io.Reader;

import org.apache.lucene.analysis.util.CharTokenizer;
import org.apache.lucene.util.AttributeFactory;

public class MySpaceTokenizer extends CharTokenizer {

	public MySpaceTokenizer(Reader reader){
		super(reader);
	}
	
	public MySpaceTokenizer(AttributeFactory factory, Reader input) {
		super(factory, input);
	}
	
	@Override
	protected boolean isTokenChar(int arg0) {
		return !Character.isSpaceChar(arg0);
	}

}
