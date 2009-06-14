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
package br.com.caelum.integracao.server.dao;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.AnnotationConfiguration;
import org.hibernate.stat.Statistics;
import org.hibernate.tool.hbm2ddl.SchemaExport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.com.caelum.integracao.server.Application;
import br.com.caelum.integracao.server.Config;
import br.com.caelum.integracao.server.plugin.RegisteredPlugin;
import br.com.caelum.integracao.server.plugin.build.RemoveOldBuildsInformation;
import br.com.caelum.integracao.server.plugin.junit.JUnitReportInformation;
import br.com.caelum.integracao.server.plugin.mail.SendMailInformation;
import br.com.caelum.vraptor.ioc.ApplicationScoped;

@ApplicationScoped
public class DatabaseFactory {

	private final Logger logger = LoggerFactory.getLogger(DatabaseFactory.class);

	private SessionFactory factory;

	@PostConstruct
	public void startup() {
		logger.debug("Starting up database");
		this.factory = new AnnotationConfiguration().configure().buildSessionFactory();
		Database db = new Database(this);
		try {
			if (new Application(db).getConfig() == null) {
				Config cfg = new Config();
				Session session = db.getSession();
				try {
					db.beginTransaction();
					logger.debug("Creating database for the first time");
					session.save(cfg);
					session.save(new RegisteredPlugin(cfg, SendMailInformation.class));
					session.save(new RegisteredPlugin(cfg, RemoveOldBuildsInformation.class));
					session.save(new RegisteredPlugin(cfg, JUnitReportInformation.class));
					db.commit();
				} finally {
					if (db.hasTransaction()) {
						db.rollback();
						logger
								.error("Was unable to insert the basic data in the database. Something really nasty will happen");
					}
				}
			}
		} finally {
			db.close();
		}
	}

	@PreDestroy
	public void destroy() {
		if (factory != null) {
			logger.debug("Shutting down database");
			this.factory.close();
		}
	}

	public Session getSession() {
		return this.factory.openSession();
	}

	public void clear() {
		// TODO  DANGER DANGER!!
		AnnotationConfiguration cfg = new AnnotationConfiguration().configure();
		new SchemaExport(cfg).create(false, true);
	}

	public Statistics getStatistics() {
		return factory.getStatistics();
	}

}
