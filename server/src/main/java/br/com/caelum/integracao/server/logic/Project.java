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
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import br.com.caelum.integracao.server.scm.ScmControl;

public class Project {

	private final Class<?> controlType;
	private final String uri;
	private String name;
	private final List<Phase> phases = new ArrayList<Phase>();
	private final File baseDir;
	private File buildsDir;
	private File workDir;
	
	protected Project() {
		this.controlType = null;
		this.uri=null;
		this.name = null;
		this.baseDir= null;
	}

	public Project(Class<?> controlType, String uri, File baseDir, String name) {
		this.controlType = controlType;
		this.uri = uri;
		this.baseDir = baseDir;
		this.buildsDir = new File(baseDir, "builds");
		this.buildsDir.mkdirs();
		this.workDir = new File(baseDir, "work");
		this.workDir.mkdirs();
		this.name = name;
		this.phases.add(new Phase(new Checkout()));
		this.phases.add(new Phase(new CreateBuildDir()));
	}

	public void add(Phase p) {
		this.phases.add(p);
	}

	public void execute(Clients clients) throws IllegalArgumentException, SecurityException, InstantiationException,
			IllegalAccessException, InvocationTargetException, NoSuchMethodException, IOException {
		ScmControl control = getControl();
		for (Phase p : phases) {
			p.execute(control, clients);
		}
	}

	private ScmControl getControl() throws InstantiationException, IllegalAccessException, InvocationTargetException,
			NoSuchMethodException {
		return (ScmControl) controlType.getDeclaredConstructor(String.class, File.class, String.class, File.class)
				.newInstance(uri, workDir, name, buildsDir);
	}

	public String getName() {
		return name;
	}

	public List<Build> getBuilds() {
		List<Build> builds = new ArrayList<Build>();
		for(File child: this.buildsDir.listFiles()) {
			if(child.isDirectory()) {
				builds.add(new Build(child));
			}
		}
		return builds;
	}
	
	public void setName(String name) {
		this.name = name;
	}

}
