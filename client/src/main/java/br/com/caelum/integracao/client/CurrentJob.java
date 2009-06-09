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

import java.io.StringWriter;
import java.util.Calendar;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.com.caelum.integracao.client.project.Project;
import br.com.caelum.vraptor.ioc.ApplicationScoped;

@ApplicationScoped
public class CurrentJob {

	private final Logger logger = LoggerFactory.getLogger(CurrentJob.class);

	private Project project;
	private Thread thread;
	private final Settings settings;

	private Calendar start;

	private StringWriter output = new StringWriter();

	private String jobId;

	public CurrentJob(Settings settings) {
		this.settings = settings;
	}

	public boolean isRunning() {
		return project != null;
	}

	public Project getProject() {
		return project;
	}

	public Thread getThread() {
		return thread;
	}

	public synchronized void start(String jobId, final Project project, final String revision,
			final List<String> startCommand, final List<String> stopCommand, final String resultUri, final JobExecution execution) {
		if (isRunning()) {
			throw new RuntimeException("Cannot take another job as im currently processing " + this.project.getName());
		}
		this.jobId = jobId;
		this.start = Calendar.getInstance();
		this.project = project;
		Runnable runnable = new Runnable() {
			public void run() {
				try {
					output = new StringWriter();
					execution.executeBuildFor(revision, startCommand, stopCommand, resultUri, project, output, settings);
				} finally {
					clearThemAll();
				}
			}
		};
		this.thread = new Thread(runnable, project.getName() + " revision " + revision);
		thread.start();
	}

	private void clearThemAll() {
		output.getBuffer().delete(0, output.getBuffer().length());
		CurrentJob.this.jobId = null;
		CurrentJob.this.project = null;
		CurrentJob.this.thread = null;
		CurrentJob.this.start = null;
	}

	public synchronized boolean stop(String jobIdToStop) {
		logger.debug("Stopping job, looking for " + jobIdToStop);
		if (this.jobId == null) {
			logger.warn("Could not stop " + jobIdToStop + " because I am not running anything");
			return true;
		}
		if (!this.jobId.equals(jobIdToStop)) {
			logger.error("Could not stop " + jobIdToStop + " because I am running: " + this.jobId);
			return false;
		}
		if (this.thread != null) {
			this.thread.interrupt();
		}
		if (this.project != null) {
			this.project.stop();
		}
		return true;
	}

	public double getTime() {
		return (System.currentTimeMillis() - start.getTimeInMillis()) / 1000.0;
	}

	public Calendar getStart() {
		return start;
	}

	public String getOutputContent()  {
		return this.output.getBuffer().toString();
	}

}
