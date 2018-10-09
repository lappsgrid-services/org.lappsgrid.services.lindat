package org.lappsgrid.services.lindat.nametag;

import org.lappsgrid.services.lindat.api.QueryParams;

/**
 *
 */
public class NameTagParams implements QueryParams
{
	private String model;
	private String output;

	public NameTagParams()
	{
		this(NameTagService.ENGLISH, "vertical");
	}

	public NameTagParams(String model) {
		this(model, "vertical");
	}

	public NameTagParams(String model, String output) {
		this.model = model;
		this.output = output;
	}

	public String toString() {
		return String.format("model=%s&output=%s&input=vertical&data=", model, output);
	}

}
