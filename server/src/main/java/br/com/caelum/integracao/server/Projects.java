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

import java.util.Collection;
import java.util.List;

import org.hibernate.Session;

import br.com.caelum.integracao.server.build.Tab;
import br.com.caelum.integracao.server.dao.Database;
import br.com.caelum.integracao.server.plugin.PluginParameter;
import br.com.caelum.integracao.server.plugin.PluginToRun;
import br.com.caelum.vraptor.ioc.RequestScoped;

/**
 * Represents all projects.
 * 
 * @author guilherme silveira
 */
@RequestScoped
public class Projects {

	private final Session session;

	public Projects(Database database) {
		this.session = database.getSession();
	}

	public Project get(String name) {
		return (Project) session.createQuery("from Project as p where p.name=:name").setParameter("name",name).uniqueResult();
	}

	public Collection<Project> all() {
		return session.createQuery("from Project").list();
	}

	public void register(Project p) {
		session.save(p);
	}

	public void create(Phase phase) {
		session.save(phase);
	}

	public void create(ExecuteCommandLine cmd) {
		session.save(cmd);
	}

	public ExecuteCommandLine load(ExecuteCommandLine command) {
		return (ExecuteCommandLine) session.load(ExecuteCommandLine.class, command.getId());
	}

	public void register(Build build) {
		session.save(build);
	}

	public void register(ExecuteCommandLine line) {
		session.save(line);
	}

	public void register(Phase phase) {
		session.save(phase);
	}

	public Phase get(Phase phase) {
		return (Phase) session.load(Phase.class, phase.getId());
	}

	public PluginToRun get(PluginToRun plugin) {
		return (PluginToRun) session.load(PluginToRun.class, plugin.getId());
	} 

	public void registerOrUpdate(List<PluginParameter> config) {
		for(PluginParameter param : config) {
			session.saveOrUpdate(param);
		}
	}

	public void remove(PluginToRun plugin) {
		for(PluginParameter parameter : plugin.getConfig()) {
			session.delete(parameter);
		}
		session.delete(plugin);
	}

	public void register(Tab tab) {
		session.save(tab);
	}

}
