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

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.com.caelum.integracao.server.BuildCommand;
import br.com.caelum.integracao.server.Phase;
import br.com.caelum.integracao.server.Project;
import br.com.caelum.integracao.server.Projects;
import br.com.caelum.integracao.server.label.Labels;
import br.com.caelum.integracao.server.plugin.PluginToRun;
import br.com.caelum.integracao.server.plugin.RegisteredPlugin;
import br.com.caelum.vraptor.Delete;
import br.com.caelum.vraptor.Get;
import br.com.caelum.vraptor.Path;
import br.com.caelum.vraptor.Post;
import br.com.caelum.vraptor.Put;
import br.com.caelum.vraptor.Resource;
import br.com.caelum.vraptor.Result;
import br.com.caelum.vraptor.ioc.RequestScoped;
import br.com.caelum.vraptor.view.Results;

@RequestScoped
@Resource
public class PhaseController {

	private final Logger logger = LoggerFactory.getLogger(PhaseController.class);

	private final Projects projects;
	private final Result result;

	private final Labels labels;

	public PhaseController(Result result, Projects projects, Labels labels) {
		this.result = result;
		this.projects = projects;
		this.labels = labels;
	}

	@Get
	@Path("/project/{project.name}/command/{command.id}/snippet")
	public void editCommand(BuildCommand command) {
		command = projects.load(command);
		result.include("command", command);
	}

	@Put
	@Path("/project/{project.name}/command/{command.id}")
	public void updateCommand(BuildCommand command, String startCommand, String stopCommand, String labels, String artifactsToPush) {
		command = projects.load(command);
		command.deactivate();
		command = new BuildCommand(command.getPhase(), commandsFor(startCommand), commandsFor(stopCommand), this.labels.lookup(labels), artifactsToPush);
		projects.create(command);
		showProject(command.getPhase().getProject());
	}

	@Delete
	@Path("/project/{project.name}/command/{command.id}")
	public void removeCommand(BuildCommand command) {
		command = projects.load(command);
		command.deactivate();
		projects.remove(command);
		showProject(command.getPhase().getProject());
	}

	@Post
	@Path("/project/{project.name}/command")
	public void addCommand(Phase phase, String startCommand, String stopCommand, String labels, String artifactsToPush) {
		logger.debug("Adding a new command with " + startCommand + " and " + stopCommand);
		phase = projects.get(phase);
		BuildCommand line = new BuildCommand(phase, commandsFor(startCommand), commandsFor(stopCommand), this.labels.lookup(labels), artifactsToPush);
		projects.register(line);
		showProject(phase.getProject());
	}

	private String[] commandsFor(String cmd) {
		if(cmd==null || cmd.trim().equals("")) {
			return new String[0];
		}
		return cmd.split("\\s");
	}

	private void showProject(Project project) {
		result.use(Results.logic()).redirectTo(ProjectController.class).show(project);
	}

	@Post
	@Path("/project/{project.name}/phase")
	public void addPhase(Project project, Phase phase) {
		project = projects.get(project.getName());
		project.add(phase);
		projects.register(phase);
		showProject(project);
	}

	@Post
	@Path("/project/{project.name}/phase/plugin")
	public void addPlugin(Phase phase, RegisteredPlugin registered) {
		phase = projects.get(phase);
		PluginToRun plugin = new PluginToRun(projects.get(registered));
		phase.add(plugin);
		projects.registerOrUpdate(plugin.getConfig());
		showProject(phase.getProject());
	}

	@Get
	@Path("/project/{project.name}/plugin/{plugin.id}/snippet")
	public void viewPlugin(PluginToRun plugin) {
		plugin = projects.get(plugin);
		result.include("plugin", plugin);
		result.include("information", plugin.getType().getInformation());
	}

	@Get
	@Path("/project/{project.name}/pluginPrepare")
	public void prepareNewPlugin(RegisteredPlugin registered) {
		registered = projects.get(registered);
		result.include("information", registered.getInformation());
	}

	@Delete
	@Path("/project/{project.name}/plugin/{plugin.id}")
	public void remove(PluginToRun plugin, Project project)  {
		project = projects.get(project.getName());
		plugin = projects.get(plugin);
		project.getPlugins().remove(plugin);
		projects.remove(plugin);
		showProject(project);
	}

	@Delete
	@Path("/project/{project.name}/phase/{phase.id}/plugin/{plugin.id}")
	public void remove(PluginToRun plugin, Phase phase, Project project) {
		phase = projects.get(phase);
		plugin = projects.get(plugin);
		phase.getPlugins().remove(plugin);
		projects.remove(plugin);
		showProject(project);
	}

	@Put
	@Path("/project/{project.name}/plugin/{plugin.id}")
	public void updatePlugin(Project project, PluginToRun plugin, List<String> keys, List<String> values)  {
		logger.debug("Updating " + plugin.getId());
		plugin = projects.get(plugin);
		plugin.updateParameters(keys, values);
		projects.registerOrUpdate(plugin.getConfig());
		showProject(project);
	}

}
