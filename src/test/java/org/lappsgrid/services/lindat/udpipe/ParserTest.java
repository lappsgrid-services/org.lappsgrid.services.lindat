package org.lappsgrid.services.lindat.udpipe;

import org.junit.Test;
import org.lappsgrid.serialization.LifException;
import org.lappsgrid.serialization.Serializer;
import org.lappsgrid.serialization.lif.Container;
import org.lappsgrid.services.lindat.udpipe.connl.Document;
import org.lappsgrid.services.lindat.udpipe.connl.Parser;
import org.lappsgrid.services.lindat.udpipe.connl.Sentence;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

/**
 *
 */
public class ParserTest
{
	@Test
	public void testParse() throws IOException
	{
		URL url = this.getClass().getResource("/input.conll");
		assertNotNull(url);

		Parser parser = new Parser();
		Document doc = parser.parse(url);
		List<Sentence> sentences = doc.getSentences();
		assertEquals(2, sentences.size());

		Sentence s = sentences.get(0);
		assertEquals(6, s.getTokens().size());
		assertEquals(0, s.getStart());
		assertEquals(23, s.getEnd());

		s = sentences.get(1);
		assertEquals(5, s.getTokens().size());
		assertEquals(24, s.getStart());
		assertEquals(50, s.getEnd());
	}

}
