/***
 * 
 * Copyright (c) 2009 Caelum - www.caelum.com.br/opensource All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer. 2. Redistributions in
 * binary form must reproduce the above copyright notice, this list of
 * conditions and the following disclaimer in the documentation and/or other
 * materials provided with the distribution. 3. Neither the name of the
 * copyright holders nor the names of its contributors may be used to endorse or
 * promote products derived from this software without specific prior written
 * permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package br.com.caelum.integracao.server.plugin.mail;

import java.util.Arrays;

import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.SimpleEmail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.com.caelum.integracao.server.Phase;
import br.com.caelum.integracao.server.plugin.Plugin;

/**
 * A plugin instance.
 * 
 * @author guilherme silveira
 */
public class SendMail implements Plugin {

	private final Logger logger = LoggerFactory.getLogger(SendMail.class);
	private final String fromMail;
	private final String fromName;
	private final String[] recipients;
	private final String host;

	public SendMail(String host, String[] recipients, String fromName, String fromMail) {
		this.host = host;
		this.recipients = recipients;
		this.fromName = fromName;
		this.fromMail = fromMail;
	}

	public boolean after(Phase phase) {
		logger.debug("Preparing to send mail to " + Arrays.toString(recipients));
		try {
			SimpleEmail email = new SimpleEmail();
			email.setHostName(host);
			for(String recipient : recipients) {
				email.addTo(recipient, recipient);
			}
			email.setFrom(fromMail, fromName);
			email.setSubject(phase.getProject().getName() + " built.");
			email.setMsg("Build completed.");
			email.send();
		} catch (EmailException e) {
			logger.debug("Unable to send mail", e);
			return false;
		}
		return false;
	}

}
