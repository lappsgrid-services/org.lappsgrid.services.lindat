package org.lappsgrid.services.lindat.net;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 *
 */
public class QueryParamsTest
{
	@Test
	public void testDefaultValues() {
		UDPipeParams params = new UDPipeParams();
		String expected = "tokenizer=ranges&tagger=&parser=&model=english&data=";
		assertEquals(expected, params.toString());
	}

	@Test
	public void testEnglishString() {
		UDPipeParams params = new UDPipeParams("EN");
		String expected = "tokenizer=ranges&tagger=&parser=&model=english&data=";
		assertEquals(expected, params.toString());

		params = new UDPipeParams("en");
		assertEquals(expected, params.toString());
	}

	@Test
	public void testLanguageEnum() {
		UDPipeParams params = new UDPipeParams(UDPipeParams.Language.EN);
		String expected = "tokenizer=ranges&tagger=&parser=&model=english&data=";
		assertEquals(expected, params.toString());

		params = new UDPipeParams(UDPipeParams.Language.CS);
		expected = "tokenizer=ranges&tagger=&parser=&model=czech&data=";
		assertEquals(expected, params.toString());
	}

	@Test
	public void testCzechString() {
		UDPipeParams params = new UDPipeParams("CS");
		String expected = "tokenizer=ranges&tagger=&parser=&model=czech&data=";
		assertEquals(expected, params.toString());

		params = new UDPipeParams("cs");
		assertEquals(expected, params.toString());
	}

	@Test
	public void testTokenizer() {
		UDPipeParams params = new UDPipeParams(false, false, "en");
		String expected = "tokenizer=ranges&model=english&data=";
		assertEquals(expected, params.toString());

	}

	@Test
	public void testTokenizerTagger() {
		UDPipeParams params = new UDPipeParams(true, false, "en");
		String expected = "tokenizer=ranges&tagger=&model=english&data=";
		assertEquals(expected, params.toString());
	}
}
