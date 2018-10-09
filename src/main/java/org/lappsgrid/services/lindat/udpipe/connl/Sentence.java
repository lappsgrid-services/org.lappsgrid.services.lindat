package org.lappsgrid.services.lindat.udpipe.connl;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 *
 */
public class Sentence
{
	private int id;
	private List<Token> tokens;

	public Sentence() {
		this(-1);
	}

	public Sentence(int id)
	{
		this.id = id;
		tokens = new ArrayList<>();
	}

	public void setId(int id) {
		this.id = id;
	}
	public int getId() { return id; }

	public int size() {
		return tokens.size();
	}

	public void add(Token token) {
		tokens.add(token);
	}

	public long getStart() {
		if (tokens.isEmpty()) {
			return -1;
		}
		Token first = tokens.get(0);
		return first.getStart();
	}

	public long getEnd() {
		if (tokens.isEmpty()) {
			return -1;
		}
		int last = tokens.size() - 1;
		Token word = tokens.get(last);
		return word.getEnd();

	}

	public List<Token> getTokens() {
		return tokens;
	}

	public Stream<Token> tokens() {
		return tokens.stream();
	}
}
