package com.columbusclubevents.pool.membershipApplication.csv;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.AbstractHttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;

import au.com.bytecode.opencsv.CSVWriter;

import com.columbusclubevents.pool.membershipApplication.model.Member;

public class CsvMessageConverter extends AbstractHttpMessageConverter<CsvResponse> {
	private static final Logger log = LoggerFactory.getLogger(CsvMessageConverter.class);
	public static final MediaType MEDIA_TYPE = new MediaType("text", "csv", Charset.forName("utf-8"));

	public CsvMessageConverter() {
		super(MEDIA_TYPE);
	}

	protected boolean supports(Class<?> clazz) {
		return CsvResponse.class.equals(clazz);
	}

	@SuppressWarnings("all")
	protected void writeInternal(CsvResponse response, HttpOutputMessage output) throws IOException, HttpMessageNotWritableException {
		log.info("Writing out CSV file");
		output.getHeaders().setContentType(MEDIA_TYPE);
		output.getHeaders().set("Content-Disposition", "attachment; filename=\"" + response.getFilename() + "\"");
		OutputStream out = output.getBody();
		CSVWriter writer = new CSVWriter(new OutputStreamWriter(out), '\u0009');
		List<Member> allRecords = response.getRecords();
		for (Member member : allRecords) {
			log.debug("Writing row for member {}", member);
			writer.writeNext(new String[] { member.toString() });
		}
	}

	@Override
	protected CsvResponse readInternal(Class<? extends CsvResponse> clazz, HttpInputMessage inputMessage) throws IOException,
	      HttpMessageNotReadableException {
		// TODO Auto-generated method stub
		return null;
	}

}
