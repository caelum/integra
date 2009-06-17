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
import java.util.List;
import java.util.Map;

import br.com.caelum.integracao.server.Phase;
import br.com.caelum.integracao.server.dao.Database;
import br.com.caelum.integracao.server.plugin.Parameter;
import br.com.caelum.integracao.server.plugin.ParameterType;
import br.com.caelum.integracao.server.plugin.PluginException;
import br.com.caelum.integracao.server.plugin.PluginInformation;
import br.com.caelum.integracao.server.template.FreemarkerTextTemplate;
import br.com.caelum.integracao.server.template.TextTemplate;

/**
 * Copies artifacts from the client machine to the server.
 * 
 * @author guilherme silveira
 */
public class SendMailInformation implements PluginInformation {

	public List<Parameter> getParameters() {
		Parameter content = new Parameter("content", "Build ${build.buildCount} has finished with success: ${build.successSoFar}", ParameterType.TEXTAREA);
		Parameter subject = new Parameter("subject", "Build ${build.buildCount}");
		return Arrays.asList(new Parameter("host", "localhost"), new Parameter("recipients", "myself@localhost"),
				new Parameter("from.name", "myself"), new Parameter("from.mail", "myself@localhost"), subject, content);
	}

	public boolean after(Phase phase) {
		return false;
	}

	public SendMail getPlugin(Database database, Map<String, String> parameters) throws PluginException {
		String value = parameters.get("recipients");
		String[] recipients = value == null ? new String[0] : value.split(",");
		try {
			TextTemplate subject = new FreemarkerTextTemplate(parameters.get("subject"));
			TextTemplate content = new FreemarkerTextTemplate(parameters.get("content"));
			return new SendMail(parameters.get("host"), recipients, parameters.get("from.name"), parameters
					.get("from.mail"), subject, content);
		} catch (IOException e) {
			throw new PluginException("Unable to send an email.", e);
		}
	}

	public String getName() {
		return "Send mail";
	}

	public boolean appliesForAPhase() {
		return true;
	}

	public boolean appliesForAProject() {
		return true;
	}

}
