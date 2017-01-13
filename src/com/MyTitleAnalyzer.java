package com;

import java.io.Reader;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.core.LetterTokenizer;

public class MyTitleAnalyzer extends Analyzer {

	@Override
	protected TokenStreamComponents createComponents(String arg0, Reader in) {
		Tokenizer tokenizer = new LetterTokenizer(in);
		TokenStream filter = new MyTitleFilter(tokenizer);
		return new TokenStreamComponents(tokenizer, filter);
	}

}
