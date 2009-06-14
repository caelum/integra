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

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.com.caelum.integracao.http.DefaultHttp;
import br.com.caelum.integracao.server.agent.Agent;
import br.com.caelum.integracao.server.agent.AgentControl;
import br.com.caelum.integracao.server.agent.AgentStatus;
import br.com.caelum.integracao.server.agent.DefaultAgent;
import br.com.caelum.integracao.server.label.Label;
import br.com.caelum.integracao.server.queue.Job;

/**
 * Represents a client machine.
 * 
 * @author guilherme silveira
 */
@Entity
public class Client {

	private static final Logger logger = LoggerFactory.getLogger(Client.class);

	@Id
	@GeneratedValue
	private Long id;

	private int port = 8080;

	private String context = "/integracao-client";

	private String host = "localhost";

	@ManyToOne
	private Job currentJob;

	@ManyToMany
	private List<Label> labels = new ArrayList<Label>();

	private boolean active;

	public String getBaseUri() {
		return "http://" + this.getHost() + ":" + this.getPort() + this.getContext();
	}

	public Agent getAgent() throws UnknownHostException, IOException {
		return new DefaultAgent(this.getBaseUri(), new DefaultHttp());
	}

	public String getContext() {
		return context;
	}

	public String getHost() {
		return host;
	}

	public Long getId() {
		return id;
	}

	public int getPort() {
		return port;
	}

	public void setContext(String context) {
		this.context = context;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public boolean work(Job job, Config config) {
		try {
			if (job.executeAt(this, config)) {
				this.currentJob = job;
				return true;
			}
			return false;
		} catch (Exception e) {
			logger.error("Unable to start the job at this client: " + getId(), e);
			return false;
		}
	}

	public void deactivate() {
		this.active = false;
	}

	public void activate() {
		this.active = true;
	}

	public boolean isAlive(AgentControl control) {
		Agent agent = control.to(getBaseUri());
		AgentStatus status = agent.getStatus();
		if (status.equals(AgentStatus.UNAVAILABLE)) {
			weirdJobMightNotBeThere();
			return false;
		}
		if (status.equals(AgentStatus.FREE)) {
			weirdJobMightNotBeThere();
			return true;
		}
		return true;
	}

	private void weirdJobMightNotBeThere() {
		if (currentJob != null) {
			logger.error("Leaving the job because the server just told me there is nothing running there..."
					+ "Did the client break or was it sending me the info right now?");
			currentJob.reschedule();
			currentJob = null;
		}
	}

	public void leaveJob() {
		this.currentJob = null;
	}

	public Job getCurrentJob() {
		return currentJob;
	}

	public List<Label> getLabels() {
		return labels;
	}

	public void tag(List<Label> list) {
		this.labels.addAll(list);
	}

	void setCurrentJob(Job currentJob) {
		this.currentJob = currentJob;
	}

	/**
	 * Will tell the client to stop this job. If the client returns a 200 its
	 * suposed to call the server back telling what hapenned to the job.
	 */
	public boolean stop(Agent agent) {
		if (agent.stop(currentJob)) {
			logger.debug("Successfully stopped the job after being requested to do so.");
			this.currentJob = null;
			return true;
		}
		return false;
	}

	public boolean canHandle(BuildCommand command, AgentControl control) {
		return isAlive(control) && this.labels.containsAll(command.getLabels());
	}

	public boolean isAlive() {
		return isAlive(new AgentControl());
	}

}
