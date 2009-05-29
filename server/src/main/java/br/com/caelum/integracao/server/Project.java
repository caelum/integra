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
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;

import org.hibernate.validator.Min;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.com.caelum.integracao.server.project.Build;
import br.com.caelum.integracao.server.scm.ScmControl;

/**
 * Represents a project which should be built.
 * 
 * @author guilherme silveira
 */
@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = "name"))
public class Project {

	@Min(10)
	private long checkInterval;

	private static final Logger logger = LoggerFactory.getLogger(Project.class);
	private Class<?> controlType;
	private String uri;
	private String name;
	@Transient
	private final List<Phase> phases = new ArrayList<Phase>();
	private File baseDir;

	private transient File buildsDirectory;
	private transient File workDir;
	private Long buildCount = 0L;
	@Transient
	private final List<Build> builds = new ArrayList<Build>();

	private Calendar lastBuild = new GregorianCalendar();

	protected Project() {
	}

	public Project(Class<?> controlType, String uri, File baseDir, String name) {
		this.controlType = controlType;
		this.uri = uri;
		this.baseDir = baseDir;
		this.buildsDirectory = new File(baseDir, "builds");
		this.buildsDirectory.mkdirs();
		this.workDir = new File(baseDir, name);
		this.workDir.mkdirs();
		this.name = name;
	}

	public void add(Phase p) {
		p.setPosition(phases.size());
		this.phases.add(p);
	}

	public ScmControl getControl() throws InstantiationException, IllegalAccessException, InvocationTargetException,
			NoSuchMethodException {
		return (ScmControl) controlType.getDeclaredConstructor(String.class, File.class, String.class).newInstance(uri,
				baseDir, name);
	}

	public String getName() {
		return name;
	}

	public List<Build> getBuilds() {
		return builds;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getUri() {
		return uri;
	}

	public Build getBuild(Long id) {
		for (Build b : getBuilds()) {
			if (b.getBuildCount().equals(id)) {
				return b;
			}
		}
		return null;
	}

	public Build build() {
		Build build = new Build(this);
		this.builds.add(build);
		return build;
	}

	public Long getBuildCount() {
		return buildCount;
	}

	public Long nextBuild() {
		return ++buildCount;
	}

	public File getBuildsDirectory() {
		return this.buildsDirectory;
	}

	public List<Phase> getPhases() {
		return this.phases;
	}

	public Calendar getLastBuild() {
		return lastBuild;
	}

}
