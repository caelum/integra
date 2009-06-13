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
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.com.caelum.integracao.client.project.Project;
import br.com.caelum.integracao.client.project.Projects;
import br.com.caelum.integracao.http.DefaultHttp;
import br.com.caelum.vraptor.Get;
import br.com.caelum.vraptor.Post;
import br.com.caelum.vraptor.Resource;
import br.com.caelum.vraptor.Result;
import br.com.caelum.vraptor.ioc.ApplicationScoped;
import br.com.caelum.vraptor.view.Results;

/**
 * Resource controlling the current job.
 * 
 * @author guilherme silveira
 */
@ApplicationScoped
@Resource
public class JobController {

	private static final int GONE = 410;

	private static final int CONFLICT = 409;

	private final Logger logger = LoggerFactory.getLogger(JobController.class);

	private final Projects projects;

	private final CurrentJob job;

	private final Result result;

	private final HttpServletResponse response;

	private final Settings settings;

	public JobController(Projects projects, CurrentJob job, Result result, HttpServletResponse response,
			Settings settings) {
		this.projects = projects;
		this.job = job;
		this.result = result;
		this.response = response;
		this.settings = settings;
	}

	@Post
	public synchronized void execute(String jobId, Project project, String revision, List<String> startCommand,
			List<String> stopCommand, String resultUri, List<String> directoryToCopy) {
		Server server = new Server(resultUri, new DefaultHttp(), settings);
		JobExecution execution = new JobExecution(projects.get(project.getName()), startCommand, stopCommand, revision,
				directoryToCopy, settings, server);
		job.start(jobId, execution);
	}

	@Get
	@Post
	public void current() throws IOException {
		// TODO possible sync fail... time to render might already set the job
		// to null :(
		result.include("job", job);
		if (job.getProject() != null) {
			logger.debug("Displaying info on current job: " + job.getProject().getName());
		} else {
			response.sendError(GONE);
			result.use(Results.nothing());
		}
	}

	public void stop(String jobId) throws IOException {
		boolean succeeded = job.stop(jobId);
		if (!succeeded) {
			response.sendError(CONFLICT);
		}
		result.use(Results.nothing());
	}
}
