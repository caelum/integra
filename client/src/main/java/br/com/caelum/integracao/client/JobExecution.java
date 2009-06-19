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
package br.com.caelum.integracao.client;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.com.caelum.integracao.client.project.Project;
import br.com.caelum.integracao.client.project.ProjectRunResult;
import br.com.caelum.vraptor.interceptor.multipart.UploadedFile;

/**
 * Executes a job in this client.
 * 
 * @author guilherme silveira
 */
public class JobExecution {

	private final Logger logger = LoggerFactory.getLogger(JobExecution.class);
	private final Project project;
	private final Command start;
	private final Command stop;
	private final List<String> directoryToCopy;
	private final String revision;
	private final Settings settings;
	private final Server server;
	private final UploadedFile content;
	private final List<String> artifactsToPush;
	private final UploadedFile artifacts;

	public JobExecution(Project project, List<String> startCommand, List<String> stopCommand, 
			String revision, List<String> directoryToCopy, Settings settings, Server server, UploadedFile content, List<String> artifactsToPush, UploadedFile artifacts) {
		this.project = project;
		this.settings = settings;
		this.server = server;
		this.content = content;
		this.artifactsToPush = artifactsToPush;
		this.artifacts = artifacts;
		this.start = new Command(startCommand);
		this.stop = new Command(stopCommand);
		this.revision = revision;
		this.directoryToCopy = directoryToCopy;
	}

	void executeBuildFor(StringWriter output) {

		ProjectRunResult unzipResult = null;
		ProjectRunResult startResult = null;
		ProjectRunResult stopResult = null;

		try {

			unzipResult = project.unzip(settings.getBaseDir(), content, output, new ProjectRunResult("",0), true);
			unzipResult = project.unzip(settings.getBaseDir(), artifacts, output, unzipResult, false);
			if (!unzipResult.failed()) {
				startResult = project.run(settings.getBaseDir(), start, output);
			}

		} catch (Exception e) {
			logger.debug("Something wrong happened during the checkout/build", e);
			StringWriter errorOutput = new StringWriter();
			e.printStackTrace(new PrintWriter(errorOutput, true));
			startResult = new ProjectRunResult(errorOutput.toString(), -1);
		} finally {
			try {
				if (stop.containsAnything()) {
					stopResult = project.run(settings.getBaseDir(), stop, new StringWriter());
				} else {
					stopResult = new ProjectRunResult("No command to run.", 0);
				}
			} catch (IOException e) {
				logger.error("Unable to stop command on server!", e);
			} finally {
				server.dispatch(project, unzipResult, startResult, stopResult, directoryToCopy, artifactsToPush);
			}
		}
	}

	public Project getProject() {
		return project;
	}

	public String getRevision() {
		return revision;
	}

	public void stop() {
		this.project.stop();
	}
}
