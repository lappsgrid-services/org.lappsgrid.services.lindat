package org.lappsgrid.services.lindat.udpipe.connl;

import org.lappsgrid.serialization.lif.Annotation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 */
public class Token
{
	private int id;
	private long start;
	private long end;
	private String word;
	private String lemma;
	private String upos;
	private String xpos;
	private Map<String,String> features;
	private String head;
	private String deprel;
//	private List<String> deps;
	private String deps;
	private String misc;

	public Token()
	{

	}

	public Token(String line) {
		System.out.println("Processing " + line);
		String[] parts = line.split("\t");
		id = Integer.parseInt(parts[0]);
//		id = parts[0];
		word = parts[1];
		lemma = parts[2];
		upos = parts[3];
		xpos = parts[4];
		processFeatures(parts[5]);
		head = parts[6];
		deprel = parts[7];
//		deps = new ArrayList();
//		deps.add(parts[8]);
		deps = parts[8];
		misc = processMisc(parts[9]);
	}

	public int getId() { return id; }
	public long getStart() { return  start; }
	public long getEnd() { return end; }

	public void copyToAnnotation(Annotation a) {
		addFeature(a, "word", word);
		addFeature(a, "lemma", lemma);
		addFeature(a, "pos", upos);
		addFeature(a, "xpos", xpos);
		features.forEach((k,v) -> addFeature(a, k,v) );
		addFeature(a,"head", head);
		addFeature(a,"deprel", deprel);

	}

	private void addFeature(Annotation a, String key, String value) {
		if (value.equals("_")) {
			return;
		}
		a.addFeature(key, value);
	}

	private void processFeatures(String input) {
		features = new HashMap<>();
		String[] parts = input.split("\\|");
		for (String part : parts) {
			String[] keyValue = part.split("=");
			if (keyValue.length == 2)
			{
				features.put(keyValue[0], keyValue[1]);
			}
			// TODO else log an error.
		}
	}

	private String processMisc(String input) {
		String[] parts = input.split("\\|");

		for (String part : parts) {
			String[] keyValue = part.split("=");
			if (keyValue.length == 2) {
				if ("TokenRange".equals(keyValue[0])) {
					String[] offsets = keyValue[1].split(":");
					if (offsets.length == 2) {
						start = Long.parseLong(offsets[0].trim());
						end = Long.parseLong(offsets[1].trim());
						return input;
					}
					// TODO else log an error
				}
				else {
					features.put(keyValue[0], keyValue[1]);
				}
			}
			// TODO else log an error
		}
		return input;
	}
}
