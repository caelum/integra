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
import java.lang.reflect.InvocationTargetException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.com.caelum.integracao.server.Application;
import br.com.caelum.integracao.server.Build;
import br.com.caelum.integracao.server.Project;
import br.com.caelum.integracao.server.Projects;
import br.com.caelum.integracao.server.action.BasicProjects;
import br.com.caelum.integracao.server.dao.Database;
import br.com.caelum.integracao.server.dao.DatabaseFactory;
import br.com.caelum.integracao.server.plugin.PluginToRun;
import br.com.caelum.integracao.server.plugin.RegisteredPlugin;
import br.com.caelum.integracao.server.queue.DatabaseRunnable;
import br.com.caelum.integracao.server.queue.Job;
import br.com.caelum.integracao.server.queue.Jobs;
import br.com.caelum.integracao.server.queue.OtherThreadRunner;
import br.com.caelum.integracao.server.queue.QueueThread;
import br.com.caelum.vraptor.Get;
import br.com.caelum.vraptor.Path;
import br.com.caelum.vraptor.Post;
import br.com.caelum.vraptor.Put;
import br.com.caelum.vraptor.Resource;
import br.com.caelum.vraptor.Result;
import br.com.caelum.vraptor.Validator;
import br.com.caelum.vraptor.interceptor.multipart.UploadedFile;
import br.com.caelum.vraptor.validator.ValidationMessage;
import br.com.caelum.vraptor.view.Results;

@Resource
public class ProjectController {

	private final Logger logger = LoggerFactory.getLogger(ProjectController.class);

	private final Projects projects;
	private final Validator validator;
	private final Result result;

	private final DatabaseFactory factory;

	private final Application app;

	private final Jobs jobs;

	private final QueueThread queue;

	private final Database currentDatabase;

	public ProjectController(Projects projects, Validator validator, Result result, DatabaseFactory factory,
			Application app, Jobs jobs, QueueThread queue, Database currentDatabase) {
		this.projects = projects;
		this.validator = validator;
		this.result = result;
		this.factory = factory;
		this.app = app;
		this.jobs = jobs;
		this.queue = queue;
		this.currentDatabase = currentDatabase;
	}

	public void addAll() {
		new BasicProjects(projects).add();
		showList();
	}

	@Get
	@Path("/project/")
	public void list() {
		result.include("projectList", projects.all());
	}

	@Post
	@Path("/project/")
	public void create(Project project, String baseDir, String scmType) throws ClassNotFoundException {
		project.setBaseDir(new File(baseDir));
		project.setControlType(Class.forName(scmType));
		projects.register(project);
		showList();
	}

	@Post
	@Path("/project/{project.name}/run")
	public void run(Project project, final String revision) throws IllegalArgumentException, SecurityException, InstantiationException,
			IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		final Project found = projects.get(project.getName());
		validator.onError().goTo(ProjectController.class).list();
		if (found == null) {
			validator.add(new ValidationMessage("", "project_not_found"));
		}
		validator.validate();
		OtherThreadRunner runner = new OtherThreadRunner(factory, new DatabaseRunnable() {
			public void run(Database db) {
				new ProjectStart(db).runProject(found.getName(), revision);
				queue.wakeup();
			}
		});
		runner.start();
		showProject(project);
	}

	@Put
	@Path("/project/{project.name}/build/{buildCount}/manual")
	public void move(Project project, Long buildCount) {
		project = projects.get(project.getName());
		Build build = project.getBuild(buildCount);
		build.proceedToNextPhase(jobs, currentDatabase);
		showProject(project);
	}

	@Post
	@Path("/finish/job/{job.id}")
	public void finish(final Job job, final String checkoutResult, final String stopResult, final String startResult,
			final boolean success, final UploadedFile content, final String zipOutput, final String artifactsOutput, final UploadedFile artifacts) {
		Job loaded = jobs.load(job.getId());
		if (loaded.getClient() == null) {
			// we do not know who was executing this job!!!
			logger.error("Dont know who was executing " + job.getId());
		} else {
			// TODO IMPORTANT JOB CHECK IF THIS IS THE RIGHT CLIENT, ITS STILL MISSING< OTHERWISE IM CLOSING THE WRONG CLIENT
			loaded.getClient().leaveJob();
		}
		OtherThreadRunner runner = new OtherThreadRunner(factory, new DatabaseRunnable() {
			public void run(Database db) {
				new ProjectContinue(db).nextPhase(job.getId(), checkoutResult, startResult,
						stopResult, success, zipOutput, content, artifactsOutput, artifacts);
				queue.wakeup();
			}
		});
		runner.start();
		this.result.use(Results.nothing());
	}

	private void showList() {
		result.use(Results.logic()).redirectTo(ProjectController.class).list();
	}

	@Post
	@Path("/project/{project.name}/plugin")
	public void addPlugin(Project project, RegisteredPlugin registered)  {
		project = projects.get(project.getName());
		PluginToRun plugin = new PluginToRun(projects.get(registered));
		project.add(plugin);
		projects.registerOrUpdate(plugin.getConfig());
		showProject(project);
	}

	private void showProject(Project project) {
		result.use(Results.logic()).redirectTo(ProjectController.class).show(project);
	}

	@Get
	@Path("/project/{project.name}/")
	public void show(Project project) {
		result.include("plugins", app.getConfig().getAvailablePlugins());
		result.include("project", projects.get(project.getName()));
	}

	@Get
	@Path("/jobs")
	public List<Job> showJobs() {
		return jobs.todo();
	}

}
