package com.xion.payload;

import com.fasterxml.jackson.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "code", "status", "message", "errors" })
public class Error {

	public Error(Integer code, String status, String message, List<Error_> errors) {
		super();
		this.code = code;
		this.status = status;
		this.message = message;
		this.errors = errors;
	}

	@JsonProperty("code")
	private Integer code;
	@JsonProperty("status")
	private String status;
	@JsonProperty("message")
	private String message;
	@JsonProperty("errors")
	private List<Error_> errors = null;
	@JsonIgnore
	private Map<String, Object> additionalProperties = new HashMap<String, Object>();

	@JsonProperty("code")
	public Integer getCode() {
		return code;
	}

	@JsonProperty("code")
	public void setCode(Integer code) {
		this.code = code;
	}

	@JsonProperty("status")
	public String getStatus() {
		return status;
	}

	@JsonProperty("status")
	public void setStatus(String status) {
		this.status = status;
	}

	@JsonProperty("message")
	public String getMessage() {
		return message;
	}

	@JsonProperty("message")
	public void setMessage(String message) {
		this.message = message;
	}

	@JsonProperty("errors")
	public List<Error_> getErrors() {
		return errors;
	}

	@JsonProperty("errors")
	public void setErrors(List<Error_> errors) {
		this.errors = errors;
	}

	@JsonAnyGetter
	public Map<String, Object> getAdditionalProperties() {
		return this.additionalProperties;
	}

	@JsonAnySetter
	public void setAdditionalProperty(String name, Object value) {
		this.additionalProperties.put(name, value);
	}

	@Override
	public String toString() {
		return "Error [code=" + code + ", status=" + status + ", message=" + message + ", errors=" + errors
				+ ", additionalProperties=" + additionalProperties + "]";
	}

}
