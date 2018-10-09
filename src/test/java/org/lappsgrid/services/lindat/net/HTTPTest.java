package org.lappsgrid.services.lindat.net;

import org.junit.Test;
import org.lappsgrid.services.lindat.net.HTTP;
import org.lappsgrid.services.lindat.udpipe.UDPipeService;

import java.io.IOException;

/**
 *
 */
public class HTTPTest
{
	public HTTPTest()
	{

	}

	@Test
	public void testPost() throws IOException
	{
		String response = HTTP.post(UDPipeService.UDPIPE_URL, "Karen flew to New York. Nancy flew to Bloomington.");
		System.out.println(response);
	}
}
