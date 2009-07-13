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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

import java.io.IOException;
import java.util.Arrays;

import org.jmock.Expectations;
import org.junit.Before;
import org.junit.Test;

import br.com.caelum.integracao.server.agent.Agent;
import br.com.caelum.integracao.server.agent.AgentControl;
import br.com.caelum.integracao.server.agent.AgentStatus;
import br.com.caelum.integracao.server.label.Label;
import br.com.caelum.integracao.server.project.BaseTest;
import br.com.caelum.integracao.server.queue.Job;

public class ClientTest extends BaseTest {

	private Job job;
	private Config config;
	private AgentControl control;
	private Agent agent;

	@Before
	public void configJob() {
		this.job = mockery.mock(Job.class);
		this.config = mockery.mock(Config.class);
		this.control = mockery.mock(AgentControl.class);
		this.agent = mockery.mock(Agent.class);
		mockery.checking(new Expectations() {
			{
				allowing(control).to("http://localhost:8080/integracao-client");
				will(returnValue(agent));
			}
		});
	}

	@Test
	public void shouldMakeItselfAsBusyWhenExecutingSomething() throws IOException {
		final Client c = new Client();
		mockery.checking(new Expectations() {
			{
				one(job).executeAt(c, config);
			}
		});
		assertThat(c.work(job, config), is(equalTo(true)));
		assertThat(c.getCurrentJob(), is(equalTo(job)));
		mockery.assertIsSatisfied();
	}

	@Test
	public void shouldNotMakeItselfAsBusyWhenExecutingSomethingWithAnError() throws IOException {
		final Client c = new Client();
		mockery.checking(new Expectations() {
			{
				one(job).executeAt(c,config);
				will(throwException(new RuntimeException()));
			}
		});
		assertThat(c.work(job, config), is(equalTo(false)));
		assertThat(c.getCurrentJob(), is(equalTo(null)));
		mockery.assertIsSatisfied();
	}

	@Test
	public void shouldFreeClientIfThoughItWasBusyButReturnedFree() {
		mockery.checking(new Expectations() {
			{
				one(agent).getStatus(); will(returnValue(AgentStatus.FREE));
			}
		});
		Client c = new Client();
		c.setCurrentJob(job);
		assertThat(c.isAlive(control), is(equalTo(true)));
		assertThat(c.getCurrentJob(), is(nullValue()));
		mockery.assertIsSatisfied();
	}

	@Test
	public void shouldMarkTheClientAsAliveIfWorking() {
		mockery.checking(new Expectations() {
			{
				one(agent).getStatus(); will(returnValue(AgentStatus.BUSY));
			}
		});
		Client c = new Client();
		c.setCurrentJob(job);
		assertThat(c.isAlive(control), is(equalTo(true)));
		assertThat(c.getCurrentJob(), is(equalTo(job)));
		mockery.assertIsSatisfied();
	}

	@Test
	public void shouldMarkTheClientAsDeadAndRemoveJobIfCannotConnect() {
		mockery.checking(new Expectations() {
			{
				one(agent).getStatus(); will(returnValue(AgentStatus.UNAVAILABLE));
			}
		});
		Client c = new Client();
		c.setCurrentJob(job);
		assertThat(c.isAlive(control), is(equalTo(false)));
		assertThat(c.getCurrentJob(), is(nullValue()));
		mockery.assertIsSatisfied();
	}

	@Test
	public void shouldMarkTheClientAsAliveIfReturning200() {
		mockery.checking(new Expectations() {
			{
				one(agent).getStatus(); will(returnValue(AgentStatus.FREE));
			}
		});
		Client c = new Client();
		assertThat(c.isAlive(control), is(equalTo(true)));
		mockery.assertIsSatisfied();
	}

	@Test
	public void shouldBeCapableToRunTheJobOnlyIfContainsAllLabelsAndIsAlive() {
		mockery.checking(new Expectations() {
			{
				exactly(2).of(agent).getStatus(); will(returnValue(AgentStatus.FREE));
			}
		});
		Label java = new Label("java");
		Label ant = new Label("ant");
		Label maven = new Label("mvn");
		Client c = new Client();
		c.tag(Arrays.asList(java,ant));
		assertThat(c.canHandle(new BuildCommand(new Phase(),new String[0], new String[0], Arrays.asList(java), ""), control), is(equalTo(true)));
		assertThat(c.canHandle(new BuildCommand(new Phase(),new String[0], new String[0], Arrays.asList(java, maven), ""), control), is(equalTo(false)));
		mockery.assertIsSatisfied();
	}

}
