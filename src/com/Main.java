package com;

import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.analysis.miscellaneous.PerFieldAnalyzerWrapper;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.document.NumericDocValuesField;
import org.apache.lucene.document.SortedDocValuesField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.AtomicReader;
import org.apache.lucene.index.AtomicReaderContext;
import org.apache.lucene.index.BinaryDocValues;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.DocValues;
import org.apache.lucene.index.DocsAndPositionsEnum;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.SlowCompositeReaderWrapper;
import org.apache.lucene.index.SortedDocValues;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.FieldCache;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.WildcardQuery;
import org.apache.lucene.search.similarities.DefaultSimilarity;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.Version;

public class Main {

	public static void main(String args[]) throws IOException{
		//docValue();
		//similarity();
		fieldCache();
	}

	public static void similarity() throws IOException{
		Analyzer analyzer = new StandardAnalyzer();
		Directory directory = new RAMDirectory();
		IndexWriterConfig config = new IndexWriterConfig(Version.LATEST, analyzer);
		
		MySimilarity mySimilarity = new MySimilarity(new DefaultSimilarity());
		config.setSimilarity(mySimilarity);
		
		IndexWriter writer = new IndexWriter(directory, config);
		Document document = new Document();
		TextField textField = new TextField("full_name", "", Store.YES);
		NumericDocValuesField docValuesField = new NumericDocValuesField("ranking", 1);
		long ranking = 1L;
		String names[] = {"John R Smith", "Mary Smith", "Peter Smith"};
		
		for(String string : names){
			ranking *= 2;
			textField.setStringValue(string);
			docValuesField.setLongValue(ranking);
			document.removeField("full_name");
			document.removeField("ranking");
			document.add(textField);
			document.add(docValuesField);
			writer.addDocument(document);
		}
		writer.close();
		
		IndexReader indexReader = DirectoryReader.open(directory);
		IndexSearcher indexSearcher = new IndexSearcher(indexReader);
		indexSearcher.setSimilarity(mySimilarity);
		Query query = new TermQuery(new Term("full_name", "smith"));
		TopDocs topDocs = indexSearcher.search(query, 100);
		System.out.println("Searching 'smith'");
		for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
			document = indexReader.document(scoreDoc.doc);
			System.out.println(document.getField("full_name").stringValue());
		}
	}
	
	public static void fieldValue() throws Exception {
		Document doc = new Document();
		doc.add(new TextField("fName", "Kunal", Store.YES));
		
		Directory dir = new RAMDirectory();
		Analyzer analyzer = new StandardAnalyzer();
		IndexWriterConfig config = new IndexWriterConfig(Version.LATEST, analyzer);
		config.setRAMBufferSizeMB(64);
		config.setMaxBufferedDocs(4000);

		IndexWriter writer = new IndexWriter(dir, config);
		writer.addDocument(doc);
		writer.close();
		
		IndexReader reader = DirectoryReader.open(dir);
		IndexSearcher indexSearcher = new IndexSearcher(reader);
		
		QueryParser parser = new QueryParser("fName", analyzer);
		Query query = parser.parse("Kunal");
		
		int hitsPerPage = 10;
		TopDocs docs = indexSearcher.search(query, hitsPerPage);
		ScoreDoc[] hits = docs.scoreDocs;
		int end = Math.min(docs.totalHits, hitsPerPage);
		System.out.print("Total Hits: " + docs.totalHits);
		System.out.print("Results: ");
		for (int i = 0; i < end; i++) {
			Document d = indexSearcher.doc(hits[i].doc);
			System.out.println("fName: " + d.get("fName"));
		}
		
	}
		
	public static void docValue() throws IOException{
		Analyzer analyzer = new StandardAnalyzer();
		Directory directory = new RAMDirectory();
		IndexWriterConfig config = new IndexWriterConfig(Version.LATEST, analyzer);
		IndexWriter writer = new IndexWriter(directory, config);
		
		Document document = new Document();
		document.add(new SortedDocValuesField("fName", new BytesRef("kunal")));
		writer.addDocument(document);
		
		document = new Document();
		document.add(new SortedDocValuesField("fName", new BytesRef("mahesh")));
		writer.addDocument(document);
		
		writer.close();
		
		IndexReader reader = DirectoryReader.open(directory);
		document = reader.document(0);
		System.out.println(document);
		
		document = reader.document(1);
		System.out.println(document);
		
		for(AtomicReaderContext atomicReaderContext : reader.leaves()){
			AtomicReader atomicReader = atomicReaderContext.reader();
			SortedDocValues sortedDocValues = DocValues.getSorted(atomicReader, "fName");
			System.out.println("Value count : "+sortedDocValues.getValueCount());
			System.out.println("Doc 0 fName : "+sortedDocValues.get(0).utf8ToString());
			System.out.println("Doc 1 fName : "+sortedDocValues.get(1).utf8ToString());
		}
		reader.close();
	}
	
	public void attributes(Analyzer analyzer){
		StringReader reader = new StringReader("Lucene is mainly used for information retrieval and you can read more about it at lucene.apache.org.");
		TokenStream ts = null;
		try {
			ts = analyzer.tokenStream("field", reader);
			OffsetAttribute offsetAtt = ts.addAttribute(OffsetAttribute.class);
			CharTermAttribute termAtt = ts.addAttribute(CharTermAttribute.class);
			TypeAttribute typeAttribute = ts.addAttribute(TypeAttribute.class);
			ts.reset();
			while (ts.incrementToken()) {
				String token = termAtt.toString();
				System.out.println("[" + token + "]");
				System.out.println("Token type : "+typeAttribute.type());
				System.out.println("Token starting offset: " + offsetAtt.startOffset());
				System.out.println("Token ending offset: " + offsetAtt.endOffset());
				System.out.println("");
			}
			ts.end();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				ts.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			analyzer.close();
		}	
	}

	public void fieldWiseAnalyser(){
		Map<String, Analyzer> analyzerPerField = new HashMap<>();
		analyzerPerField.put("fName", new WhitespaceAnalyzer());
		PerFieldAnalyzerWrapper perFieldAnalyzerWrapper = new PerFieldAnalyzerWrapper(new StandardAnalyzer(), analyzerPerField);
		attributes(perFieldAnalyzerWrapper);
	}

	public static void fieldCache() throws IOException{
		Analyzer analyzer = new StandardAnalyzer();
		Directory directory = new RAMDirectory();
		IndexWriterConfig config = new IndexWriterConfig(Version.LATEST, analyzer);
		IndexWriter writer = new IndexWriter(directory, config);
		
		Document doc = new Document();
		StringField stringField = new StringField("name", "", Store.YES);
		
		String[] contents = {"alpha", "bravo", "charlie", "delta", "echo", "foxtrot"};
		for (String content : contents) {
			stringField.setStringValue(content);
			doc.removeField("name");
			doc.add(stringField);
			writer.addDocument(doc);
		}
		writer.commit();
		writer.close();
		IndexReader indexReader = DirectoryReader.open(directory);
		BinaryDocValues cache = FieldCache.DEFAULT.getTerms(SlowCompositeReaderWrapper.wrap(indexReader), "name", true);
		for (int i = 0; i < indexReader.maxDoc(); i++) {
			BytesRef bytesRef = cache.get(i);
			System.out.println(i + ": " + bytesRef.utf8ToString());
		}
	}
	
	public static void termVector() throws IOException{
		StandardAnalyzer analyzer = new StandardAnalyzer();
		Directory directory = new RAMDirectory();
		IndexWriterConfig config = new IndexWriterConfig(Version.LATEST, analyzer);
		IndexWriter indexWriter = new IndexWriter(directory, config);

		FieldType textFieldType = new FieldType();
		textFieldType.setIndexed(true);
		textFieldType.setTokenized(true);
		textFieldType.setStored(true);
		textFieldType.setStoreTermVectors(true);
		textFieldType.setStoreTermVectorPositions(true);
		textFieldType.setStoreTermVectorOffsets(true);
		
		Document doc = new Document();
		Field textField = new Field("content", "", textFieldType);
		String[] contents = {"Humpty Dumpty sat on a wall,",
			"Humpty Dumpty had a great fall.",
			"All the king's horses and all the king's men",
			"Couldn't put Humpty together again."};
		for (String content : contents) {
			textField.setStringValue(content);
			doc.removeField("content");
			doc.add(textField);
			indexWriter.addDocument(doc);
		}
		indexWriter.commit();
		IndexReader indexReader = DirectoryReader.open(directory);
		DocsAndPositionsEnum docsAndPositionsEnum = null;
		Terms termsVector = null;
		TermsEnum termsEnum = null;
		BytesRef term = null;
		String val = null;
		for (int i = 0; i < indexReader.maxDoc(); i++) {
			termsVector = indexReader.getTermVector(i, "content");
			termsEnum = termsVector.iterator(termsEnum);
			while ( (term = termsEnum.next()) != null ) {
				val = term.utf8ToString();
				System.out.println("DocId: " + i);
				System.out.println(" term: " + val);
				System.out.println(" length: " + term.length);
				docsAndPositionsEnum =
				termsEnum.docsAndPositions(null, docsAndPositionsEnum);
				if (docsAndPositionsEnum.nextDoc() >= 0) {
					int freq = docsAndPositionsEnum.freq();
					System.out.println(" freq: " +
					docsAndPositionsEnum.freq());
					for (int j = 0; j < freq; j++) {
						System.out.println(" [");
						System.out.println(" position: " +
						docsAndPositionsEnum.nextPosition());
						System.out.println(" offset start: " +
						docsAndPositionsEnum.startOffset());
						System.out.println(" offset end: " +
						docsAndPositionsEnum.endOffset());
						System.out.println(" ]");
					}
				}
			}
		}
		indexWriter.close();
	}

	public static void termQuery() throws IOException{
		Directory directory = new RAMDirectory();
		IndexReader indexReader = DirectoryReader.open(directory);
		IndexSearcher indexSearcher = new IndexSearcher(indexReader);
		Query query = new TermQuery(new Term("content", "alpha"));
		TopDocs topDocs = indexSearcher.search(query, 100);
	}
	
	public static void queryParser() throws IOException, ParseException{
		Directory directory = new RAMDirectory();
		Analyzer analyzer = new StandardAnalyzer();
		IndexReader indexReader = DirectoryReader.open(directory);
		IndexSearcher indexSearcher = new IndexSearcher(indexReader);
		QueryParser queryParser = new QueryParser("content", analyzer);
		Query query = queryParser.parse("alpha beta");
		TopDocs topDocs = indexSearcher.search(query, 100);
	}
	
	public static void sort() throws IOException{
		StandardAnalyzer analyzer = new StandardAnalyzer();
		Directory directory = new RAMDirectory();
		IndexWriterConfig config = new IndexWriterConfig(Version.LATEST, analyzer);
		IndexWriter indexWriter = new IndexWriter(directory, config);
		Document doc = new Document();
		StringField stringField = new StringField("name", "", Field.Store.YES);
		String[] contents = {"foxtrot", "echo", "delta", "charlie", "bravo", "alpha"};
		for (String content : contents) {
			stringField.setStringValue(content);
			doc.removeField("name");
			doc.add(stringField);
			indexWriter.addDocument(doc);
		}
		indexWriter.commit();
		IndexReader indexReader = DirectoryReader.open(directory);
		IndexSearcher indexSearcher = new IndexSearcher(indexReader);
		WildcardQuery query = new WildcardQuery(new Term("name","*"));
		SortField sortField = new SortField("name", SortField.Type.STRING);
		Sort sort = new Sort(sortField);
		TopDocs topDocs = indexSearcher.search(query, null, 100, sort);
		for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
			doc = indexReader.document(scoreDoc.doc);
			System.out.println(scoreDoc.score + ": " + doc.getField("name").stringValue());
		}
		indexWriter.close();
	}
}
