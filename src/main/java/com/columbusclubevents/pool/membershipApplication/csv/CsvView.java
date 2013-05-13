package com.columbusclubevents.pool.membershipApplication.csv;

import java.io.BufferedWriter;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.view.AbstractView;

import com.columbusclubevents.pool.membershipApplication.model.Member;

public class CsvView extends AbstractView {
	private static final Logger log = LoggerFactory.getLogger(CsvView.class);

	@Override @SuppressWarnings("unchecked")
	protected void renderMergedOutputModel(Map<String, Object> model, HttpServletRequest request, HttpServletResponse response)
	      throws Exception {
		log.debug("Rendering merged output");
		BufferedWriter writer = null;
		try {
			writer = new BufferedWriter(response.getWriter());
			response.setHeader("Content-Disposition", "attachment; filename=\"members.csv\"");
			List<Member> members = (List<Member>)model.get("members");
			for(Member member : members) {
				//log.debug("Writing member {}", member);
				writer.write(member.toCSVString());
				writer.newLine();
			}
		} finally {
			if (writer != null) {
				writer.flush();
				writer.close();
			}
		}
	}
}
