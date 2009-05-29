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
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;

import org.hibernate.validator.Min;
import org.hibernate.validator.NotEmpty;
import org.hibernate.validator.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
	private long checkInterval = 60;

	@Id
	@GeneratedValue
	private Long id;

	private static final Logger logger = LoggerFactory.getLogger(Project.class);
	private Class<?> controlType;
	@NotEmpty
	private String uri;
	@NotEmpty
	private String name;
	@OneToMany(mappedBy = "project")
	@OrderBy("position")
	private List<Phase> phases = new ArrayList<Phase>();
	@NotNull
	private File baseDir;

	private Long buildCount = 0L;

	@Transient
	private final List<Build> builds = new ArrayList<Build>();
	@NotNull
	private Calendar lastBuild = new GregorianCalendar();

	protected Project() {
	}

	public Project(Class<?> controlType, String uri, File dir, String name) {
		this.controlType = controlType;
		this.uri = uri;
		this.name = name;
		this.setBaseDir(dir);
	}

	public void add(Phase p) {
		p.setPosition(phases.size());
		this.phases.add(p);
	}

	public Build build() {
		Build build = new Build(this);
		this.builds.add(build);
		return build;
	}

	public File getBaseDir() {
		return baseDir;
	}

	public Build getBuild(Long id) {
		for (Build b : getBuilds()) {
			if (b.getBuildCount().equals(id)) {
				return b;
			}
		}
		return null;
	}

	public Long getBuildCount() {
		return buildCount;
	}

	public List<Build> getBuilds() {
		return builds;
	}

	public File getBuildsDirectory() {
		File buildsDirectory = new File(baseDir, "builds");
		buildsDirectory.mkdirs();
		return buildsDirectory;
	}

	public long getCheckInterval() {
		return checkInterval;
	}

	public ScmControl getControl() throws InstantiationException, IllegalAccessException, InvocationTargetException,
			NoSuchMethodException {
		logger.debug("Creating scm control " + controlType.getName() + " for project " + getName());
		return (ScmControl) controlType.getDeclaredConstructor(String.class, File.class, String.class).newInstance(uri,
				baseDir, name);
	}

	public Calendar getLastBuild() {
		return lastBuild;
	}

	public String getName() {
		return name;
	}

	public List<Phase> getPhases() {
		return this.phases;
	}

	public String getUri() {
		return uri;
	}

	public Long nextBuild() {
		return ++buildCount;
	}

	public void setBaseDir(File dir) {
		this.baseDir = dir;
		new File(baseDir, name).mkdirs();
	}

	public void setBuildCount(Long buildCount) {
		this.buildCount = buildCount;
	}

	public void setCheckInterval(long checkInterval) {
		this.checkInterval = checkInterval;
	}

	public void setLastBuild(Calendar lastBuild) {
		this.lastBuild = lastBuild;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setPhases(List<Phase> phases) {
		this.phases = phases;
	}

	public void setUri(String uri) {
		this.uri = uri;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getId() {
		return id;
	}

}
