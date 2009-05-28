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
package br.com.caelum.integracao.server.project;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.com.caelum.integracao.server.Clients;
import br.com.caelum.integracao.server.Phase;
import br.com.caelum.integracao.server.Project;
import br.com.caelum.integracao.server.scm.ScmControl;

/**
 * Represents an build either in process or already processed.
 * 
 * @author guilherme silveira
 * 
 */
public class Build {

	private final Logger logger = LoggerFactory.getLogger(Build.class);

	private Project project;

	private Long buildCount;
	private String revision;
	private int currentPhase;
	private Set<Integer> executedCommandsFromThisPhase = new HashSet<Integer>();
	private boolean sucessSoFar = true;
	private boolean finished = false;

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

	public synchronized void finish(int phaseId, int commandId, String result, boolean sucess, Clients clients)
			throws IOException, InstantiationException, IllegalAccessException, InvocationTargetException,
			NoSuchMethodException {
		sucessSoFar &= sucess;
		executedCommandsFromThisPhase.add(commandId);
		File file = getFile(phaseId + "/" + commandId + ".txt");
		file.getParentFile().mkdirs();
		PrintWriter writer = new PrintWriter(new FileWriter(file), true);
		writer.print(result);
		writer.close();
		boolean executedAllCommands = executedCommandsFromThisPhase.size() == project.getPhases().get(phaseId)
				.getCommandCount();
		if(executedAllCommands && !sucessSoFar) {
			finished = true;
		}
		if (executedAllCommands && sucessSoFar) {
			currentPhase++;
			executedCommandsFromThisPhase.clear();
			if (project.getPhases().size() != currentPhase) {
				project.getPhases().get(phaseId + 1).execute(project.getControl(), this, clients);
			} else {
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
}
