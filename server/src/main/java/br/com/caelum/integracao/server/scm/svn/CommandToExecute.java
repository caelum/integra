package br.com.caelum.integracao.server.scm.svn;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;

public class CommandToExecute {

	private final String[] cmd;
	private File baseDir;

	public CommandToExecute(String... cmd) {
		this.cmd = cmd;
	}

	public CommandToExecute at(File baseDir) {
		this.baseDir = baseDir;
		return this;
	}

	public int runAs(String id) {
		ProcessBuilder builder = new ProcessBuilder();
		builder.directory(baseDir);
		builder.command(Arrays.asList(cmd));
		builder.redirectErrorStream(true);
		try {
			Process process = builder.start();
			AtomicBoolean stopFlag = new AtomicBoolean(false);
			Thread t = print(process, stopFlag, id);
			int result = process.waitFor();
			t.stop();
			return result;
		} catch (IOException e) {
			throw new RuntimeException(e);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}


	private Thread print(Process p, final AtomicBoolean stopFlag, final String prefix) {
		final InputStream inputStream = p.getInputStream();
		final InputStreamReader isr = new InputStreamReader(inputStream);
		final BufferedReader reader = new BufferedReader(isr);
		Thread t = new Thread(new Runnable() {
			public void run() {
				while(!stopFlag.get()) {
					try {
						while (reader.ready()) {
							System.out.println(prefix + ">> " + reader.readLine());
						}
					} catch (IOException e) {
						e.printStackTrace();
					}
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		});
		t.start();
		return t;
	}
}
