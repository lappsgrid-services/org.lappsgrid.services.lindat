package org.lappsgrid.services.lindat.udpipe;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.lappsgrid.api.WebService;
import org.lappsgrid.metadata.ServiceMetadata;
import org.lappsgrid.serialization.Data;
import org.lappsgrid.serialization.DataContainer;
import org.lappsgrid.serialization.Serializer;
import org.lappsgrid.serialization.lif.Annotation;
import org.lappsgrid.serialization.lif.Container;
import org.lappsgrid.serialization.lif.View;
import org.lappsgrid.services.lindat.Version;
import org.lappsgrid.vocabulary.Features;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;
import static org.lappsgrid.discriminator.Discriminators.*;

/**
 *
 */
public class ServiceTest
{
	String text = "Karen flew to New York. Nancy flew to Bloomington.";

	WebService service;

	@Before
	public void setup() {
		service = new UDPipeService();
	}

	@After
	public void teardown() {
		service = null;
	}

	@Test
	public void testMetadata() {
		String json = service.getMetadata();
		assertNotNull(json);
		Data data = Serializer.parse(json);
		assertEquals("Invalid discriminator", Uri.META, data.getDiscriminator());

		ServiceMetadata metadata = new ServiceMetadata((Map) data.getPayload());
		assertEquals(Version.getVersion(), metadata.getVersion());
		assertEquals("http://lappsgrid.org", metadata.getVendor());
		assertEquals(UDPipeService.LICENSE, metadata.getLicense());
		assertEquals(Uri.NO_COMMERCIAL, metadata.getAllow());

		List<String> formats = metadata.getProduces().getFormat();
		assertEquals("Wrong number of output formats", 1, formats.size());
		assertEquals(Uri.LIF, formats.get(0));

		formats = metadata.getRequires().getFormat();
		List<String> expected = Arrays.asList(Uri.LIF, Uri.TEXT);
		assertEquals("Wrong number of input formats", expected.size(), formats.size());
		assertTrue(formats.containsAll(expected));
	}

	@Test
	public void testText() {
		Data data = new Data(Uri.TEXT, text);
		String json = service.execute(data.asJson());
		validate(json);
	}

	@Test
	public void testLif() {
		Container container = new Container();
		container.setText(text);
		Data data = new Data(Uri.LIF, container);
		String json = service.execute(data.asJson());
		validate(json);
	}

	@Test
	public void testTokenizer() {
		Data data = new Data(Uri.TEXT, text);
		data.setParameter(UDPipeService.Parameters.TOOLS, "t");
		data.setParameter(UDPipeService.Parameters.MODEL, "en");
		String json = service.execute(data.asJson());
		assertNotNull(json);
		data = Serializer.parse(json, DataContainer.class);
		System.out.println(data.asPrettyJson());
		Container container = (Container) data.getPayload();
		assertEquals(2, container.getViews().size());
		List<View> views = container.findViewsThatContain(Uri.TOKEN);
		assertNotNull(views);
		assertEquals(1, views.size());
		views = container.findViewsThatContain(Uri.SENTENCE);
		assertNotNull(views);
		assertEquals(1, views.size());
		views = container.findViewsThatContain(Uri.LEMMA);
		assertEquals(0, views.size());
		views = container.findViewsThatContain(Uri.POS);
		assertEquals(0, views.size());
	}

	@Test
	public void testTokenizerTagger() {
		Data data = new Data(Uri.TEXT, text);
		data.setParameter(UDPipeService.Parameters.TOOLS, "tagger");
		data.setParameter(UDPipeService.Parameters.MODEL, "en");
		String json = service.execute(data.asJson());
		assertNotNull(json);
		data = Serializer.parse(json, DataContainer.class);
		System.out.println(data.asPrettyJson());
		Container container = (Container) data.getPayload();
		assertEquals(2, container.getViews().size());
		List<View> views = container.findViewsThatContain(Uri.TOKEN);
		assertNotNull(views);
		assertEquals(1, views.size());
		views = container.findViewsThatContain(Uri.SENTENCE);
		assertNotNull(views);
		assertEquals(1, views.size());
		views = container.findViewsThatContain(Uri.LEMMA);
		assertEquals(1, views.size());
		View view = views.get(0);
		for (Annotation a : view.getAnnotations()) {
			assertNotNull(a.getFeature(Features.Token.LEMMA));
		}
		views = container.findViewsThatContain(Uri.POS);
		assertEquals(1, views.size());
		view = views.get(0);
		for (Annotation a : view.getAnnotations()) {
			assertNotNull(a.getFeature(Features.Token.PART_OF_SPEECH));
		}
	}

	private void validate(String json) {
		assertNotNull(json);
		Data data = Serializer.parse(json);
		assertEquals(Uri.LIF, data.getDiscriminator());
		System.out.println(data.asPrettyJson());
	}
}
