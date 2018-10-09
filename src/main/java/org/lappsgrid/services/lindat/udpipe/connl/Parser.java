package org.lappsgrid.services.lindat.udpipe.connl;

import java.io.*;
import java.net.URL;

/**
 *
 */
public class Parser
{
	private Document document;
	private Sentence current;
	private int id;

	public Parser()
	{
		id = -1;
		document = new Document();
		current = null;
	}

	public Document parse(File file) throws FileNotFoundException
	{
		return this.parse(new FileInputStream(file));
	}

	public Document parse(URL url) throws IOException
	{
		return this.parse(url.openStream());
	}

	public Document parse(String connl) {
		return parse(new StringReader(connl));
	}

	public Document parse(InputStream input) {
		BufferedReader reader = new BufferedReader(new InputStreamReader(input));
		reader.lines().forEach(line -> process(line));
		return document;
	}

	public Document parse(Reader reader) {
		BufferedReader bufferedReader;
		if (reader instanceof BufferedReader) {
			bufferedReader = (BufferedReader) reader;
		}
		else {
			bufferedReader = new BufferedReader(reader);
		}
		bufferedReader.lines().forEach(line -> process(line));
		return document;
	}

	private void process(String line) {
		if (line.startsWith("#")) {
			processComment(line);
		}
		else if (line.length() == 0) {
			// A blank line indicates the end of a sentence.
			if (current != null) {
//				document.add(current);
				current = null;
			}
		}
		else {
			current.add(new Token(line));
		}
	}

	private void processComment(String line) {
		if (line.contains("sent_id")) {
			// Every sentence in a well formed CoNNL-U document should have
			// the sentence id specified in a comment.
			String[] parts = line.split("=");
			if (parts.length == 2) {
				int id = Integer.parseInt(parts[1].trim());
				current = new Sentence(id);
				document.add(current);
			}
		}
	}

//	private void processLine(String line) {
//
//	}


}
