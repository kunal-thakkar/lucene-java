package com;

import java.io.Reader;

import org.apache.lucene.analysis.Analyzer;

public class MyAnalyzer extends Analyzer {

	@Override
	protected TokenStreamComponents createComponents(String arg0, Reader arg1) {
		return null;
	}

}
