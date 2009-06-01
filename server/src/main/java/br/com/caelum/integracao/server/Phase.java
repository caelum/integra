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
package br.com.caelum.integracao.server;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.com.caelum.integracao.server.plugin.PluginToRun;
import br.com.caelum.integracao.server.scm.ScmControl;

/**
 * A build consists of many different phases. At each phase some targets are
 * executed. ExecuteCommands within phases might be parallellized.
 * 
 * @author guilherme silveira
 */
@Entity
public class Phase {

	private static final Logger logger = LoggerFactory.getLogger(Phase.class);

	@OneToMany(mappedBy = "phase")
	@OrderBy("position")
	private List<ExecuteCommandLine> commands = new ArrayList<ExecuteCommandLine>();
	
	@OneToMany(mappedBy="phase")
	@OrderBy("position")
	private List<PluginToRun> plugins;

	@Id
	@GeneratedValue
	private Long id;
	private String name;
	
	@Column(name="pos")
	private long position;

	@ManyToOne
	private Project project;

	public Phase(String id, ExecuteCommandLine... cmds) {
		this.name = id;
		this.commands = new ArrayList<ExecuteCommandLine>();
		for (ExecuteCommandLine cmd : cmds) {
			commands.add(cmd);
		}
	}

	public Phase() {
	}

	public void execute(ScmControl control, Build build, Clients clients, Application app) throws IOException {
		if (logger.isDebugEnabled()) {
			logger.debug("Starting phase " + name + " for project " + build.getProject().getName() + " containing "
					+ commands.size() + " parallel commands.");
		}
		build.getFile(position + "").mkdirs();
		for (ExecuteCommandLine command : commands) {
			Client client;
			try {
				client = clients.getFreeClient(getName() + "/" + command.getName());
			} catch (IllegalStateException e) {
				// there is no client available
				try {
					build.finish((int) position, command.getPosition(), "NOT ENOUGHT CLIENTS", false, clients, app);
				} catch (Exception ex) {
					ex.printStackTrace();
				}
				return;
			}
			command.executeAt(client, build, control, File.createTempFile("connection", "txt"), app.getConfig().getUrl());
		}
	}

	public int getCommandCount() {
		return getCommands().size();
	}

	public long getPhasePosition() {
		return position;
	}

	public String getName() {
		return name;
	}

	public List<ExecuteCommandLine> getCommands() {
		if(commands==null) {
			this.commands = new ArrayList<ExecuteCommandLine>();
		}
		return commands;
	}

	public void setCommands(List<ExecuteCommandLine> commands) {
		this.commands = commands;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setPosition(long position) {
		this.position = position;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public long getPosition() {
		return position;
	}

	public void setProject(Project project) {
		this.project = project;
	}

	public Project getProject() {
		return project;
	}

	public void remove(Projects projects, ExecuteCommandLine command) {
		for(ExecuteCommandLine cmd : getCommands()) {
			if(cmd.getPosition()>command.getPosition()) {
				cmd.setPosition(cmd.getPosition()-1);
			}
		}
		getCommands().remove(command);
		projects.remove(command);
	}
	
	public List<PluginToRun> getPlugins() {
		return plugins;
	}

}
