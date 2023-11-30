package com.xion.payload.accounting;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class DocumentDTO {

	@JsonProperty("error")
	private Error error;

	@JsonProperty("documents")
	private List<DocumentRequest> documentRequest;

	public DocumentDTO() {
		super();
		// TODO Auto-generated constructor stub
	}

	public DocumentDTO(Error error) {
		super();
		this.error = error;
	}

	public Error getError() {
		return error;
	}

	public void setError(Error error) {
		this.error = error;
	}

	public List<DocumentRequest> getDocumentRequest() {
		return documentRequest;
	}

	public void setDocumentRequest(List<DocumentRequest> documentRequest) {
		this.documentRequest = documentRequest;
	}

	@Override
	public String toString() {
		return "DocumentDTO [error=" + error + ", documentRequest=" + documentRequest + "]";
	}

}
