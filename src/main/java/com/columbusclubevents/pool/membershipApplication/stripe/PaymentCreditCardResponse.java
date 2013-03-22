package com.columbusclubevents.pool.membershipApplication.stripe;

public class PaymentCreditCardResponse {

	private Boolean success;
	private String successId;
	private String type;
	private String message;
	private String code;
	private String param;
	
	public Boolean getSuccess() {
		return success;
	}
	public void setSuccess(Boolean success) {
		this.success = success;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}
	public String getParam() {
		return param;
	}
	public void setParam(String param) {
		this.param = param;
	}
	public String getSuccessId() {
		return successId;
	}
	public void setSuccessId(String successId) {
		this.successId = successId;
	}
	@Override
   public String toString() {
	   StringBuilder builder = new StringBuilder();
	   builder.append("PaymentCreditCardResponse [success=").append(success)
	         .append(", successId=").append(successId).append(", type=")
	         .append(type).append(", message=").append(message)
	         .append(", code=").append(code).append(", param=").append(param)
	         .append("]");
	   return builder.toString();
   }
}
