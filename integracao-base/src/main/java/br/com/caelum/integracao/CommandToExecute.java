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
package br.com.caelum.integracao;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.Arrays;
import java.util.Scanner;

public class CommandToExecute {

	private final String[] cmd;
	private File baseDir;
	private PrintWriter outputWriter = new PrintWriter(System.out, true);
	private boolean closeWriter = false;
	private Thread waitThread;
	private Thread outputThread;
	private Process process;

	public CommandToExecute(String... cmd) {
		this.cmd = cmd;
	}

	public CommandToExecute at(File baseDir) {
		this.baseDir = baseDir;
		return this;
	}

	public int run() {
		ProcessBuilder builder = new ProcessBuilder();
		baseDir.mkdirs();
		builder.directory(baseDir);
		builder.command(Arrays.asList(cmd));
		builder.redirectErrorStream(true);
		try {
			this.process = builder.start();
			Runnable waitRun = new Runnable() {
				public void run() {
					try {
						process.waitFor();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			};
			this.waitThread = new Thread(waitRun);
			waitThread.start();
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
			outputThread.start();
			waitThread.join();
			outputThread.join();
			if (closeWriter) {
				this.outputWriter.close();
			}
			return process.exitValue();
		} catch (IOException e) {
			throw new RuntimeException(e);
		} catch (InterruptedException e) {
			// someone told me to STOP this job, ok ,please stop it now!!!
			throw new RuntimeException(e);
		}
	}

	public CommandToExecute logTo(File build) throws IOException {
		return logTo(new FileWriter(build));
	}

	public CommandToExecute logTo(Writer writer) {
		this.outputWriter = new PrintWriter(writer, true);
		this.closeWriter = true;
		return this;
	}

	public void stop() {
		if (this.waitThread != null) {
			this.waitThread.stop();
		}
		if (this.outputThread != null) {
			this.outputThread.stop();
		}
		if (this.process != null) {
			this.process.destroy();
		}
	}
}
