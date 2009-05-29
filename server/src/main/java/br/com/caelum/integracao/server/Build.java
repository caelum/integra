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
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.com.caelum.integracao.server.scm.ScmControl;

/**
 * Represents an build either in process or already processed.
 * 
 * @author guilherme silveira
 * 
 */
@Entity
public class Build {

	private static final Logger logger = LoggerFactory.getLogger(Build.class);

	@ManyToOne
	private Project project;
	
	@Id
	@GeneratedValue
	private Long id;

	private Long buildCount;
	private String revision;
	private int currentPhase;
	@Transient
	private Set<Integer> executedCommandsFromThisPhase = new HashSet<Integer>();
	private boolean sucessSoFar = true;
	private boolean finished = false;

	private Calendar startTime = new GregorianCalendar();
	private Calendar finishTime;

	protected Build() {
	}

	public Build(Project project) {
		this.project = project;
		this.buildCount = project.nextBuild();
		getBaseDirectory().mkdirs();
	}

	public String getRevision() {
		return revision;
	}

	public Long getBuildCount() {
		return buildCount;
	}

	public File[] getContent() {
		return getBaseDirectory().listFiles();
	}

	public File getBaseDirectory() {
		return new File(project.getBuildsDirectory(), "build-" + buildCount);
	}

	public File getFile(String filename) {
		return new File(getBaseDirectory(), filename);
	}

	public void start(Clients clients) throws IllegalArgumentException, SecurityException, InstantiationException,
			IllegalAccessException, InvocationTargetException, NoSuchMethodException, IOException {
		logger.debug("Starting executing process for " + project.getName());
		ScmControl control = project.getControl();
		File tmpFile = File.createTempFile("loading-checkout", ".log");
		control.checkout(tmpFile);
		this.revision = control.getRevision();
		tmpFile.renameTo(getFile("checkout"));

		// executes the first phase
		List<Phase> phases = project.getPhases();
		if (!phases.isEmpty()) {
			this.currentPhase = 0;
			Phase phase = phases.get(0);
			phase.execute(control, this, clients);
		}
	}

	public Project getProject() {
		return this.project;
	}

	public int getCurrentPhase() {
		return currentPhase;
	}

	public synchronized void finish(int phasePosition, int commandId, String result, boolean success, Clients clients)
			throws IOException, InstantiationException, IllegalAccessException, InvocationTargetException,
			NoSuchMethodException {
		sucessSoFar &= success;
		executedCommandsFromThisPhase.add(commandId);
		File file = getFile(phasePosition + "/" + commandId + ".txt");
		file.getParentFile().mkdirs();
		PrintWriter writer = new PrintWriter(new FileWriter(file), true);
		writer.print(result);
		writer.close();
		boolean executedAllCommands = executedCommandsFromThisPhase.size() == project.getPhases().get(phasePosition)
				.getCommandCount();
		if (executedAllCommands && !sucessSoFar) {
			finishTime = new GregorianCalendar();
			finished = true;
		}
		if (executedAllCommands && sucessSoFar) {
			currentPhase++;
			executedCommandsFromThisPhase.clear();
			if (project.getPhases().size() != currentPhase) {
				project.getPhases().get(phasePosition + 1).execute(project.getControl(), this, clients);
			} else {
				finishTime = new GregorianCalendar();
				finished = true;
			}
		}
	}

	public Set<Integer> getExecutedCommandsFromThisPhase() {
		return executedCommandsFromThisPhase;
	}

	public boolean isSuccessSoFar() {
		return sucessSoFar;
	}

	public boolean isFinished() {
		return finished;
	}

	public double getRuntime() {
		Calendar f = Calendar.getInstance();
		if (finishTime != null) {
			f = finishTime;
		}
		return (f.getTimeInMillis() - startTime.getTimeInMillis()) / 1000.0;
	}
	
	public Calendar getFinishTime() {
		return finishTime;
	}
	
	public Calendar getStartTime() {
		return startTime;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getId() {
		return id;
	}
}
