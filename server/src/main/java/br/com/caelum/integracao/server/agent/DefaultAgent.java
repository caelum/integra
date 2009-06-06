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
package br.com.caelum.integracao.server.agent;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.com.caelum.integracao.http.DefaultHttp;
import br.com.caelum.integracao.http.Method;
import br.com.caelum.integracao.server.queue.Job;

/**
 * Default implementation of an agent.
 * 
 * @author guilherme silveira
 */
public class DefaultAgent implements Agent {

	private static final int CONFLICT = 409;

	private static final int GONE = 410;

	private final Logger logger = LoggerFactory.getLogger(DefaultAgent.class);

	private final String baseUri;

	public DefaultAgent(String baseUri) {
		this.baseUri = baseUri;
	}

	public AgentStatus getStatus() {
		Method post = new DefaultHttp().post(baseUri + "/job/current");
		try {
			try {
				post.send();
			} catch (IOException e) {
				logger.debug("Setting the agent as unavailable.", e);
				return AgentStatus.UNAVAILABLE;
			}
			if (post.getResult() == GONE) {
				return AgentStatus.FREE;
			}
			if (post.getResult() != 200) {
				return AgentStatus.UNAVAILABLE;
			}
			return AgentStatus.BUSY;
		} finally {
			post.close();
		}
	}

	public boolean stop(Job currentJob) {
		Method post = new DefaultHttp().post(baseUri + "/job/stop/");
		post.with("jobId", ""+ currentJob.getId());
		try {
			try {
				post.send();
			} catch (IOException e) {
				logger.debug("Could not stop the job.", currentJob.getId());
				return false;
			}
			if (post.getResult() != 200) {
				logger.debug("Could not stop the job.", currentJob.getId());
				return false;
			}
			return true;
		} finally {
			post.close();
		}
	}

}
