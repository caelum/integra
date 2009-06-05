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

import br.com.caelum.integracao.server.ExecuteCommandLine;
import br.com.caelum.integracao.server.Phase;
import br.com.caelum.integracao.server.Project;
import br.com.caelum.integracao.server.Projects;
import br.com.caelum.integracao.server.plugin.PluginInformation;
import br.com.caelum.integracao.server.plugin.PluginParameter;
import br.com.caelum.integracao.server.plugin.PluginToRun;
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

	public PhaseController(Result result, Projects projects) {
		this.result = result;
		this.projects = projects;
	}

	@Delete
	@Path("/project/{project.name}/command/{command.id}")
	public void removeCommand(ExecuteCommandLine command) {
		command = projects.load(command);
		projects.remove(command);
		showProject(command.getPhase().getProject());
	}

	@Post
	@Path("/project/{project.name}/command")
	public void addCommand(Phase phase, String startCommand, String stopCommand) {
		logger.debug("Adding a new command with " + startCommand + " and " + stopCommand);
		phase = projects.load(phase);
		ExecuteCommandLine line = new ExecuteCommandLine(phase, commandsFor(startCommand), commandsFor(stopCommand));
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

	@SuppressWarnings("unchecked")
	@Post
	@Path("/project/{project.name}/phase/plugin")
	public void addPlugin(Phase phase, String pluginType) throws ClassNotFoundException {
		phase = projects.load(phase);
		PluginToRun plugin = new PluginToRun((Class<? extends PluginInformation>) Class.forName(pluginType));
		phase.add(plugin);
		showProject(phase.getProject());
	}

	@Get
	@Path("/project/{project.name}/plugin/{plugin.id}")
	public void viewPlugin(PluginToRun plugin) throws ClassNotFoundException {
		result.include("plugin", projects.get(plugin));
	}

	@Delete
	@Path("/project/{project.name}/plugin/{plugin.id}")
	public void remove(PluginToRun plugin, Project project) throws ClassNotFoundException {
		project = projects.get(project.getName());
		plugin = projects.get(plugin);
		project.getPlugins().remove(plugin);
		for(PluginParameter parameter : plugin.getConfig()) {
			projects.delete(parameter);
		}
		projects.remove(plugin);
		showProject(project);
	}

	@Put
	@Path("/project/{project.name}/plugin/{plugin.id}")
	public void updatePlugin(Project project, PluginToRun plugin, List<String> keys, List<String> values) throws ClassNotFoundException {
		logger.debug("Updating " + plugin.getId());
		plugin = projects.get(plugin);
		plugin.updateParameters(keys, values);
		projects.registerOrUpdate(plugin.getConfig());
		showProject(project);
	}

}
