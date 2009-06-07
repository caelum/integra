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

import br.com.caelum.integracao.http.DefaultHttp;
import br.com.caelum.integracao.http.Method;
import br.com.caelum.integracao.server.Client;
import br.com.caelum.integracao.server.ExecuteCommandLine;
import br.com.caelum.integracao.server.Project;
import br.com.caelum.integracao.server.queue.Job;

/**
 * Dispatch commands to register a project or execute an specific command from a build an specific client.
 * 
 * @author guilherme silveira
 */
public class Dispatcher {

	private final PrintWriter log;

	private final Client client;

	private final String myself;

	public Dispatcher(Client client, File logFile, String myself) throws UnknownHostException, IOException {
		this.client = client;
		this.myself = myself;
		logFile.getParentFile().mkdirs();
		this.log = new PrintWriter(new FileWriter(logFile), true);
	}

	public Dispatcher register(Project project) {
		Method post = new DefaultHttp().post(this.client.getBaseUri() + "/project/register");
		post.with("project.name", project.getName());
		post.with("project.uri", project.getUri());
		post.with("project.scmType", project.getControlType().getName());
		try {
			post.send();
			int result = post.getResult();
			if (result != 200) {
				throw new RuntimeException("Unable to continue with result " + result + " and " + post.getContent());
			}
		} catch (IOException e) {
			throw new RuntimeException("Unable to continue. ", e);
		}
		return this;
	}

	public Dispatcher execute(ExecuteCommandLine command, Job job) throws IOException {
		Method post = new DefaultHttp().post(this.client.getBaseUri() + "/job/execute");
		post.with("jobId", ""+ job.getId());
		post.with("revision",  job.getBuild().getRevision().getName());
		post.with("project.name", job.getBuild().getProject().getName());
		post.with("resultUri", "http://" + myself + "/integracao/finish/job/" + job.getId());
		for (int i = 0; i < command.getStartCommands().size(); i++) {
			post.with("startCommand[" + i + "]", command.getStartCommands().get(i).getValue());
		}
		for (int i = 0; i < command.getStopCommands().size(); i++) {
			post.with("stopCommand[" + i + "]", command.getStopCommands().get(i).getValue());
		}
		try {
			post.send();
			int result = post.getResult();
			if (result != 200) {
				throw new RuntimeException("Unable to continue with result " + result + " with " + post.getContent());
			}
		} catch (IOException e) {
			throw new RuntimeException("Unable to run the job at the server.", e);
		}
		return this;
	}

	public void close() throws IOException {
		this.log.close();
	}

}
