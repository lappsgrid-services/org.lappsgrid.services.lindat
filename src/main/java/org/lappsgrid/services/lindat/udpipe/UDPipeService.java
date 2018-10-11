package org.lappsgrid.services.lindat.udpipe;

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
import org.lappsgrid.services.lindat.net.UDPipeParams;
import org.lappsgrid.services.lindat.udpipe.connl.Document;
import org.lappsgrid.services.lindat.udpipe.connl.Parser;
import org.lappsgrid.services.lindat.udpipe.connl.Sentence;
import org.lappsgrid.services.lindat.udpipe.connl.Token;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.lappsgrid.discriminator.Discriminators.*;

/**
 *
 */
public class UDPipeService implements WebService
{
	public static final class Parameters {
		public static final String TOOLS = "tools";
		public static final String MODEL = "model";

		public static final class Tools {
			public static final String TOKENIZER = "tokenizer";
			public static final String TAGGER = "tagger";
			public static final String PARSER = "parser";
		}
	}

	public static final String UDPIPE_URL = "http://lindat.mff.cuni.cz/services/udpipe/api/process";

	// TODO replace with a Discriminator.Uri when it becomes available.
//	public static final String TYPE = "http://vocab.lappsgrid.org/ns/media/connl-u";
	public static final String LICENSE = Uri.LICENSE + "#mozilla=2";
	public static final String UTF8 = StandardCharsets.UTF_8.name();

	private String metadata;

	public UDPipeService()
	{

	}

	public String getMetadata() {
		if (metadata == null) {
			initializeMetadata();
		}
		return metadata;
	}

	public String execute(final String input) {
		Data inputData = Serializer.parse(input, Data.class);
		String discriminator = inputData.getDiscriminator();
		String text = null;
		if (Uri.ERROR.equals(discriminator)) {
			return input;
		}
		if (Uri.TEXT.equals(discriminator)) {
			text = inputData.getPayload().toString();
		}
		else if (Uri.LIF.equals(discriminator)) {
			Container container = new Container((Map) inputData.getPayload());
			text = container.getText();
		}
		else {
			return new Data(Uri.ERROR, "Invalid discriminator type: " + discriminator).asPrettyJson();
		}
		String response = null;
		Object tools = inputData.getParameter(Parameters.TOOLS);
		Object model = inputData.getParameter(Parameters.MODEL);

		boolean tagger = false;
		boolean parser = false;
		String lang = "en";
		if (tools != null) {
			switch (tools.toString()) {
				case Parameters.Tools.TOKENIZER:
					// Default, tokenizer only.
					break;
				case Parameters.Tools.TAGGER:
					tagger = true;
					break;
				case Parameters.Tools.PARSER:
					tagger = true;
					parser = true;
					break;
			}
		}
		if (model != null) {
			lang = model.toString();
		}

		QueryParams params = new UDPipeParams(tagger, parser, lang);
		try
		{
			response = HTTP.post(text, params);
		}
		catch (IOException e)
		{
			StringWriter writer = new StringWriter();
			PrintWriter out = new PrintWriter(writer);
			e.printStackTrace(out);
			return new Data(Uri.ERROR, writer.toString()).asPrettyJson();
		}

		Map<String,Object> map = Serializer.parse(response, HashMap.class);
		String connl = map.get("result").toString();
//		System.out.println(connl);
		Container container = null;
		try
		{
			container = convert(connl, tagger);
		}
		catch (LifException | IOException e)
		{
			StringWriter writer = new StringWriter();
			PrintWriter printWriter = new PrintWriter(writer);
			e.printStackTrace(printWriter);
			return new Data(Uri.ERROR, writer.toString()).asPrettyJson();
		}
		container.setText(text);
		return new Data(Uri.LIF, container).asJson();
	}

	protected Container convert(String connl, boolean tagged) throws LifException, IOException
	{
		Parser parser = new Parser();
		Container container = parser.parse(connl);
		List<View> views = container.findViewsThatContain(Uri.TOKEN);
		if (views.size() == 0) {
			throw new LifException("No token view found.");
		}
		if (tagged)
		{
			View tokenView = views.get(0);
			tokenView.addContains(Uri.POS, this.getClass().getName(), "udpipe");
			tokenView.addContains(Uri.LEMMA, this.getClass().getName(), "udpipe");
		}
		return container;
//		Document doc = parser.parse(connl);

		/*
		Container container = new Container();
		View sentenceView = container.newView();
		sentenceView.addContains(Uri.SENTENCE, this.getClass().getName(), "udpipe");
		View tokenView = container.newView();
		tokenView.addContains(Uri.TOKEN, this.getClass().getName(), "udpipe");
		if (tagged)
		{
			tokenView.addContains(Uri.POS, this.getClass().getName(), "udpipe");
			tokenView.addContains(Uri.LEMMA, this.getClass().getName(), "udpipe");
		}
		List<Sentence> sentences = doc.getSentences();
		for (Sentence s : sentences) {
			int sid = s.getId();
			sentenceView.newAnnotation("s" + sid, Uri.SENTENCE, s.getStart(), s.getEnd());
			List<Token> tokens = s.getTokens();
			for (Token token : tokens) {
				String tid = "tok-" + sid + "-" + token.getId();
				Annotation annotation = tokenView.newAnnotation(tid, Uri.TOKEN, token.getStart(), token.getEnd());
				token.copyToAnnotation(annotation);
			}
		}
		return container;
		*/
	}

	protected synchronized void initializeMetadata() {
		if (metadata != null) {
			return;
		}

		ServiceMetadata md = new ServiceMetadataBuilder()
				.name(this.getClass().getName())
				.description("UDPipe service provided by Clarin Lindat.")
				.version(Version.getVersion())
				.vendor("http://lappsgrid.org")
				.license(LICENSE)
				.allow(Uri.NO_COMMERCIAL)
				.requireFormats(Uri.TEXT, Uri.LIF)
				.requireEncoding(UTF8)
				.produceFormat(Uri.LIF)
				.produceEncoding(UTF8)
				.produces(Uri.TOKEN, Uri.POS, Uri.LEMMA)
				.build();

		metadata = new Data(Uri.META, md).asPrettyJson();
	}
}
