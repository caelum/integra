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
package br.com.caelum.integracao.server.queue;

import java.util.Iterator;
import java.util.List;

import br.com.caelum.integracao.server.Client;
import br.com.caelum.integracao.server.Clients;
import br.com.caelum.integracao.server.Config;

public class DefaultJobQueue implements JobQueue{
	
	
	private final Jobs jobs;
	private final Clients clients;
	private final Config config;

	public DefaultJobQueue(Jobs jobs, Clients clients, Config config) {
		this.jobs = jobs;
		this.clients = clients;
		this.config = config;
	}

	public int iterate() {
		List<Job> todo = jobs.todo();
		int completed = 0;
		List<Client> freeFound = clients.freeClients();
		for(Job job : todo) {
			for (Iterator<Client> iterator = freeFound.iterator(); iterator.hasNext();) {
				Client client = iterator.next();
				if(client.canHandle(job.getCommand()) && client.work(job, config)) {
					completed++;
					break;
				}
			}
		}
		return completed;
	}

}
