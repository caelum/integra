package br.com.caelum.integracao.server.queue;

import br.com.caelum.integracao.server.dao.Database;
import br.com.caelum.integracao.server.dao.DatabaseFactory;

/**
 * Runs tasks in other threads.
 * @author adriano
 */
public class OtherThreadRunner {

	private final DatabaseRunnable run;
	private final DatabaseFactory factory;
	public OtherThreadRunner(DatabaseFactory factory,DatabaseRunnable run) {
		this.factory = factory;
		this.run = run;
	}

	public void start() {
		Runnable execution = new Runnable() {
			public void run() {
				Database db = new Database(factory);
				try {
					run.run(db);
				} finally {
					if (db.hasTransaction()) {
						db.rollback();
					}
					db.close();
				}
			}
		};
		Thread thread = new Thread(execution);
		thread.start();
	}

}
