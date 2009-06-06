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

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.com.caelum.integracao.server.dao.Database;
import br.com.caelum.integracao.server.plugin.Plugin;
import br.com.caelum.integracao.server.plugin.PluginToRun;
import br.com.caelum.integracao.server.queue.Job;
import br.com.caelum.integracao.server.queue.Jobs;

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
	@OrderBy("id")
	private List<ExecuteCommandLine> commands = new ArrayList<ExecuteCommandLine>();

	@OneToMany
	@OrderBy("position")
	@Cascade(CascadeType.ALL)
	private List<PluginToRun> plugins = new ArrayList<PluginToRun>();

	@Id
	@GeneratedValue
	private Long id;
	private String name;

	@Column(name = "pos")
	private long position;

	@ManyToOne
	private Project project;

	public Phase(String name, ExecuteCommandLine... cmds) {
		this.name = name;
		this.commands = new ArrayList<ExecuteCommandLine>();
		for (ExecuteCommandLine cmd : cmds) {
			commands.add(cmd);
		}
	}

	protected Phase() {
	}

	public void execute(Build build, Jobs jobs) {
		if (logger.isDebugEnabled()) {
			logger.debug("Scheduling phase " + name + " for project " + build.getProject().getName() + " containing "
					+ commands.size() + " parallel commands.");
		}
		build.getFile(name).mkdirs();
		for (ExecuteCommandLine command : commands) {
			jobs.add(new Job(build, command));
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
		List<ExecuteCommandLine> active = new ArrayList<ExecuteCommandLine>();
		for (ExecuteCommandLine cmd : commands) {
			if (cmd.isActive()) {
				active.add(cmd);
			}
		}
		return active;
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

	public List<PluginToRun> getPlugins() {
		return plugins;
	}

	/**
	 * Runs all plugins after this build phase has been completed, no matter
	 * with a sucess or not. Plugins are run in order and if one tells the
	 * system to stop, no other plugins are run.
	 */
	public boolean runAfter(Build build, Database database) {
		for (PluginToRun toRun : getPlugins()) {
			Plugin plugin = toRun.getPlugin(database);
			if (plugin == null) {
				return false;
			} else {
				if (!plugin.after(build, this)) {
					return false;
				}
			}
		}
		return true;
	}

	public void add(PluginToRun run) {
		getPlugins().add(run);
		run.setPosition(this.getPlugins().size());
	}

}
