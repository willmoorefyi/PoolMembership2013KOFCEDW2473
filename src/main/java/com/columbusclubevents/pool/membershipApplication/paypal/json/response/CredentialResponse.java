package com.columbusclubevents.pool.membershipApplication.paypal.json.response;

/**
 * Credentials response from Paypal OAuth REST API service
 * @author wmoore
 *
 */
public class CredentialResponse {

	private String scope;
	private String access_token;
	private String token_type;
	private String app_id;
	private Integer expires_in;
	
	public String getScope() {
		return scope;
	}
	public void setScope(String scope) {
		this.scope = scope;
	}
	public String getAccess_token() {
		return access_token;
	}
	public void setAccess_token(String access_token) {
		this.access_token = access_token;
	}
	public String getToken_type() {
		return token_type;
	}
	public void setToken_type(String token_type) {
		this.token_type = token_type;
	}
	public String getApp_id() {
		return app_id;
	}
	public void setApp_id(String app_id) {
		this.app_id = app_id;
	}
	public Integer getExpires_in() {
		return expires_in;
	}
	public void setExpires_in(Integer expires_in) {
		this.expires_in = expires_in;
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("CredentialResponse [scope=").append(scope)
				.append(", access_token=").append(access_token)
				.append(", token_type=").append(token_type).append(", app_id=")
				.append(app_id).append(", expires_in=").append(expires_in)
				.append("]");
		return builder.toString();
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((access_token == null) ? 0 : access_token.hashCode());
		result = prime * result + ((app_id == null) ? 0 : app_id.hashCode());
		result = prime * result
				+ ((expires_in == null) ? 0 : expires_in.hashCode());
		result = prime * result + ((scope == null) ? 0 : scope.hashCode());
		result = prime * result
				+ ((token_type == null) ? 0 : token_type.hashCode());
		return result;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		CredentialResponse other = (CredentialResponse) obj;
		if (access_token == null) {
			if (other.access_token != null)
				return false;
		} else if (!access_token.equals(other.access_token))
			return false;
		if (app_id == null) {
			if (other.app_id != null)
				return false;
		} else if (!app_id.equals(other.app_id))
			return false;
		if (expires_in == null) {
			if (other.expires_in != null)
				return false;
		} else if (!expires_in.equals(other.expires_in))
			return false;
		if (scope == null) {
			if (other.scope != null)
				return false;
		} else if (!scope.equals(other.scope))
			return false;
		if (token_type == null) {
			if (other.token_type != null)
				return false;
		} else if (!token_type.equals(other.token_type))
			return false;
		return true;
	}
}
