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

import java.io.PrintWriter;
import java.io.StringWriter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.com.caelum.integracao.server.Builds;
import br.com.caelum.integracao.server.Project;
import br.com.caelum.integracao.server.Projects;
import br.com.caelum.integracao.server.dao.Database;
import br.com.caelum.integracao.server.dao.DatabaseFactory;
import br.com.caelum.integracao.server.log.LogFile;
import br.com.caelum.integracao.server.scm.Revision;

public class PingScm {

	private final Logger logger = LoggerFactory.getLogger(PingScm.class);

	void buildProjects(DatabaseFactory factory) {
		synchronized (ProjectStart.protectTwoBuildsOfProcessingAtTheSameTime) {
			Database db = new Database(factory);
			try {
				Projects projects = new Projects(db);
				for (Project project : projects.allActive()) {
					tryToBuild(db, project);
				}
			} finally {
				db.close();
			}
		}
	}

	private void tryToBuild(Database db, Project project) {
		if (project.wasntBuiltYet()) {
			return;
		}
		Builds builds = new Builds(db);
		if (!project.isReadyToRestartBuild()) {
			return;
		}
		logger.debug("Project " + project.getName() + " is ready for a scm check.");
		StringWriter log = new StringWriter();
		PrintWriter writer = new PrintWriter(log, true);
		try {
			Revision nextRevision = project.extractNextRevision(project.getControl(), builds, new LogFile(writer));
			if(project.mostUpdatedRevisionIs(nextRevision)) {
				new ProjectStart(db).runProject(project.getName(), null);
			} else {
				logger.debug(project.getName() + " did not require a new build");
			}
		} catch (Exception e) {
			logger.debug("Unable to build project " + project.getName() + " due to " + log.getBuffer().toString(), e);
		}
	}

}
