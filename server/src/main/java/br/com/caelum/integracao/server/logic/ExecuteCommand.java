package br.com.caelum.integracao.server.logic;

import java.io.IOException;

import br.com.caelum.integracao.server.Client;
import br.com.caelum.integracao.server.scm.ScmControl;

public interface ExecuteCommand {

	void executeAt(Client client, ScmControl control) throws IOException;


}
