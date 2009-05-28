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

import java.io.File;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.com.caelum.integracao.server.command.ExecuteCommand;
import br.com.caelum.integracao.server.project.Build;
import br.com.caelum.integracao.server.scm.ScmControl;

/**
 * A build consists of many different phases. At each phase some targets are
 * executed. ExecuteCommands within phases might be parallellized.
 * 
 * @author guilherme silveira
 */
public class Phase {

	private final Logger logger = LoggerFactory.getLogger(Phase.class);
	private final ExecuteCommand[] commands;
	private final String id;
	private final int phasePosition;

	public Phase(int phasePosition, String id, ExecuteCommand... cmds) {
		this.phasePosition = phasePosition;
		this.id = id;
		this.commands = cmds;
	}

	public void execute(ScmControl control, Build build, Clients clients) throws IOException {
		if (logger.isDebugEnabled()) {
			logger.debug("Starting phase " + id + " for project " + build.getProject().getName() + " containing "
					+ commands.length + " parallel commands.");
		}
		build.getFile(phasePosition + "/" + id).mkdirs();
		for (ExecuteCommand command : commands) {
			Client client = clients.getFreeClient();
			command.executeAt(client, build, control, File.createTempFile("connection", "txt"));
		}
	}

	public int getCommandCount() {
		return commands.length;
	}

}
