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
package br.com.caelum.integracao.server.action;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.UnknownHostException;
import java.util.List;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;

import br.com.caelum.integracao.server.Build;
import br.com.caelum.integracao.server.Client;
import br.com.caelum.integracao.server.Command;
import br.com.caelum.integracao.server.Phase;
import br.com.caelum.integracao.server.Project;

public class Dispatcher {

	private final PrintWriter log;

	private final Client client;

	private final String myUrl;

	public Dispatcher(Client client, File logFile, String myUrl) throws UnknownHostException, IOException {
		this.client = client;
		this.myUrl = myUrl;
		logFile.getParentFile().mkdirs();
		this.log = new PrintWriter(new FileWriter(logFile), true);
	}

	public Dispatcher register(Project project) {
		HttpClient client = new HttpClient();
		PostMethod post = new PostMethod(this.client.getBaseUri() + "/project/register");
		post.addParameter("project.name", project.getName());
		post.addParameter("project.uri", project.getUri());
		try {
			int result = client.executeMethod(post);
			if (result != 200) {
				throw new RuntimeException("Unable to continue with result " + result);
			}
		} catch (Exception e) {
			throw new RuntimeException("Unable to continue. ", e);
		}
		return this;
	}

	public Dispatcher execute(Build build, Phase phase, int commandCount, List<Command> commands)
			throws IOException {
		HttpClient client = new HttpClient();
		PostMethod post = new PostMethod(this.client.getBaseUri() + "/job/execute");
		post.addParameter("revision", build.getRevision());
		post.addParameter("project.name", build.getProject().getName());
		post.addParameter("clientId", "" + this.client.getId());
		post.addParameter("resultUri", "http://" + myUrl + "/integracao/finish/project/" + build.getProject().getName() + "/" + build.getBuildCount() + "/"
				+ phase.getPosition() + "/" + commandCount);
		for (int i = 0; i < commands.size(); i++) {
			post.addParameter("command[" + i + "]", commands.get(i).getValue());
		}
		try {
			int result = client.executeMethod(post);
			if (result != 200) {
				throw new RuntimeException("Unable to continue with result " + result);
			}
		} catch (Exception e) {
			throw new RuntimeException("Unable to continue. ", e);
		}
		return this;
	}

	public void close() throws IOException {
		this.log.close();
	}

}
