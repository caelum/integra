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
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import org.hibernate.validator.Min;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.com.caelum.integracao.server.build.Tab;
import br.com.caelum.integracao.server.dao.Database;
import br.com.caelum.integracao.server.plugin.PluginToRun;
import br.com.caelum.integracao.server.queue.Job;
import br.com.caelum.integracao.server.queue.Jobs;
import br.com.caelum.integracao.server.scm.Revision;
import br.com.caelum.integracao.server.scm.ScmControl;
import br.com.caelum.integracao.server.scm.ScmException;

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

	@ManyToOne
	private Revision revision;

	@Min(0)
	private int currentPhase = 0;

	private boolean successSoFar = true;
	private boolean finished = false;

	private Calendar startTime = new GregorianCalendar();
	private Calendar finishTime;

	@OneToMany(mappedBy = "build")
	private List<Job> jobs = new ArrayList<Job>();
	
	@OneToMany(mappedBy="build")
	private List<Tab> tabs = new ArrayList<Tab>();

	protected Build() {
	}

	public Build(Project project) {
		this.project = project;
		this.buildCount = project.nextBuild();

		// clears the base directory before using it
		File baseDirectory = getBaseDirectory();
		remove(baseDirectory);
		baseDirectory.mkdirs();
	}

	public Revision getRevision() {
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

	public void start(Jobs jobs, Database db, Builds builds) throws ScmException {
		this.currentPhase = 0;
		logger.debug("Starting executing build for " + project.getName() + " at "
				+ project.getBaseDir().getAbsolutePath());
		try {
			ScmControl control = project.getControl();
			this.revision = project.extractNextRevision(this, builds, control);
		} catch (Exception ex) {
			logger.error("Unable to retrieve revision for " + project.getName(), ex);
			finish(false);
			return;
		}
		for (PluginToRun plugin : project.getPlugins()) {
			if (!plugin.getPlugin(db).before(this)) {
				logger.debug("Plugin " + plugin.getType().getName() + " told us to stop the build");
				finish(false);
				return;
			}
		}
		List<Phase> phases = project.getPhases();
		if (!phases.isEmpty()) {
			Phase phase = phases.get(0);
			phase.execute(this, jobs);
		}
	}

	private void finish(boolean success) {
		this.finished = true;
		this.successSoFar = success;
		this.finishTime = new GregorianCalendar();
	}

	public Project getProject() {
		return this.project;
	}

	public int getCurrentPhase() {
		return currentPhase;
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

	public List<Job> getJobsFor(Phase phase) {
		List<Job> clients = new ArrayList<Job>();
		for (Job client : jobs) {
			if (phase.getCommands().contains(client.getCommand())) {
				clients.add(client);
			}
		}
		return clients;
	}

	public void remove(Database database) {
		database.getSession().delete(this);
		remove(getBaseDirectory());
	}

	private void remove(File dir) {
		if (!dir.exists()) {
			return;
		}
		for (File found : dir.listFiles()) {
			if (found.isDirectory()) {
				remove(found);
			} else {
				found.delete();
			}
		}
		dir.delete();
	}

	public void failed() {
		this.successSoFar = false;
	}

	public void proceed(Phase actualPhase, Database database) throws IOException {
		List<Job> jobs = getJobsFor(actualPhase);
		int finished = 0;
		for (Job j : jobs) {
			if (j.isFinished()) {
				finished++;
			}
		}
		boolean executedAllCommands = finished == actualPhase.getCommandCount();
		if (executedAllCommands) {
			logger.debug("Preparing to execute plugins for " + getProject().getName() + " with success = "
					+ successSoFar);
			successSoFar &= actualPhase.runAfter(this, database);
			if (successSoFar) {
				currentPhase++;
				if (project.getPhases().size() != currentPhase) {
					project.getPhases().get(currentPhase).execute(this, new Jobs(database));
				} else {
					finish(true);
				}
			} else {
				finish(false);
			}
		}
	}

	public String getRevisionName() {
		if (revision == null) {
			return "unknown";
		}
		return revision.getName();
	}
	
	public List<Tab> getTabs() {
		return tabs;
	}

}
