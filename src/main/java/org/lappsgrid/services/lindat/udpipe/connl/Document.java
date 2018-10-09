package org.lappsgrid.services.lindat.udpipe.connl;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 *
 */
public class Document
{
	private List<Sentence> sentences;

	public Document()
	{
		sentences = new ArrayList<>();
	}

	public void  add(Sentence sentence) {
		sentences.add(sentence);
	}

	public int size() {
		return sentences.size();
	}

	public List<Sentence> getSentences()
	{
		return sentences;
	}
	public Stream<Sentence> sentences() { return sentences.stream(); }

	public Sentence getSentence(int index) {
//		if (index < 0 || index >= sentences.size()) {
//			throw new IndexOutOfBoundsException();
//		}
		return sentences.get(index);
	}
}
