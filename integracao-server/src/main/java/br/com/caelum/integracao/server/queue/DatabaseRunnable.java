package br.com.caelum.integracao.server.queue;

import br.com.caelum.integracao.server.dao.Database;

/**
 * Runs a task with a database
 * @author adriano
 *
 */
public interface DatabaseRunnable {

	void run(Database db);

}
