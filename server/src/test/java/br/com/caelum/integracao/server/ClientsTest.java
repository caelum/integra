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

import org.junit.Before;
import org.junit.Test;

import br.com.caelum.integracao.server.project.DatabaseBasedTest;
import br.com.caelum.integracao.server.queue.Job;
import br.com.caelum.integracao.server.queue.Jobs;

public class ClientsTest extends DatabaseBasedTest{

	private Clients clients;
	private Client busy;
	private Client free;
	private Client inactive;

	@Before
	public void configClients() {
		this.clients = new Clients(database);
		
		Job job = new Job(null,null);
		new Jobs(database).add(job);

		this.busy = new Client();
		busy.setCurrentJob(job);
		clients.register(busy);
		
		this.free = new Client();
		clients.register(free);
		
		this.inactive = new Client();
		clients.register(inactive);
		this.inactive.deactivate();
		
		database.getSession().flush();
	}

	@Test
	public void shouldReturnOnlyFreeClients() {
		assertThat(clients.freeClients().contains(free), is(equalTo(true)));
		assertThat(clients.freeClients().contains(inactive), is(equalTo(false)));
		assertThat(clients.freeClients().contains(busy), is(equalTo(false)));
	}

	@Test
	public void shouldReturnOnlyInactiveClients() {
		assertThat(clients.inactiveClients().contains(free), is(equalTo(false)));
		assertThat(clients.inactiveClients().contains(inactive), is(equalTo(true)));
		assertThat(clients.inactiveClients().contains(busy), is(equalTo(false)));
	}

	@Test
	public void shouldReturnOnlyBusyClients() {
		assertThat(clients.lockedClients().contains(free), is(equalTo(false)));
		assertThat(clients.lockedClients().contains(inactive), is(equalTo(false)));
		assertThat(clients.lockedClients().contains(busy), is(equalTo(true)));
	}

}
