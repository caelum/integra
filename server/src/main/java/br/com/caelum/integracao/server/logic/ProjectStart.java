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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.com.caelum.integracao.server.Build;
import br.com.caelum.integracao.server.Builds;
import br.com.caelum.integracao.server.Project;
import br.com.caelum.integracao.server.Projects;
import br.com.caelum.integracao.server.dao.Database;
import br.com.caelum.integracao.server.queue.Jobs;

/**
 * Few commands together for starting up the project.
 * 
 * @author guilherme silveira
 */
public class ProjectStart {

	private final static Object protectTwoBuildsOfStartingAtTheSameTime = new Object();

	private final Logger logger = LoggerFactory.getLogger(ProjectStart.class);
	private final Database database;

	public ProjectStart(Database database) {
		this.database = database;
	}

	void runProject(String name) {
		synchronized (protectTwoBuildsOfStartingAtTheSameTime) {
			// TODO we probably dont need a new database connection, we can
			// probably
			// work out with the original one
			logger.debug("Starting building project id=" + name);
			try {
				database.beginTransaction();
				Project toBuild = new Projects(database).get(name);
				Build build = toBuild.build();
				new Projects(database).register(build);
				build.setRevisionAsNextOne(new Projects(database), new Builds(database), database);
				build.start(new Jobs(database), database);
				database.commit();
			} catch (Exception e) {
				logger.error("Unable to start project build", e);
			} finally {
				if (database.hasTransaction()) {
					database.rollback();
				}
			}
		}
	}

}
