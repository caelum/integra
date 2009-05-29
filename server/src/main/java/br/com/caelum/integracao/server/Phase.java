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

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.com.caelum.integracao.server.project.Build;
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
	
	@OneToMany
	private List<ExecuteCommandLine> commands;
	
	@Id
	@GeneratedValue
	private Long id;
	private String name;
	private long position;

	public Phase(String id, ExecuteCommandLine... cmds) {
		this.name = id;
		this.commands = new ArrayList<ExecuteCommandLine>();
		for(ExecuteCommandLine cmd : cmds) {
			commands.add(cmd);
		}
	}
	
	public Phase() {
	}

	public void execute(ScmControl control, Build build, Clients clients) throws IOException {
		if (logger.isDebugEnabled()) {
			logger.debug("Starting phase " + name + " for project " + build.getProject().getName() + " containing "
					+ commands.size() + " parallel commands.");
		}
		build.getFile(position + "/" + name).mkdirs();
		for (ExecuteCommandLine command : commands) {
			Client client;
			try {
				client = clients.getFreeClient(getName() + "/"+command.getName());
			} catch (IllegalStateException e) {
				// there is no client available
				try {
					build.finish((int) position, command.getPosition(), "NOT ENOUGHT CLIENTS", false, clients);
				} catch (Exception ex) {
					ex.printStackTrace();
				}
				return;
			}
			command.executeAt(client, build, control, File.createTempFile("connection", "txt"));
		}
	}

	public int getCommandCount() {
		return commands.size();
	}
	
	public long getPhasePosition() {
		return position;
	}
	
	public String getName() {
		return name;
	}
	
	public List<ExecuteCommandLine> getCommands() {
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
	
}
