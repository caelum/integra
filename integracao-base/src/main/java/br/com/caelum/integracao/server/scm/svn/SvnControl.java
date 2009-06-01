package br.com.caelum.integracao.server.scm.svn;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;

import br.com.caelum.integracao.CommandToExecute;
import br.com.caelum.integracao.server.scm.ScmControl;

public class SvnControl implements ScmControl {

	private final String uri;
	private final File baseDir;
	private String baseName;
	private File baseDirectory;

	public SvnControl(String uri, File baseDir, String baseName) {
		this.uri = uri;
		this.baseDir = baseDir;
		this.baseName = baseName;
		this.baseDirectory = new File(baseDir, baseName);
	}

	public int checkout(File log) throws IOException {
		return prepare("svn", "co", uri, baseName).at(baseDir).logTo(log).run();
	}

	public File getDir() {
		return baseDirectory;
	}

	public int add(File file) {
		return prepare("svn", "add", file.getAbsolutePath()).at(file.getParentFile()).run();
	}

	private CommandToExecute prepare(String ... cmd) {
		return new CommandToExecute(cmd);
	}

	public int commit(String message) {
		return prepare("svn", "commit", "-m", message).at(getDir()).run();
	}

	public int update() {
		return prepare("svn", "update").at(getDir()).run();
	}

	public int remove(File file) {
		return prepare("svn", "remove", file.getAbsolutePath()).at(file.getParentFile()).run();
	}

	public String getRevision() {
		StringWriter writer = new StringWriter();
		prepare("svn", "info").logTo(writer).at(getDir()).run();
		String content = writer.getBuffer().toString();
		int pos = content.indexOf("Last Changed Rev: ");
		return content.substring(pos+ "Last Changed Rev: ".length(), content.indexOf("\n", pos));
	}

}
