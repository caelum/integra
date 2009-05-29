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

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import br.com.caelum.integracao.server.ExecuteCommandLine;
import br.com.caelum.integracao.server.Phase;
import br.com.caelum.integracao.server.Project;
import br.com.caelum.integracao.server.Projects;
import br.com.caelum.integracao.server.scm.svn.SvnControl;

public class BasicProjects {

	private final Projects projects;

	public BasicProjects(Projects projects) {
		this.projects = projects;
	}

	public void add(String myUrl) {
		{
			Project p = new Project(SvnControl.class, "svn+ssh://localhost/svn/caelum/caelumweb2/trunk",
					new File("/home/integra/build/caelumweb2"), "caelumweb2");
			projects.register(p);
			p.add(new Phase("compile", new ExecuteCommandLine(myUrl, 0, 0, "ant", "clear", "compile")));
			p.add(new Phase("test", new ExecuteCommandLine(myUrl, 1, 0, "ant", "clear", "test")));
			p.add(new Phase("integration-test", new ExecuteCommandLine(myUrl, 2, 0, "ant", "clear",
					"integration-test-1"), new ExecuteCommandLine(myUrl, 2, 1, "ant", "integration-test-2")));
			saveInternals(p);
		}
		{
			Project p = new Project(SvnControl.class,
					"https://vraptor2.svn.sourceforge.net/svnroot/vraptor2/trunk/core", new File(
							"/home/integra/build/vraptor2"), "vraptor2");
			projects.register(p);
			p.add(new Phase("compile", new ExecuteCommandLine(myUrl, 0, 0, "mvn", "clean", "compile")));
			p.add(new Phase("test", new ExecuteCommandLine(myUrl, 1, 0, "mvn", "clean", "test")));
			p.add(new Phase("double-test", new ExecuteCommandLine(myUrl, 2, 0, "mvn", "clean", "test"),
					new ExecuteCommandLine(myUrl, 2, 1, "mvn", "test")));
			saveInternals(p);
		}
		{
			Project p = new Project(SvnControl.class,
					"http://svn.vidageek.net/mirror/trunk", new File(
							"/home/integra/build/mirror"), "mirror");
			projects.register(p);
			p.add(new Phase("compile", new ExecuteCommandLine(myUrl, 0, 0, "mvn", "clean", "compile")));
			p.add(new Phase("test", new ExecuteCommandLine(myUrl, 1, 0, "mvn", "clean", "test")));
			p.add(new Phase("double-test", new ExecuteCommandLine(myUrl, 2, 0, "mvn", "clean", "test"),
					new ExecuteCommandLine(myUrl, 2, 1, "mvn", "clean", "test")));
			saveInternals(p);
		}
		{
			Project p = new Project(SvnControl.class, "file:///Users/guilherme/Documents/temp/myproject",
					new File("/Users/guilherme/int"), "my-anted");
			projects.register(p);
			p.add(new Phase("compile", new ExecuteCommandLine(myUrl, 0, 0, "ant", "compile")));
			p.add(new Phase("test", new ExecuteCommandLine(myUrl, 1, 0, "ant", "test")));
			p.add(new Phase("deploy", new ExecuteCommandLine(myUrl, 2, 0, "ant", "deploy"), new ExecuteCommandLine(
					myUrl, 2, 1, "ant", "while-deploy")));
			saveInternals(p);
		}
	}


	private void saveInternals(Project p) {
		for(Phase phase : p.getPhases()) {
			phase.setProject(p);
			List<ExecuteCommandLine> cmds = phase.getCommands();
			phase.setCommands(new ArrayList<ExecuteCommandLine>());
			projects.create(phase);
			for(ExecuteCommandLine cmd : cmds) {
				cmd.setPhase(phase);
				projects.create(cmd);
			}
		}
	}
}
