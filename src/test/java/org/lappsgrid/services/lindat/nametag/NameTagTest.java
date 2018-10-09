package org.lappsgrid.services.lindat.nametag;

import org.junit.*;
import org.lappsgrid.api.WebService;
import org.lappsgrid.metadata.IOSpecification;
import org.lappsgrid.metadata.ServiceMetadata;
import org.lappsgrid.serialization.Data;
import org.lappsgrid.serialization.Serializer;
import org.lappsgrid.serialization.lif.Annotation;
import org.lappsgrid.serialization.lif.Container;
import org.lappsgrid.serialization.lif.View;
import org.lappsgrid.services.lindat.Version;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.Assert.*;
import static org.lappsgrid.discriminator.Discriminators.*;

/**
 *
 */
public class NameTagTest
{
	WebService service;

	@Before
	public void setup() {
		service = new NameTagService();
	}

	@After
	public void teardown() {
		service = null;
	}

	@Test
	public void testMetatdata() {
		String json = service.getMetadata();
		assertNotNull(json);

		Data data = Serializer.parse(json);
		assertEquals(Uri.META, data.getDiscriminator());

		ServiceMetadata metadata = new ServiceMetadata((Map) data.getPayload());
		assertEquals(Version.getVersion(), metadata.getVersion());
		assertEquals(Uri.NO_COMMERCIAL, metadata.getAllow());
		assertEquals(NameTagService.class.getName(), metadata.getName());

		IOSpecification requires = metadata.getRequires();
		assertEquals(StandardCharsets.UTF_8.name(), requires.getEncoding());
		List<String> formats = requires.getFormat();
		assertEquals(1, formats.size());
		assertEquals(Uri.LIF, formats.get(0));
		assertTrue(containsAll(requires.getAnnotations(), Uri.TOKEN, Uri.SENTENCE));

		IOSpecification produces = metadata.getProduces();
		formats = produces.getFormat();
		assertEquals(1, formats.size());
		assertEquals(Uri.LIF, formats.get(0));
		assertEquals(StandardCharsets.UTF_8.name(), produces.getEncoding());
	}

	private boolean containsAll(List<String> list, String ...items) {
		return list.containsAll(Arrays.asList(items));
	}

	@Test
	public void testExecute() {
		InputStream stream = this.getClass().getResourceAsStream("/input.lif");
		assertNotNull("Test data not found.", stream);
		InputStreamReader reader = new InputStreamReader(stream);
		BufferedReader in = new BufferedReader(reader);
		String lif = in.lines().collect(Collectors.joining("\n"));
		String json = service.execute(lif);
		Data data = Serializer.parse(json);
		System.out.println(data.asPrettyJson());
		assertEquals(Uri.LIF, data.getDiscriminator());
		Container container = new Container((Map) data.getPayload());
		assertEquals(3, container.getViews().size());

		String text = container.getText();
		List<View> views = container.findViewsThatContain(Uri.NE);
		assertEquals(1, views.size());
		View view = views.get(0);
		List<Annotation> entities = view.findByAtType(Uri.NE);
		assertEquals(4, entities.size());
		for (Annotation ne : entities) {
			String actual = ne.getFeature("string");
			assertNotNull(actual);
			String expected = substring(text, ne);
			assertEquals(expected, actual);
		}
		System.out.println("NameTagTest.testExecute");
	}

	private String substring(String text, Annotation a) {
		int start = a.getStart().intValue();
		int end = a.getEnd().intValue();
		return text.substring(start, end);
	}
}
