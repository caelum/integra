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
package br.com.caelum.integracao.server.command.remote;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.com.caelum.integracao.server.Client;
import br.com.caelum.integracao.server.action.Dispatcher;
import br.com.caelum.integracao.server.command.ExecuteCommand;
import br.com.caelum.integracao.server.project.Build;
import br.com.caelum.integracao.server.scm.ScmControl;

public class ExecuteCommandLine implements ExecuteCommand {

	private final Logger logger = LoggerFactory.getLogger(ExecuteCommandLine.class);

	private final String[] cmd;

	private final int phaseCount;

	private final int position;

	private final String myUrl;

	public ExecuteCommandLine(String myUrl, int phaseCount, int commandCount, String... cmd) {
		this.myUrl = myUrl;
		this.phaseCount = phaseCount;
		this.position = commandCount;
		this.cmd = cmd;
	}

	public void executeAt(Client client, Build build, ScmControl control, File logFile) throws IOException {
		logger.debug("Trying to execute " + Arrays.toString(cmd) + " @ " + client.getHost() + ":" + client.getPort());
		Dispatcher connection = client.getConnection(logFile, myUrl);
		try {
			connection.register(build.getProject()).execute(build,phaseCount,position, cmd).close();
		} finally {
			connection.close();
		}
	}

	public String getName() {
		return Arrays.toString(cmd);
	}
	
	public int getPosition() {
		return position;
	}

}
