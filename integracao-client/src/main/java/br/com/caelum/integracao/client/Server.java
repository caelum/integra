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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringWriter;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.com.caelum.integracao.client.project.Project;
import br.com.caelum.integracao.client.project.ProjectRunResult;
import br.com.caelum.integracao.http.DefaultHttp;
import br.com.caelum.integracao.http.Http;
import br.com.caelum.integracao.http.Method;

public class Server {

	private final Logger logger = LoggerFactory.getLogger(Server.class);

	private final String resultUri;
	private final DefaultHttp http;
	private final Settings settings;

	public Server(String resultUri, DefaultHttp http, Settings settings) {
		this.resultUri = resultUri;
		this.http = http;
		this.settings = settings;
	}

	public void dispatch(Project project, ProjectRunResult unzipResult, ProjectRunResult startResult,
			ProjectRunResult stopResult, List<String> directoryToCopy, List<String> artifactsToPush) {
		logger.debug("Job " + project.getName() + " has finished");
		Http http = new DefaultHttp();
		logger.debug("Acessing uri " + resultUri + " to finish the job");
		Method post = http.post(resultUri);
		try {
			try {
				addTo(post, unzipResult, "checkout");
				addTo(post, startResult, "start");
				addTo(post, stopResult, "stop");
				zip("zipOutput", "content", project, directoryToCopy, post);
				zip("artifactsOutput", "artifacts", project, artifactsToPush, post);
				post.with("success", (failed(unzipResult) || failed(stopResult) || failed(startResult)) ? "false" : "true");
				post.send();
			} catch (Exception e) {
				logger.error("Was unable to notify the server of this request..."
						+ "maybe the server thinks im still busy.", e);
			}
			if (post.getResult() != 200) {
				logger.error(post.getContent());
				throw new RuntimeException("The server returned a problematic answer: " + post.getResult());
			}
		} catch (IOException e) {
			throw new RuntimeException("The server returned a problematic answer", e);
		} finally {
			post.close();
		}
	}

	private void zip(String logName, String contentName, Project project, List<String> resources, Method post) throws IOException,
			FileNotFoundException {
		StringWriter zipOutput = new StringWriter();
		if (resources == null || resources.isEmpty()) {
			zipOutput.write("Nothing to zip to client.");
		} else {
			File zip = new CopyFiles(resources, settings, project, zipOutput).zipThemAll();
			if (zip != null) {
				logger.debug("After zipping, resulted in =" + zip.getAbsolutePath());
				post.with(contentName, zip);
			}
		}
		post.with(logName, zipOutput.getBuffer().toString());
	}

	private boolean failed(ProjectRunResult result) {
		return result == null || result.failed();
	}

	private void addTo(Method post, ProjectRunResult result, String prefix) {
		if (result != null) {
			logger.debug(prefix + "Result success=" + result.getResult());
			post.with(prefix + "Result", result.getContent());
		} else {
			post.with(prefix + "Result", "unable to read result");
		}
	}

}
