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

import java.util.List;

import org.hibernate.Query;
import org.hibernate.Session;

import br.com.caelum.integracao.server.client.Tag;
import br.com.caelum.integracao.server.dao.Database;
import br.com.caelum.vraptor.ioc.RequestScoped;

@RequestScoped
@SuppressWarnings("unchecked")
public class Clients {

	private final Session session;

	public Clients(Database database) {
		this.session = database.getSession();
	}

	public void register(Client client) {
		client.activate();
		this.session.save(client);
	}

	public List<Client> freeClients() {
		return this.session.createQuery("from Client as c where c.active = true and c.currentJob is null").list();
	}

	public List<Client> lockedClients() {
		return this.session.createQuery("from Client as c where c.currentJob is not null and c.active = true").list();
	}

	public List<Client> inactiveClients() {
		return this.session.createQuery("from Client as c where c.active = false").list();
	}

	public Client get(Client client) {
		return (Client) session.load(Client.class, client.getId());
	}

	public List<Tag> getTags() {
		return session.createQuery("from Tag").list();
	}

	public Tag getTag(String name) {
		Query query = session.createQuery("from Tag as t where t.name = :name");
		query.setParameter("name", name);
		List<Tag> results = query.list();
		if(results.isEmpty()) {
			Tag tag = new Tag(name);
			session.save(tag);
			return tag;
		}
		return results.get(0);
	}

}
