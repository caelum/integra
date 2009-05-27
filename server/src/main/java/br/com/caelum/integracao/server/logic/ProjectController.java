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
package br.com.caelum.integracao.server.logic;

import java.lang.reflect.InvocationTargetException;
import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.com.caelum.integracao.server.Clients;
import br.com.caelum.integracao.server.Project;
import br.com.caelum.integracao.server.Projects;
import br.com.caelum.integracao.server.jobs.Job;
import br.com.caelum.integracao.server.jobs.Jobs;
import br.com.caelum.vraptor.Get;
import br.com.caelum.vraptor.Path;
import br.com.caelum.vraptor.Resource;
import br.com.caelum.vraptor.Result;
import br.com.caelum.vraptor.Validator;
import br.com.caelum.vraptor.validator.ValidationMessage;

@Resource
public class ProjectController {
	
	private final Logger logger = LoggerFactory.getLogger(ProjectController.class);

	private final Clients clients;
	private final Projects projects;
	private final Validator validator;
	private final Jobs jobs;
	private final Result result;

	public ProjectController(Clients clients, Projects projects, Validator validator, Jobs jobs, Result result) {
		this.clients = clients;
		this.projects = projects;
		this.validator = validator;
		this.jobs = jobs;
		this.result = result;
	}
	
	public Collection<Project> list() {
		return this.projects.all();
	}

	public void run(Project project) throws IllegalArgumentException, SecurityException, InstantiationException,
			IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		final Project found = projects.get(project.getName());
		validator.onError().goTo(ProjectController.class).list();
		if(found==null) {
			validator.add(new ValidationMessage("", "project_not_found"));
		}
		validator.validate();
		final Job job = new Job("Building project " + found.getName());
		jobs.add(job);
		Runnable execution = new Runnable() {
			public void run() {
				try {
					logger.debug("Starting building project " + found.getName());
					found.execute(clients);
					jobs.remove(job);
				} catch (Exception e) {
					// TODO save pu
					e.printStackTrace();
				}
			}
		};
		Thread thread = new Thread(execution);
		job.attach(thread);
		thread.start();
	}
	
	@Get
	@Path("/project/{project.name}/{revision}")
	public void show(Project project, String revision) {
		logger.debug("Displaying build result for " + project.getName() + "@"+ revision);
		project = projects.get(project.getName());
		result.include("project", project);
		result.include("build", project.getBuild(revision));
	}

}
