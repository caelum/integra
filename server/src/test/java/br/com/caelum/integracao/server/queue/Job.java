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
package br.com.caelum.integracao.server.queue;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Calendar;
import java.util.GregorianCalendar;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.com.caelum.integracao.server.Build;
import br.com.caelum.integracao.server.Client;
import br.com.caelum.integracao.server.Config;
import br.com.caelum.integracao.server.ExecuteCommandLine;
import br.com.caelum.integracao.server.Phase;
import br.com.caelum.integracao.server.Project;
import br.com.caelum.integracao.server.action.Dispatcher;
import br.com.caelum.integracao.server.dao.Database;

@Entity
public class Job {

	private static final Logger logger = LoggerFactory.getLogger(Job.class);

	@ManyToOne
	private Build build;

	@ManyToOne
	private ExecuteCommandLine command;

	@ManyToOne
	private Client client;

	public Job(Build build, ExecuteCommandLine command) {
		this.build = build;
		this.command = command;
	}

	@Id
	@GeneratedValue
	private Long id;

	@Temporal(TemporalType.TIMESTAMP)
	private Calendar schedulingTime = new GregorianCalendar();

	private boolean finished;

	@Temporal(TemporalType.TIMESTAMP)
	private Calendar startTime;

	private Calendar finishTime;

	public ExecuteCommandLine getCommand() {
		return command;
	}

	public void executeAt(Client at, File logFile, Config config) throws IOException {
		logger.debug("Trying to execute " + command.getName() + " @ " + at.getHost() + ":" + at.getPort());
		Dispatcher connection = client.getConnection(logFile, config.getUrl());
		try {
			connection.register(build.getProject()).execute(command, this).close();
			this.client = at;
			this.startTime = Calendar.getInstance();
		} finally {
			connection.close();
		}
	}

	public Long getId() {
		return id;
	}

	public Build getBuild() {
		return build;
	}

	public Client getClient() {
		return client;
	}

	public boolean isFinished() {
		return finished;
	}

	public void finish(String result, boolean success, Database database) throws IOException {
		
		Project project = build.getProject();
		logger.debug("Finishing " + project.getName() + " build " + build.getBuildCount() + " phase "
				+ command.getPhase().getName() + " command " + command.getId());
		
		if(!success) {
			build.failed();
		}
		finished = true;
		this.finishTime = Calendar.getInstance();
		
		Phase phase = command.getPhase();
		File file = build.getFile(phase.getName() + "/" + command.getName() + ".txt");
		file.getParentFile().mkdirs();
		PrintWriter writer = new PrintWriter(new FileWriter(file), true);
		writer.print(result);
		writer.close();
		
		build.proceed(phase, database);
		
	}

}
