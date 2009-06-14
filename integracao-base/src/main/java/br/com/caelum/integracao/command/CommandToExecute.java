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
package br.com.caelum.integracao.command;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CommandToExecute {

	private final Logger logger = LoggerFactory.getLogger(CommandToExecute.class);

	private final String[] cmd;
	private File baseDir;
	private PrintWriter outputWriter = new PrintWriter(System.out, true);
	private boolean closeWriter = false;
	private Thread waitThread;
	private Thread outputThread;
	private Process process;

	private Status status = Status.RUN;

	public CommandToExecute(String... cmd) {
		this.cmd = cmd;
	}

	public CommandToExecute at(File baseDir) {
		this.baseDir = baseDir;
		return this;
	}

	public int run() {
		List<String> commands = Arrays.asList(cmd);
		logger.debug("Ready to execute " + commands);
		ProcessBuilder builder = new ProcessBuilder();
		baseDir.mkdirs();
		builder.directory(baseDir);
		builder.command(commands);
		builder.redirectErrorStream(true);
		try {
			this.process = builder.start();
			Runnable waitRun = new Runnable() {
				public void run() {
					try {
						process.waitFor();
					} catch (InterruptedException e) {
						logger.debug("Stopped the process during the run", e);
					}
				}
			};
			this.waitThread = new Thread(waitRun);
			if (status == Status.RUN) {
				waitThread.start();
			}
			Runnable outputRun = new Runnable() {
				public void run() {
					InputStream is = process.getInputStream();
					Scanner sc = new Scanner(is).useDelimiter("\\n");
					while (sc.hasNext()) {
						outputWriter.println(sc.next());
						outputWriter.flush();
					}
					sc.close();
				}
			};
			this.outputThread = new Thread(outputRun);
			if (status == Status.RUN) {
				outputThread.start();
			}
			if (status == Status.RUN) {
				waitThread.join();
			}
			if (status == Status.RUN) {
				outputThread.join();
			}
			if(status==Status.RUN) {
				return process.exitValue();
			} else {
				// stopped by hand!
				return -1;
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		} catch (InterruptedException e) {
			// someone told me to STOP this job, ok ,please stop it now!!!
			throw new RuntimeException(e);
		} finally {
			if (closeWriter) {
				this.outputWriter.close();
			}
		}
	}

	public CommandToExecute logTo(Writer writer) {
		this.outputWriter = new PrintWriter(writer, true);
		this.closeWriter = true;
		return this;
	}

	public void stop() {
		this.status = Status.STOP;
		if (this.waitThread != null) {
			logger.debug("Interrupting wait thread");
			this.waitThread.interrupt();
		}
		if (this.outputThread != null) {
			logger.debug("Interrupting output thread");
			this.outputThread.interrupt();
		}
		if (this.process != null) {
			logger.debug("Destroying process " + process);
			this.process.destroy();
		}
	}

	enum Status {
		RUN, STOP
	}
	
	public String getName() {
		return Arrays.toString(cmd);
	}
}
