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

import java.util.HashSet;
import java.util.Set;

import javax.annotation.PostConstruct;

import br.com.caelum.vraptor.ioc.ApplicationScoped;

@ApplicationScoped
public class Clients {

	private final Set<Client> lockedClients = new HashSet<Client>();

	private final Set<Client> clients = new HashSet<Client>();

	private long uniqueCount = 0;

	@PostConstruct
	public void base() {
		/*Client c = new Client();
		c.setContext("/integracao-client");
		c.setHost("localhost");
		c.setPort(8080);
		register(c);*/
	}

	public synchronized void register(Client client) {
		client.setId(++uniqueCount);
		this.clients.add(client);
	}

	public Set<Client> clients() {
		return this.clients;
	}

	public synchronized Client getFreeClient() {
		if (clients.isEmpty()) {
			throw new IllegalStateException("There are not enough clients");
		}
		Client client = clients.iterator().next();
		clients.remove(client);
		lockedClients.add(client);
		return client;
	}

	public synchronized void release(Long id) {
		for (Client c : lockedClients) {
			if (c.getId().equals(id)) {
				lockedClients.remove(c);
				clients.add(c);
				break;
			}
		}
	}

}
