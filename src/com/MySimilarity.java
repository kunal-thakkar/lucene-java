package com;

import java.io.IOException;

import org.apache.lucene.index.AtomicReaderContext;
import org.apache.lucene.index.FieldInvertState;
import org.apache.lucene.index.NumericDocValues;
import org.apache.lucene.search.CollectionStatistics;
import org.apache.lucene.search.TermStatistics;
import org.apache.lucene.search.similarities.Similarity;
import org.apache.lucene.util.BytesRef;

public class MySimilarity extends Similarity {
	
	private Similarity similarity = null;
	
	public MySimilarity(Similarity similarity) {
		this.similarity = similarity;
	}

	@Override
	public long computeNorm(FieldInvertState state) {
		return this.similarity.computeNorm(state);
	}

	@Override
	public Similarity.SimWeight computeWeight(float queryBoost, CollectionStatistics collectionStatistics, TermStatistics... termStatistics){
		return this.similarity.computeWeight(queryBoost, collectionStatistics, termStatistics);
	}
	
	@Override
	public Similarity.SimScorer simScorer(SimWeight simWeight, AtomicReaderContext atomicReaderContext) throws IOException {
		final Similarity.SimScorer scorer = this.similarity.simScorer(simWeight, atomicReaderContext);
		final NumericDocValues values = atomicReaderContext.reader().getNumericDocValues("ranking");
		return new SimScorer() {
			@Override
			public float score(int i, float v) {
				return values.get(i) * scorer.score(i, v);
			}
			
			@Override
			public float computeSlopFactor(int i) {
				return scorer.computeSlopFactor(i);
			}
			
			@Override
			public float computePayloadFactor(int i, int i1, int i2, BytesRef byteRef) {
				return scorer.computePayloadFactor(i, i1, i2, byteRef);
			}
		};
	}

}
