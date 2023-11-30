package com.xion.payload.accounting;

//@JsonInclude(JsonInclude.Include.NON_NULL)
//@JsonPropertyOrder({ "name" })
public class Contact {

//	@JsonProperty("name")
	private String name;

//	@JsonProperty("name")
	public String getName() {
		return name;
	}

//	@JsonProperty("name")
	public void setName(String name) {
		this.name = name;
	}

}
