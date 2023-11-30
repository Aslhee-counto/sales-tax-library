package com.xion.payload.accounting;

import com.fasterxml.jackson.annotation.*;

import java.util.HashMap;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "pre_tax_amount", "tax_amount", "tax_type", "description", "account", "total_amount" })
public class LineItem {

	@JsonProperty("pre_tax_amount")
	private Double preTaxAmount;
	@JsonProperty("tax_amount")
	private Double taxAmount;
	@JsonProperty("tax_type")
	private String taxType;
	@JsonProperty("description")
	private String description;
	@JsonProperty("account")
	private String account;
	@JsonProperty("total_amount")
	private String totalAmount;
	@JsonProperty("account_code")
	private String accountCode;
	@JsonIgnore
	private Map<String, Object> additionalProperties = new HashMap<String, Object>();

	@JsonProperty("pre_tax_amount")
	public Double getPreTaxAmount() {
		return preTaxAmount;
	}

	@JsonProperty("pre_tax_amount")
	public void setPreTaxAmount(Double preTaxAmount) {
		this.preTaxAmount = preTaxAmount;
	}

	@JsonProperty("tax_amount")
	public Double getTaxAmount() {
		return taxAmount;
	}

	@JsonProperty("tax_amount")
	public void setTaxAmount(Double taxAmount) {
		this.taxAmount = taxAmount;
	}

	@JsonProperty("tax_type")
	public String getTaxType() {
		return taxType;
	}

	@JsonProperty("tax_type")
	public void setTaxType(String taxType) {
		this.taxType = taxType;
	}

	@JsonProperty("description")
	public String getDescription() {
		return description;
	}

	@JsonProperty("description")
	public void setDescription(String description) {
		this.description = description;
	}

	@JsonProperty("account")
	public String getAccount() {
		return account;
	}

	@JsonProperty("account")
	public void setAccount(String account) {
		this.account = account;
	}

	@JsonProperty("total_amount")
	public String getTotalAmount() {
		return totalAmount;
	}

	@JsonProperty("total_amount")
	public void setTotalAmount(String totalAmount) {
		this.totalAmount = totalAmount;
	}

	@JsonAnyGetter
	public Map<String, Object> getAdditionalProperties() {
		return this.additionalProperties;
	}

	@JsonAnySetter
	public void setAdditionalProperty(String name, Object value) {
		this.additionalProperties.put(name, value);
	}

	public String getAccountCode() {
		return accountCode;
	}

	public void setAccountCode(String accountCode) {
		this.accountCode = accountCode;
	}

}
