package org.lappsgrid.services.lindat.udpipe.connl;

import org.lappsgrid.serialization.LifException;
import org.lappsgrid.serialization.lif.Annotation;
import org.lappsgrid.serialization.lif.Container;
import org.lappsgrid.serialization.lif.View;
import org.lappsgrid.vocabulary.Features;

import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static org.lappsgrid.discriminator.Discriminators.*;

/**
 *
 */
public class Parser
{
	// Until a proper discriminator is introduced.
	private static final String MULTI_TERM_TOKEN = "http://vocab.lappsgrid.org/ns/syntax/mwt";

	private BufferedReader bufferedReader;
	private View sentences;
	private View tokens;
	private Annotation sentence;

	private boolean saveSentenceStartOffset = false;
	private long lastTokenEndOffset = -1;

	public Parser()
	{
		sentence = null;
	}

	public Container parse(File file) throws IOException, LifException
	{
		return this.parse(new FileInputStream(file));
	}

	public Container parse(URL url) throws IOException, LifException
	{
		return this.parse(url.openStream());
	}

	public Container parse(String connl) throws IOException, LifException
	{
		return parse(new StringReader(connl));
	}

	public Container parse(InputStream input) throws IOException, LifException
	{
		return parse(new InputStreamReader(input));
	}

	public Container parse(Reader reader) throws IOException, LifException
	{
		if (reader instanceof BufferedReader) {
			bufferedReader = (BufferedReader) reader;
		}
		else {
			bufferedReader = new BufferedReader(reader);
		}

		Container container = new Container();
		sentences = container.newView();
		sentences.addContains(Uri.SENTENCE, this.getClass().getName(), "udpipe");
		tokens = container.newView();
		tokens.addContains(Uri.TOKEN, this.getClass().getName(), "udpipe");

		String line = bufferedReader.readLine();
		while (line != null) {
			if (line.startsWith("#")) {
				processComment(line);
			}
			else if (line.length() == 0) {
				if (sentence != null) {
					sentence.setEnd(lastTokenEndOffset);
					sentence = null;
				}
			}
			else {
				process(line);
			}
			line = bufferedReader.readLine();
		}
		return container;
	}

	private void process(String line) throws IOException
	{
//		System.out.println("processing " + line);
		Token token = new Token(line);
		Annotation a = tokens.newAnnotation("tok" + token.getId(), Uri.TOKEN);
		a.setStart(token.getStart());
		a.setEnd(token.getEnd());
		token.copyToAnnotation(a);
		if (saveSentenceStartOffset) {
			sentence.setStart(a.getStart());
			saveSentenceStartOffset = false;
		}
		lastTokenEndOffset = a.getEnd();
		if (token.getId().contains("-")) {
			String[] parts = token.getId().split("-");
			if (parts.length != 2) {
				throw new IOException("Invalid multi-word token ID: " + token.getId());
			}
			List<String> targets = new ArrayList<>();
			a.addFeature("targets", targets);
			boolean inMWE = true;
			while (inMWE) {
				String mwe = bufferedReader.readLine();
				Token mweToken = new Token(mwe);
				if (parts[1].equals(mweToken.getId())) {
					// this is the last token in the MWE.
					inMWE = false;
				}
				Annotation mweAnnotation = tokens.newAnnotation("mwt-" + mweToken.getId(), Uri.TOKEN);
				mweToken.copyToAnnotation(mweAnnotation);
				targets.add(mweAnnotation.getId());
				List<String> parent = new ArrayList<>();
				parent.add(a.getId());
				mweAnnotation.addFeature(Features.Token.TARGETS, parent);
				// TODO we need a proper discriminator here.
				mweAnnotation.addFeature(Features.Token.TYPE, MULTI_TERM_TOKEN);
			}
		}
	}

	private void processComment(String line) {
		if (line.contains("sent_id")) {
			// Every sentence in a well formed CoNNL-U document should have
			// the sentence id specified in a comment.
			String[] parts = line.split("=");
			if (parts.length == 2) {
				int id = Integer.parseInt(parts[1].trim());
				sentence = sentences.newAnnotation("s" + id, Uri.SENTENCE);
				saveSentenceStartOffset = true;
			}
		}
	}

}
