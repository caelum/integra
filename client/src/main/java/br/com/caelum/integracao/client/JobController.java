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

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.com.caelum.integracao.client.project.Project;
import br.com.caelum.integracao.client.project.Projects;
import br.com.caelum.integracao.http.Http;
import br.com.caelum.integracao.http.Method;
import br.com.caelum.vraptor.Resource;
import br.com.caelum.vraptor.ioc.ApplicationScoped;

@ApplicationScoped
@Resource
public class JobController {

	private final Logger logger = LoggerFactory.getLogger(JobController.class);

	private final EntryPoint point;
	private final Projects projects;

	private Project currentJob;

	public JobController(EntryPoint point, Projects projects) {
		this.point = point;
		this.projects = projects;
		this.currentJob = null;
	}

	public synchronized void execute(Project project, final String revision, final List<String> command,
			final String resultUri, final String clientId) {
		if (this.currentJob != null) {
			throw new RuntimeException("Cannot take another job as im currently processing " + currentJob.getName());
		}
		this.currentJob = projects.get(project.getName());
		if (this.currentJob == null) {
			throw new RuntimeException("Unable to find project " + project.getName());
		}
		new Thread(new Runnable() {
			public void run() {
				final StringBuffer result = new StringBuffer();
				final AtomicBoolean success = new AtomicBoolean(false);
				try {
					result.append(currentJob.run(point.getBaseDir(), revision, command));
					success.set(true);
				} finally {
					if (currentJob != null) {
						try {
							logger.debug("Job " + currentJob.getName() + " has finished");
							Http http = new Http();
							Method put = http.post(resultUri);
							put.with("result", result.toString()).with("project.name", currentJob.getName());
							put.with("revision", revision);
							put.with("success", "" + success.get()).with("client.id", clientId).send();
							if (put.getResult() != 200) {
								throw new RuntimeException("The server returned a problematic answer: "
										+ put.getResult());
							}
						} catch (Exception e) {
							logger.error("Was unable to notify the server of this request.", e);
						} finally {
							currentJob = null;
						}
					}
				}
			}
		}).start();
	}
}
