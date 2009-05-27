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
package br.com.caelum.integracao.client;

import java.io.File;
import java.io.StringWriter;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.com.caelum.integracao.CommandToExecute;
import br.com.caelum.integracao.client.project.Project;
import br.com.caelum.integracao.client.project.Projects;
import br.com.caelum.vraptor.Resource;

@Resource
public class JobController {
	
	private final Logger logger = LoggerFactory.getLogger(JobController.class);
	
	private final EntryPoint point;
	private final Projects projects;
	
	private Project currentJob;

	public JobController(EntryPoint point, Projects projects) {
		this.point = point;
		this.projects = projects;
		this.currentJob = null;
	}

	public void execute(Project p, String revision, List<String> command, String phase, String commandPosition) {
		if(this.currentJob!=null) {
			throw new RuntimeException("Cannot take another job as im currently processing " + currentJob.getName());
		}
		this.currentJob = projects.get(p.getName());
		try {
			File dir = new File(point.getBaseDir(), currentJob.getName());
			StringWriter writer = new StringWriter();
			new CommandToExecute("svn", "update", "-r" , revision).at(dir).runAs("svn-update");
			String[] commands = command.toArray(new String[command.size()]);
			new CommandToExecute(commands).at(dir).logTo(writer).runAs("execute");
		} finally {
			logger.debug("Job " + this.currentJob.getName() + " has finished");
			this.currentJob = null;
		}
	}

}
