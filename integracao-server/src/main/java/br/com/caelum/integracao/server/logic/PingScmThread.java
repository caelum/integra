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
package br.com.caelum.integracao.server.logic;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.com.caelum.integracao.server.Application;
import br.com.caelum.integracao.server.Config;
import br.com.caelum.integracao.server.dao.Database;
import br.com.caelum.integracao.server.dao.DatabaseFactory;
import br.com.caelum.vraptor.ioc.ApplicationScoped;

/**
 * Pings the scm server once in a while.
 *
 * @author guilherme silveira
 */
@ApplicationScoped
public class PingScmThread {

	private final Logger logger = LoggerFactory.getLogger(PingScmThread.class);

	private Thread thread;

	private final DatabaseFactory factory;

	public PingScmThread(DatabaseFactory factory) {
		this.factory = factory;
	}

	@PostConstruct
	public void startup() {
		logger.debug("Starting up ping scm thread");
		this.thread = new Thread(new Runnable() {
			public void run() {
				while (true) {
					try {
						pingSystem();
					} catch (Exception ex) {
						logger.error("Something really nasty ocurred while pinging the scm servers", ex);
					}
				}
			}
		});
		thread.start();
	}

	public void pingSystem() {
		Database db = new Database(factory);
		try {
			Config config = new Application(db).getConfig();
			int time = config.getCheckInterval();
			db.close();
			if (time <= 0) {
				// no automatic pinging, thank you
				Thread.sleep(5 * 60 * 1000);
				return;
			}
			Thread.sleep(time * 1000);
			new PingScm().buildProjects(factory);
		} catch (Exception ex) {
			logger.error("Something really nasty ocurred while pinging the scm servers", ex);
		} finally {
			if (!db.isClosed()) {
				if (db.hasTransaction()) {
					db.rollback();
				}
				db.close();
			}
		}
	}

	@PreDestroy
	public void destroy() {
		if (thread != null && thread.isAlive()) {
			logger.debug("Shutting down ping thread");
			thread.stop();
		}
	}

}
