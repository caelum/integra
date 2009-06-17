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

import java.io.IOException;
import java.util.Arrays;

import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.SimpleEmail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.com.caelum.integracao.server.Build;
import br.com.caelum.integracao.server.Phase;
import br.com.caelum.integracao.server.plugin.Plugin;
import br.com.caelum.integracao.server.template.TextTemplate;
import freemarker.template.TemplateException;

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
	private final TextTemplate subject;
	private final TextTemplate content;

	public SendMail(String host, String[] recipients, String fromName, String fromMail, TextTemplate subject, TextTemplate content) {
		this.host = host;
		this.recipients = recipients;
		this.fromName = fromName;
		this.fromMail = fromMail;
		this.subject = subject;
		this.content = content;
	}

	public boolean after(Build build, Phase phase) {
		return sendMail(build,phase);
	}

	private boolean sendMail(Build build, Phase phase) {
		subject.with("build", build).with("phase", phase);
		content.with("build", build).with("phase", phase);
		logger.debug("Preparing to send mail to " + Arrays.toString(recipients));
		try {
			SimpleEmail email = new SimpleEmail();
			email.setHostName(host);
			for(String recipient : recipients) {
				email.addTo(recipient, recipient);
			}
			email.setFrom(fromMail, fromName);
			email.setSubject(subject.value());
			email.setMsg(content.value());
			email.send();
			return true;
		} catch (EmailException e) {
			logger.debug("Unable to send mail", e);
			return false;
		} catch (TemplateException e) {
			logger.debug("Unable to send mail", e);
			return false;
		} catch (IOException e) {
			logger.debug("Unable to send mail", e);
			return false;
		}
	}

	public boolean before(Build build) {
		return true;
	}

	public void after(Build build) {
		sendMail(build, null);
	}

}
