/*******************************************************************************
 * Copyright 2014 Celartem, Inc., dba LizardTech
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package com.lizardtech.expresszip.model;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import com.lizardtech.expresszip.vaadin.ExpressZipApplication;

public class MailServices {

	public MailServices(Session mailSession) {
		_session = mailSession;
	}

	public boolean isValidEmailConfig() {
		try {
			Transport transport = _session.getTransport();
			transport.connect();
			transport.close();
		} catch (MessagingException e) {
			if (ExpressZipApplication.logger != null)
				ExpressZipApplication.logger.error("mail configuration error: " + e.getMessage());
			return false;
		}
		return true;
	}

	public void sendEmail(String toAddress, String fromAddress, String subjectLine, String msgBody, String mimeType) {
		MimeMessage msg = new MimeMessage(_session);
		try {
			msg.setFrom(new InternetAddress(fromAddress));
			msg.addRecipient(Message.RecipientType.TO, new InternetAddress(toAddress));
			msg.setSubject(subjectLine);
			msg.setContent(msgBody, mimeType);
			Transport.send(msg);
		} catch (MessagingException e) {
			if (ExpressZipApplication.logger != null)
				ExpressZipApplication.logger.error("failed sending message to " + toAddress + " due to: " + e.getMessage());
		}
	}

	private Session _session;
}
