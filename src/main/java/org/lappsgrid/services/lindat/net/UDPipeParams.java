package org.lappsgrid.services.lindat.net;

import org.lappsgrid.services.lindat.api.QueryParams;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 *
 */
public class UDPipeParams implements QueryParams
{
//	public enum Language {
//		EN("english"), CS("czech"), DE("german"), FR("french"), ES("spanish");
//
//		private String string;
//		private Language(String string) {
//			this.string = string;
//		}
//		public String toString() { return string; }
//	};

	private boolean tagger;
	private boolean parser;
	private String lang;

//	public UDPipeParams()
//	{
//		this(true, true, Language.EN);
//	}

//	public UDPipeParams(Language lang) {
//		this(true, true, lang);
//	}

	public UDPipeParams(String lang) {
		this(true, true, lang);
	}

	public UDPipeParams(boolean tagger, boolean parser, String lang) {
		this.tagger = tagger;
		this.parser = parser;
		this.lang = lang.toLowerCase(); //Language.valueOf(lang.toUpperCase());
	}

//	public UDPipeParams(boolean tagger, boolean parser, Language lang) {
//		this.tagger = tagger;
//		this.parser = parser;
//		this.lang = lang;
//	}

	public String toString() {
		StringBuilder buffer = new StringBuilder();
		buffer.append("tokenizer=ranges");
		if (tagger) {
			buffer.append("&tagger=");
		}
		if (parser) {
			buffer.append("&parser=");
		}
		buffer.append("&model=");
		buffer.append(lang);
		buffer.append("&data=");
		return buffer.toString();

//		List<String> options = new ArrayList<>();
//		options.add("tokenizer=ranges");
//		if (tagger) {
//			options.add("tagger=");
//		}
//		if (parser) {
//			options.add("parser=");
//		}
//		options.add("model=" + lang.toString());
//		options.add("data=");
//		return options.stream().collect(Collectors.joining("&"));
	}
}
