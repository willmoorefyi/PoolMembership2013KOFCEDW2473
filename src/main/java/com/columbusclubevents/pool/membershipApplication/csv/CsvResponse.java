package com.columbusclubevents.pool.membershipApplication.csv;

import java.util.List;

import com.columbusclubevents.pool.membershipApplication.model.Member;

public class CsvResponse {
	private final String filename;
	private final List<Member> records;

	public CsvResponse(List<Member> records, String filename) {
		this.records = records;
		this.filename = filename;
	}

	public String getFilename() {
		return filename;
	}

	public List<Member> getRecords() {
		return records;
	}

}
