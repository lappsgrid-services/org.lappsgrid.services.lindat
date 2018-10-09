package org.lappsgrid.services.lindat.net;

import org.lappsgrid.services.lindat.api.QueryParams;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.stream.Collectors;

/**
 *
 */
public class HTTP
{
	public static final String URL = "http://lindat.mff.cuni.cz/services/udpipe/api/process";

	private HTTP()
	{

	}

	public static String post(String data) throws IOException
	{
		return post(data, new UDPipeParams());
	}

	public static String post(String url, String data) throws IOException
	{
		return post(url, data, new UDPipeParams());
	}

	public static String post(String data, QueryParams params) throws IOException
	{
		return post(URL, data, params);
	}

	public static String post(String path, String data, QueryParams params) throws IOException
	{
		URL url = new URL(path);
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		connection.setRequestMethod("POST");
		connection.setDoOutput(true);

		String query = params.toString() + URLEncoder.encode(data, "UTF-8");
		OutputStream out = connection.getOutputStream();
		out.write(query.getBytes());
		out.close();

		InputStream in = connection.getInputStream();
		BufferedReader reader = new BufferedReader(new InputStreamReader(in));
		return reader.lines().collect(Collectors.joining("\n"));
	}

	/**
	 *
	 */
}
