package br.com.caelum.integracao.server.scm.svn;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.Arrays;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicBoolean;

public class CommandToExecute {

	private final String[] cmd;
	private File baseDir;
	private PrintWriter outputWriter = new PrintWriter(System.out, true);
	private boolean closeWriter = false;

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
			int result = process.waitFor();
			InputStream is = process.getInputStream();
			Scanner sc = new Scanner(is).useDelimiter("\\n");
			while(sc.hasNext()) {
				outputWriter.println(id + ">> " + sc.next());
			}
			sc.close();
			stopFlag.set(true);
			if(closeWriter) {
				this.outputWriter.close();
			}
			return result;
		} catch (IOException e) {
			throw new RuntimeException(e);
		} catch (InterruptedException e) {
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
}
