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

import br.com.caelum.integracao.server.Project;
import br.com.caelum.integracao.server.Projects;
import br.com.caelum.integracao.server.scm.svn.SvnControl;

public class BasicProjects {

	private final Projects projects;

	public BasicProjects(Projects projects) {
		this.projects = projects;
	}

	public void add() {
		{
			Project p = new Project(SvnControl.class, "svn+ssh://192.168.0.2/svn/caelum/caelumweb2/trunk", new File(
					"/home/integra/build/caelumweb2"), "caelumweb2");
			projects.register(p);
		}
		{
			Project p = new Project(SvnControl.class, "https://vraptor2.svn.sourceforge.net/svnroot/vraptor2/trunk",
					new File("/home/integra/build/vraptor2"), "vraptor2");
			projects.register(p);
		}
		{
			Project p = new Project(SvnControl.class, "http://svn.vidageek.net/mirror/trunk", new File(
					"/home/integra/build/mirror"), "mirror");
			projects.register(p);
		}
		{
			Project p = new Project(SvnControl.class, "svn://svn.paranamer.codehaus.org/paranamer/trunk", new File(
					"/home/integra/build/paranamer"), "paranamer");
			projects.register(p);
		}
		{
			Project p = new Project(SvnControl.class,
					"http://seleniumdsl.svn.sourceforge.net/svnroot/seleniumdsl/trunk", new File(
							"/home/integra/build/selenium-dsl"), "selenium-dsl");
			projects.register(p);
		}
		{
			Project p = new Project(SvnControl.class, "file:///Users/guilherme/Documents/temp/myproject", new File(
					"/Users/guilherme/int/anted"), "my-anted");
			projects.register(p);
		}
		{
			Project p = new Project(SvnControl.class, "file:///Users/guilherme/Documents/temp/myproject", new File(
					"/Users/guilherme/int/wicked"), "wicked");
			projects.register(p);
		}
	}

}
