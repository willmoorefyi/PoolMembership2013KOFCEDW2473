package com.columbusclubevents.pool.membershipApplication.email;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.activation.URLDataSource;
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

	// Variables for confirmation email
	private Resource memberConfirmationFileText;
	private Resource nonmemberConfirmationFileText;
	private Resource memberConfirmationFileHtm;
	private Resource nonmemberConfirmationFileHtm;
	private Resource parkingAttachment;
	private Resource rulesAttachment;
	private String from;
	private String welcomeSubject;

	// Variables for additional payment email
	private String paymentSubject;
	private String urlRoot;
	private Resource memberAdditionalPaymentEmailText;
	private Resource memberAdditionalPaymentEmailHtm;
	private Resource memberAdditionalPaymentEmailLogo;

	public void googleEnqueueMessage(Long id, String endpoint) {
		log.debug("Enqueueing task for ID '{}'", id);
		Queue queue = QueueFactory.getDefaultQueue();
		queue.add(TaskOptions.Builder.withUrl(
		      "/sendemail/" + id.toString() + "/" + endpoint)
		      .method(Method.POST));
	}

	public void sendAcceptanceMessage(String to, boolean member)
	      throws MessagingException, IOException {
		log.debug("Sending acceptance letter to: {}", to);
		Properties props = new Properties();
		Session session = Session.getDefaultInstance(props, null);
		
		Message msg = new MimeMessage(session);
		Multipart mp = new MimeMultipart();
		
		msg.setFrom(new InternetAddress(from));
		msg.addRecipient(Message.RecipientType.TO, new InternetAddress(to));
		msg.setSubject(welcomeSubject);
		
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

	public void sendPaymentEmail(String to, String relativeUrl) throws MessagingException, IOException {
		log.debug("Sending payment letter to '{}' with URL '{}'", to, relativeUrl);
		String fullUrl = urlRoot + relativeUrl;
		log.debug("Computed full URL as: {}", fullUrl);
		Properties props = new Properties();
		Session session = Session.getDefaultInstance(props, null);

		Message msg = new MimeMessage(session);
		Multipart mp = new MimeMultipart();

		msg.setFrom(new InternetAddress(from));
		msg.addRecipient(Message.RecipientType.TO, new InternetAddress(to));
		msg.setSubject(paymentSubject);

		MimeBodyPart htmlPart = new MimeBodyPart();
		MimeBodyPart textPart = new MimeBodyPart();
		MimeBodyPart logoPart = new MimeBodyPart();
		StringWriter htmlWriter = new StringWriter();
		StringWriter textWriter = new StringWriter();

		IOUtils.copy(memberAdditionalPaymentEmailHtm.getInputStream(), htmlWriter, "UTF-8");
		IOUtils.copy(memberAdditionalPaymentEmailText.getInputStream(), textWriter, "UTF-8");

		DataSource dataSource = new URLDataSource(memberAdditionalPaymentEmailLogo.getURL());
		logoPart.setDataHandler(new DataHandler(dataSource));
		logoPart.setFileName(memberAdditionalPaymentEmailLogo.getFilename());

		//neither of these commands do anything because appengine is garbage that you shouldn't ever use
		logoPart.setContentID("<columbusclubarlington_logo>");
		logoPart.setHeader("Content-ID", "<columbusclubarlington_logo>");
		logoPart.setDisposition(MimeBodyPart.INLINE);

		htmlPart.setContent(htmlWriter.toString().replaceAll("\\{url\\}", fullUrl), "text/html; charset=utf-8");
		textPart.setText(textWriter.toString().replaceAll("\\{url\\}", fullUrl), "utf-8");
		mp.addBodyPart(textPart);
		mp.addBodyPart(htmlPart);
		mp.addBodyPart(logoPart);

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

	public String getWelcomeSubject() {
		return welcomeSubject;
	}

	public void setWelcomeSubject(String welcomeSubject) {
		this.welcomeSubject = welcomeSubject;
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

	public String getPaymentSubject() {
		return paymentSubject;
	}

	public void setPaymentSubject(String paymentSubject) {
		this.paymentSubject = paymentSubject;
	}

	public String getUrlRoot() {
		return urlRoot;
	}

	public void setUrlRoot(String urlRoot) {
		this.urlRoot = urlRoot;
	}

	public Resource getMemberAdditionalPaymentEmailText() {
		return memberAdditionalPaymentEmailText;
	}

	public void setMemberAdditionalPaymentEmailText(Resource memberAdditionalPaymentEmailText) {
		this.memberAdditionalPaymentEmailText = memberAdditionalPaymentEmailText;
	}

	public Resource getMemberAdditionalPaymentEmailHtm() {
		return memberAdditionalPaymentEmailHtm;
	}

	public void setMemberAdditionalPaymentEmailHtm(Resource memberAdditionalPaymentEmailHtm) {
		this.memberAdditionalPaymentEmailHtm = memberAdditionalPaymentEmailHtm;
	}

	public Resource getMemberAdditionalPaymentEmailLogo() {
		return memberAdditionalPaymentEmailLogo;
	}

	public void setMemberAdditionalPaymentEmailLogo(Resource memberAdditionalPaymentEmailLogo) {
		this.memberAdditionalPaymentEmailLogo = memberAdditionalPaymentEmailLogo;
	}
}
