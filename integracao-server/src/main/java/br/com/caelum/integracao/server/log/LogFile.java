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
package br.com.caelum.integracao.server.log;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LogFile {

	private final Logger logger = LoggerFactory.getLogger(LogFile.class);

	private final FileWriter output;
	private final PrintWriter writer;
	private boolean closed = false;

	public LogFile(File file) throws IOException {
		this.output = new FileWriter(file);
		this.writer = new PrintWriter(output, true);
	}

	public LogFile(PrintWriter log) {
		this.output = null;
		this.writer = log;
		this.closed = true;
	}

	public void error(String msg, Throwable ex) {
		writer.write("[error]" + msg);
		ex.printStackTrace(writer);
	}

	synchronized public void close() {
		closed = true;
		writer.flush();
		writer.close();
		try {
			output.close();
		} catch (IOException e) {
			logger.error("Unable to close log file", e);
		}
	}

	public void error(String msg) {
		writer.write("[error]" + msg);
	}

	synchronized protected void finalize() throws Throwable {
		if (!closed) {
			close();
		}
	}

	public PrintWriter getWriter() {
		return writer;
	}

}
