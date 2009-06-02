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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import org.hibernate.annotations.CollectionOfElements;
import org.hibernate.validator.Min;
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

	@Min(0)
	private Long buildCount;
	private String revision;

	@Min(0)
	private int currentPhase = 0;

	@CollectionOfElements
	private Set<Integer> executedCommandsFromThisPhase = new HashSet<Integer>();

	private boolean successSoFar = true;
	private boolean finished = false;

	private Calendar startTime = new GregorianCalendar();
	private Calendar finishTime;
	
	@OneToMany(mappedBy="build")
	private List<UsedClient> usedClients = new ArrayList<UsedClient>(); 

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

	public void start(Clients clients, Application app) throws IllegalArgumentException, SecurityException,
			InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException,
			IOException {
		this.currentPhase = 0;
		logger.debug("Starting executing process for " + project.getName() + " at "
				+ project.getBaseDir().getAbsolutePath());
		ScmControl control = project.getControl();
		int result = update(control);
		if (result == 0) {
			List<Phase> phases = project.getPhases();
			if (!phases.isEmpty()) {
				Phase phase = phases.get(0);
				phase.execute(control, this, clients, app);
			}
		} else {
			finish(false);
		}
	}

	private void finish(boolean success) {
		this.finished = true;
		this.successSoFar = success;
		this.finishTime = new GregorianCalendar();
	}

	private int update(ScmControl control) throws InstantiationException, IllegalAccessException,
			InvocationTargetException, NoSuchMethodException, IOException {
		File tmpFile = File.createTempFile("loading-checkout", ".log");
		int result = control.checkoutOrUpdate(tmpFile);
		this.revision = control.getRevision();
		tmpFile.renameTo(getFile("checkout.txt"));
		logger.debug("Checking out " + project.getName() + ", build = " + buildCount + " resulted in " + result);
		return result;
	}

	public Project getProject() {
		return this.project;
	}

	public int getCurrentPhase() {
		return currentPhase;
	}

	public synchronized void finish(int phasePosition, int commandId, String result, boolean success, Clients clients,
			Application app) throws IOException, InstantiationException, IllegalAccessException,
			InvocationTargetException, NoSuchMethodException {
		successSoFar &= success;
		executedCommandsFromThisPhase.add(commandId);
		File file = getFile(phasePosition + "/" + commandId + ".txt");
		file.getParentFile().mkdirs();
		PrintWriter writer = new PrintWriter(new FileWriter(file), true);
		writer.print(result);
		writer.close();
		Phase actualPhase = project.getPhases().get(phasePosition);
		boolean executedAllCommands = executedCommandsFromThisPhase.size() == actualPhase.getCommandCount();
		if (executedAllCommands) {
			logger.debug("Preparing to execute plugins for " + getProject().getName() + " with success = " + successSoFar);
			successSoFar &= actualPhase.runAfter(this);
			if (successSoFar) {
				currentPhase++;
				executedCommandsFromThisPhase.clear();
				if (project.getPhases().size() != currentPhase) {
					project.getPhases().get(phasePosition + 1).execute(project.getControl(), this, clients, app);
				} else {
					finish(true);
				}
			} else {
				finish(false);
			}
		}
	}

	public Set<Integer> getExecutedCommandsFromThisPhase() {
		return executedCommandsFromThisPhase;
	}

	public boolean isSuccessSoFar() {
		return successSoFar;
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

	public boolean buildStatusChangedFromLastBuild() {
		Build previous = getProject().getBuild(getBuildCount() - 1);
		if (previous == null) {
			// first build ever
			return true;
		}
		return previous.isSuccessSoFar() != isSuccessSoFar();
	}

	public List<UsedClient> getClientsFor(Phase phase) {
		List<UsedClient> clients = new ArrayList<UsedClient>();
		for(UsedClient client : usedClients) {
			if(phase.getCommands().contains(client.getExecutedCommand())) {
				clients.add(client);
			}
		}
		return clients;
	}

	public void remove() {
	}
}
