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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.com.caelum.integracao.server.Application;
import br.com.caelum.integracao.server.Build;
import br.com.caelum.integracao.server.Client;
import br.com.caelum.integracao.server.Clients;
import br.com.caelum.integracao.server.Project;
import br.com.caelum.integracao.server.Projects;
import br.com.caelum.integracao.server.dao.Database;
import br.com.caelum.integracao.server.dao.DatabaseFactory;
import br.com.caelum.integracao.server.scm.svn.SvnControl;
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
	private final Result result;

	private final DatabaseFactory factory;

	private final Application app;

	public ProjectController(Clients clients, Projects projects, Validator validator, Result result,
			DatabaseFactory factory, Application app) {
		this.clients = clients;
		this.projects = projects;
		this.validator = validator;
		this.result = result;
		this.factory = factory;
		this.app = app;
	}

	public void addAll() {
		new BasicProjects(projects).add();
		showList();
	}

	@Get
	@Path("/project/")
	public void list() {
		result.include("projectList", projects.all());
		result.include("plugins", app.getConfig().getAvailablePlugins());
	}
	
	@Post
	@Path("/project/")
	public void create(Project project, String baseDir) {
		project.setBaseDir(new File(baseDir));
		project.setControlType(SvnControl.class);
		projects.register(project);
		showList();
	}

	public void run(Project project) throws IllegalArgumentException, SecurityException, InstantiationException,
			IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		final Project found = projects.get(project.getName());
		validator.onError().goTo(ProjectController.class).list();
		if (found == null) {
			validator.add(new ValidationMessage("", "project_not_found"));
		}
		validator.validate();
		Runnable execution = new Runnable() {
			public void run() {
				runProject(found.getName());
			}
		};
		Thread thread = new Thread(execution);
		thread.start();
	}

	private void runProject(String name) {
		logger.debug("Starting building project id=" + name);
		Database db = new Database(factory);
		db.beginTransaction();
		try {
			Project toBuild = new Projects(db).get(name);
			Build build = toBuild.build();
			new Projects(db).register(build);
			build.start(new Clients(db), new Application(db));
			db.commit();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (db.hasTransaction()) {
				db.rollback();
			}
			db.close();
		}
	}

	@Post
	@Path("/finish/project/{project.name}/{buildId}/{phasePosition}/{commandId}")
	public void finish(Project project, Long buildId, int phasePosition, int commandId, String result, boolean success,
			Client client) throws IOException, InstantiationException, IllegalAccessException,
			InvocationTargetException, NoSuchMethodException {
		clients.release(client.getId());
		logger.debug("Finishing " + project.getName() + " build " + buildId + " phase " + phasePosition + " command "
				+ commandId);
		project = projects.get(project.getName());
		Build build = project.getBuild(buildId);
		build.finish(phasePosition, commandId, result, success, clients, app);
		this.result.use(Results.nothing());
	}

	private void showList() {
		result.use(Results.logic()).redirectTo(ProjectController.class).list();
	}

}
