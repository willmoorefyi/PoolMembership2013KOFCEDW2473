package com.columbusclubevents.pool.membershipApplication.email;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.util.ByteArrayDataSource;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions;
import com.google.appengine.api.taskqueue.TaskOptions.Method;

@Service("mailSender")
public class MailSender {
	private static Logger log = LoggerFactory.getLogger(MailSender.class);

	private Resource memberConfirmationFileText;
	private Resource nonmemberConfirmationFileText;
	private Resource memberConfirmationFileHtm;
	private Resource nonmemberConfirmationFileHtm;
	private Resource parkingAttachment;
	private Resource rulesAttachment;
	private String from;
	private String subject;

	public void googleEnqueueMessage(Long memberId) {
		log.debug("Enqueueing task for member '{}'", memberId);
		Queue queue = QueueFactory.getDefaultQueue();
		queue.add(TaskOptions.Builder.withUrl(
		      "/sendemail/" + memberId.toString() + "/sendAcceptance.htm")
		      .method(Method.POST));
	}

	public void sendAcceptanceMessage(String to, String memberId, String paymentId, boolean member)
	      throws MessagingException, IOException {
		log.debug("Sending acceptance letter to: {}", to);
		Properties props = new Properties();
		Session session = Session.getDefaultInstance(props, null);
		
		Message msg = new MimeMessage(session);
		Multipart mp = new MimeMultipart();
		
		msg.setFrom(new InternetAddress(from));
		msg.addRecipient(Message.RecipientType.TO, new InternetAddress(to));
		msg.setSubject(subject); //String.format(subject, memberId, StringUtils.isEmpty(paymentId) ? "N/A" : paymentId) 
		
      MimeBodyPart htmlPart = new MimeBodyPart();
		MimeBodyPart textPart = new MimeBodyPart();
		StringWriter htmlWriter = new StringWriter();
		StringWriter textWriter = new StringWriter();
		if (member) {
			IOUtils.copy(memberConfirmationFileHtm.getInputStream(), htmlWriter, "UTF-8");
			IOUtils.copy(memberConfirmationFileText.getInputStream(), textWriter, "UTF-8");
		} else {
			IOUtils.copy(nonmemberConfirmationFileHtm.getInputStream(), htmlWriter, "UTF-8");
			IOUtils.copy(nonmemberConfirmationFileText.getInputStream(), textWriter, "UTF-8");
		}
		//msg.setText(textWriter.toString());
		htmlPart.setContent(htmlWriter.toString(), "text/html; charset=utf-8");
		textPart.setText(textWriter.toString(), "utf-8");
		mp.addBodyPart(textPart);
		mp.addBodyPart(htmlPart);
		
		MimeBodyPart attachmentParking = new MimeBodyPart();
		attachmentParking.setFileName("Columbus Club Parking Map.pdf");
		byte[] attachmentParkingData = IOUtils.toByteArray(parkingAttachment.getInputStream());
		DataSource parkingSrc = new ByteArrayDataSource(attachmentParkingData, "application/pdf");
		attachmentParking.setDataHandler(new DataHandler(parkingSrc));
		mp.addBodyPart(attachmentParking);
		
		MimeBodyPart attachmentRules = new MimeBodyPart();
		attachmentRules.setFileName("Pool Rules 2014.pdf");
		byte[] attachmentRulesData = IOUtils.toByteArray(rulesAttachment.getInputStream());
		DataSource rulesSrc = new ByteArrayDataSource(attachmentRulesData, "application/pdf");
		attachmentRules.setDataHandler(new DataHandler(rulesSrc));
		mp.addBodyPart(attachmentRules);
		
		msg.setContent(mp);
		log.debug("sending message: {}", msg.getContent());
		Transport.send(msg);

	}

	public String getFrom() {
		return from;
	}

	public void setFrom(String from) {
		this.from = from;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public Resource getParkingAttachment() {
		return parkingAttachment;
	}

	public void setParkingAttachment(Resource parkingAttachment) {
		this.parkingAttachment = parkingAttachment;
	}

	public Resource getRulesAttachment() {
		return rulesAttachment;
	}

	public void setRulesAttachment(Resource rulesAttachment) {
		this.rulesAttachment = rulesAttachment;
	}

	public Resource getMemberConfirmationFileText() {
		return memberConfirmationFileText;
	}

	public void setMemberConfirmationFileText(Resource memberConfirmationFileText) {
		this.memberConfirmationFileText = memberConfirmationFileText;
	}

	public Resource getNonmemberConfirmationFileText() {
		return nonmemberConfirmationFileText;
	}

	public void setNonmemberConfirmationFileText(
	      Resource nonmemberConfirmationFileText) {
		this.nonmemberConfirmationFileText = nonmemberConfirmationFileText;
	}

	public Resource getMemberConfirmationFileHtm() {
		return memberConfirmationFileHtm;
	}

	public void setMemberConfirmationFileHtm(Resource memberConfirmationFileHtm) {
		this.memberConfirmationFileHtm = memberConfirmationFileHtm;
	}

	public Resource getNonmemberConfirmationFileHtm() {
		return nonmemberConfirmationFileHtm;
	}

	public void setNonmemberConfirmationFileHtm(
	      Resource nonmemberConfirmationFileHtm) {
		this.nonmemberConfirmationFileHtm = nonmemberConfirmationFileHtm;
	}
}
