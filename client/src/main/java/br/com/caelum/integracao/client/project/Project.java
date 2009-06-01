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
package br.com.caelum.integracao.client.project;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.com.caelum.integracao.CommandToExecute;

public class Project {

	private final Logger logger = LoggerFactory.getLogger(Project.class);
	private String name;

	private String uri;
	private CommandToExecute executing;

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setUri(String uri) {
		this.uri = uri;
	}

	public String getUri() {
		return uri;
	}

	public ProjectRunResult run(File baseDir, String revision, List<String> command, File tmp) throws IOException {
		File dir = new File(baseDir, name);
		FileWriter tmpOutput = new FileWriter(tmp);
		logger.debug("Checking out project @ " + uri + ", revision=" + revision + " to " + baseDir + "/" + name);
		this.executing = new CommandToExecute("svn", "checkout", "-r", revision, uri, name).at(baseDir);
		this.executing.run();

		String[] commands = command.toArray(new String[command.size()]);
		logger.debug("Ready to execute " + Arrays.toString(commands) + " @ " + dir.getAbsolutePath() + " using log=" + tmp.getAbsolutePath());
		this.executing = new CommandToExecute(commands).at(dir).logTo(tmpOutput);
		int result = this.executing.run();
		Scanner sc = new Scanner(new FileInputStream(tmp)).useDelimiter("117473826478234211");
		String content;
		if(!sc.hasNext()) {
			content = "";
		} else {
			content = sc.next();
		}
		return new ProjectRunResult(content, result);

	}

	public void stop() {
		if (this.executing != null) {
			executing.stop();
		}
	}

}
