package com;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.lucene.index.AtomicReaderContext;
import org.apache.lucene.search.Collector;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Scorer;

public class MyCollector extends Collector {

	private int totalHits = 0;
	private int docBase;
	private Scorer scorer;
	private List<ScoreDoc> topDocs = new ArrayList();
	private ScoreDoc[] scoreDocs;
	
	public MyCollector() {

	}
	
	@Override
	public boolean acceptsDocsOutOfOrder() {
		return false;
	}

	@Override
	public void collect(int doc) throws IOException {
		float score = scorer.score();
		if (score > 0) {
			score += (1 / (doc + 1));
		}
		ScoreDoc scoreDoc =
		new ScoreDoc(doc + docBase, score);
		topDocs.add(scoreDoc);
		totalHits++;
	}

	@Override
	public void setNextReader(AtomicReaderContext context) throws IOException {
		this.docBase = context.docBase;
	}

	@Override
	public void setScorer(Scorer scorer) throws IOException {
		this.scorer = scorer;
	}

	public int getTotalHits() {
		return totalHits;
	}
	
	public ScoreDoc[] getScoreDocs() {
		if (scoreDocs != null) {
			return scoreDocs;
		}
		Collections.sort(topDocs, new Comparator<ScoreDoc>() {
			public int compare(ScoreDoc d1, ScoreDoc d2) {
				if (d1.score > d2.score) {
					return -1;
				} else if (d1.score == d2.score) {
					return 0;
				} else {
					return 1;
				}
			}
		});
		scoreDocs = topDocs.toArray(
		new ScoreDoc[topDocs.size()]);
		return scoreDocs;
	}
}
