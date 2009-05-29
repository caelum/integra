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

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.com.caelum.integracao.server.Client;
import br.com.caelum.integracao.server.Clients;
import br.com.caelum.integracao.server.ExecuteCommandLine;
import br.com.caelum.integracao.server.Phase;
import br.com.caelum.integracao.server.Project;
import br.com.caelum.integracao.server.Projects;
import br.com.caelum.integracao.server.dao.Database;
import br.com.caelum.integracao.server.dao.DatabaseFactory;
import br.com.caelum.integracao.server.jobs.Job;
import br.com.caelum.integracao.server.jobs.Jobs;
import br.com.caelum.integracao.server.project.Build;
import br.com.caelum.vraptor.Delete;
import br.com.caelum.vraptor.Get;
import br.com.caelum.vraptor.Path;
import br.com.caelum.vraptor.Post;
import br.com.caelum.vraptor.Resource;
import br.com.caelum.vraptor.Result;
import br.com.caelum.vraptor.Validator;
import br.com.caelum.vraptor.validator.ValidationMessage;
import br.com.caelum.vraptor.view.Results;

@Resource
public class ProjectController {

	private final Logger logger = LoggerFactory.getLogger(ProjectController.class);

	private final Clients clients;
	private final Projects projects;
	private final Validator validator;
	private final Jobs jobs;
	private final Result result;

	private final DatabaseFactory factory;

	public ProjectController(Clients clients, Projects projects, Validator validator, Jobs jobs, Result result, DatabaseFactory factory) {
		this.clients = clients;
		this.projects = projects;
		this.validator = validator;
		this.jobs = jobs;
		this.result = result;
		this.factory = factory;
	}

	public void addAll(String myUrl) {
		new BasicProjects(projects).add(myUrl);
		result.use(Results.logic()).redirectTo(ProjectController.class).list();
	}

	@Post
	@Path("/project/phase")
	public void addPhase(Project project, Phase phase) {
		project = projects.get(project.getName());
		project.add(phase);
		result.use(Results.logic()).redirectTo(ProjectController.class).list();
	}

	@Get
	@Path("/project/")
	public Collection<Project> list() {
		return this.projects.all();
	}

	public void run(Project project) throws IllegalArgumentException, SecurityException, InstantiationException,
			IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		final Project found = projects.get(project.getName());
		validator.onError().goTo(ProjectController.class).list();
		if (found == null) {
			validator.add(new ValidationMessage("", "project_not_found"));
		}
		validator.validate();
		final Job job = new Job("Building project " + found.getName());
		jobs.add(job);
		Runnable execution = new Runnable() {
			public void run() {
				try {
					logger.debug("Starting building project " + found.getName());
					Database db = new Database(factory);
					Project toBuild = new Projects(db).get(found.getName()); 
					Build build = toBuild.build();
					new Projects(db).register(build);
					build.start(new Clients(db));
					db.close();
					jobs.remove(job);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		};
		Thread thread = new Thread(execution);
		job.attach(thread);
		thread.start();
	}

	@Get
	@Path("/project/{project.name}/{buildId}")
	public void show(Project project, Long buildId, String filename) {
		logger.debug("Displaying build result for " + project.getName() + "@build-" + buildId + "@" + filename);
		project = projects.get(project.getName());
		result.include("project", project);
		Build build = project.getBuild(buildId);
		result.include("build", build);
		if (filename.equals("")) {
			result.include("currentPath", "");
			result.include("content", build.getContent());
		} else {
			filename = filename.replace('$', '/');
			File base = build.getFile(filename);
			result.include("currentPath", base.getName() + "$");
			result.include("content", base.listFiles());
		}
	}

	@Get
	@Path("/download/project/{project.name}/{buildId}")
	public File showFile(Project project, Long buildId, String filename) {
		logger.debug("Displaying file for " + project.getName() + "@" + buildId + ", file=" + filename);
		project = projects.get(project.getName());
		Build build = project.getBuild(buildId);
		return build.getFile(filename.replace('$', '/'));
	}

	@Post
	@Path("/finish/project/{project.name}/{buildId}/{phasePosition}/{commandId}")
	public void finish(Project project, Long buildId, int phasePosition, int commandId, String result, boolean success,
			Client client) throws IOException, InstantiationException, IllegalAccessException,
			InvocationTargetException, NoSuchMethodException {
		clients.release(client.getId());
		logger.debug("Finishing " + project.getName() + " build " + buildId + " phase " + phasePosition + " command " + commandId);
		project = projects.get(project.getName());
		Build build = project.getBuild(buildId);
		build.finish(phasePosition, commandId, result, success, clients);
		this.result.use(Results.nothing());
	}
	
	@Delete
	@Path("/project/command/{command.id}") 
	public void removeCommand(ExecuteCommandLine command){
		Phase phase = projects.load(command).getPhase();
		phase.remove(projects, command);
	}

}
