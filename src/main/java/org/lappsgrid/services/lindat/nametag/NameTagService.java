package org.lappsgrid.services.lindat.nametag;

import static org.lappsgrid.discriminator.Discriminators.*;

import org.lappsgrid.api.WebService;
import org.lappsgrid.metadata.ServiceMetadata;
import org.lappsgrid.metadata.ServiceMetadataBuilder;
import org.lappsgrid.serialization.Data;
import org.lappsgrid.serialization.LifException;
import org.lappsgrid.serialization.Serializer;
import org.lappsgrid.serialization.lif.Annotation;
import org.lappsgrid.serialization.lif.Container;
import org.lappsgrid.serialization.lif.View;
import org.lappsgrid.services.lindat.Version;
import org.lappsgrid.services.lindat.api.QueryParams;
import org.lappsgrid.services.lindat.net.HTTP;
import org.lappsgrid.vocabulary.Features;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 */
public class NameTagService implements WebService
{
	public static final String ENGLISH = "english-conll-140408";
	public static final String CZECH = "czech-cnec2.0-140304";

	public static final String LICENSE = Uri.LICENSE + "#mozilla=2";
	public static final String UTF8 = StandardCharsets.UTF_8.name();
	private static final String SERVICE_URL = "https://lindat.mff.cuni.cz/services/nametag/api/recognize";

	private String metadata = null;
	private int count = 0;

	public String getMetadata() {
		if (metadata == null) {
			initialize();
		}
		return metadata;
	}
	public String execute(String json) {
		Data data = Serializer.parse(json);
		String discriminator = data.getDiscriminator();
		if (Uri.ERROR.equals(discriminator)) {
			return json;
		}
		if (!Uri.LIF.equals(discriminator)) {
			return error("Invalid input document type. Expected LIF but found " + discriminator);
		}
		Container container = new Container((Map) data.getPayload());
		List<View> views = container.findViewsThatContain(Uri.TOKEN);
		if (views.size() == 0) {
			return error("Input document does not contain any token annotationss.");
		}

		int last = views.size() - 1;
		View tokenView = views.get(last);

		views = container.findViewsThatContain(Uri.SENTENCE);
		if (views.size() == 0) {
			return error("Input document does not contain any sentence annotations.");
		}

		Object object = data.getParameter("model");
		String model = null;
		if (object == null) {
			model = ENGLISH;
		}
		else {
			model = object.toString();
			if ("english".equals(model)) {
				model = ENGLISH;
			}
			else if ("czech".equals(model)) {
				model = CZECH;
			}
		}
		String text = container.getText();
		View sentenceView = views.get(views.size() - 1);
		List<Annotation> sentences = sentenceView.findByAtType(Uri.SENTENCE);
		List<Annotation> tokens = tokenView.findByAtType(Uri.TOKEN);
		List<Annotation> tokenCopy = new ArrayList<>();

		StringWriter swriter = new StringWriter();
		PrintWriter out = new PrintWriter(swriter);
		Annotation space = new Annotation("dummy", Uri.TOKEN, -1, -1);
		for (Annotation sentence : sentences) {
			for (Annotation token : getTokensInSentence(sentence, tokens)) {
				int start = token.getStart().intValue();
				int end = token.getEnd().intValue();
				String word = text.substring(start, end);
				out.println(word);
				tokenCopy.add(token);
			}
			tokenCopy.add(space);
			out.println();
		}
		out.println();
		String verticalText = swriter.toString();


		QueryParams params = new NameTagParams(model);
		String response = null;
		try
		{
			response = HTTP.post(SERVICE_URL, verticalText, params);
		}
		catch (IOException e)
		{
			return error(e);
		}

		Map<String,Object> map = Serializer.parse(response, HashMap.class);
		String responseText = map.get("result").toString();
		try
		{
			generateView(container, tokenCopy, responseText);
		}
		catch (LifException e)
		{
			return error(e);
		}
		return new Data(Uri.LIF, container).asPrettyJson();
	}

	protected String error(Exception e) {
		StringWriter writer = new StringWriter();
		PrintWriter printer = new PrintWriter(writer);
		e.printStackTrace(printer);
		return new Data(Uri.ERROR, writer.toString()).asPrettyJson();
	}

	protected String error(String message) {
		return new Data(Uri.ERROR, message).asPrettyJson();
	}

	protected void generateView(Container container, List<Annotation> tokens, String input) throws LifException
	{
		if (input.trim().length() == 0) {
			return;
		}
		count = 0;
		View view = container.newView();
		view.addContains(Uri.NE, this.getClass().getName(), "lindat:nametag");
		BufferedReader reader = new BufferedReader(new StringReader(input));
		reader.lines().forEach(s -> process(s, tokens, view));
	}

	protected void process(String line, List<Annotation> tokens, View view)
	{
		String[] parts = line.split("\t");
		if (parts.length == 3) {
			String type = getType(parts[1]);
			Annotation ne = view.newAnnotation("ne-" + (count++), Uri.NE);
			ne.addFeature(Features.NamedEntity.CATEGORY, type);
			ne.addFeature("string", parts[2]);
			addOffsets(ne, tokens, parts[0]);
		}
	}

	protected void addOffsets(Annotation ne, List<Annotation> tokens, String input) {
		String[] parts = input.split(",");
		int startToken = Integer.parseInt(parts[0]) - 1;
		int last = parts.length - 1;
		int endToken = Integer.parseInt(parts[last]) - 1;

		long start = tokens.get(startToken).getStart();
		ne.setStart(start);

		long end = tokens.get(endToken).getEnd();
		ne.setEnd(end);
	}

	protected String getType(final String input) {
		if ("PER".equals(input)) {
			return "PERSON";
		}
		if ("LOC".equals(input)) {
			return "LOCATION";
		}
		if ("ORG".equals(input)) {
			return "ORGANIZATION";
		}
		return input;
	}
	protected List<Annotation> getTokensInSentence(Annotation sentence, List<Annotation> tokens) {
		List<Annotation> result = new ArrayList<>();
		long start = sentence.getStart();
		long end = sentence.getEnd();
		for (Annotation token : tokens) {
			long tStart = token.getStart();
			if (tStart >= start) {
				long tEnd = token.getEnd();
				if (tEnd <= end) {
					result.add(token);
				}
			}
		}
		return result;
	}

	private synchronized void initialize() {
		if (metadata != null) {
			return;
		}
		ServiceMetadata md = new ServiceMetadataBuilder()
				.name(this.getClass().getName())
				.description("NameTag service from Clarin Lindat")
				.version(Version.getVersion())
				.vendor("http://lappsgrid.org")
				.license(LICENSE)
				.allow(Uri.NO_COMMERCIAL)
				.requireFormat(Uri.LIF)
				.requires(Uri.TOKEN, Uri.SENTENCE)
				.requireEncoding(UTF8)
				.produceFormat(Uri.LIF)
				.produceEncoding(UTF8)
				.produces(Uri.NE)
				.build();
		metadata = new Data(Uri.META, md).asPrettyJson();
	}

	public static void main(String[] args) {
		NameTagService service = new NameTagService();
		System.out.println(service.execute(null));
	}
}
