package br.com.caelum.integracao.server.scm.svn;

import java.io.File;

public class SvnControl {

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

	public int checkout() {
		return prepare("svn", "co", uri, baseName).at(baseDir).runAs("svn-checkout");
	}

	public File getDir() {
		return baseDirectory;
	}

	public int add(File file) {
		return prepare("svn", "add", file.getAbsolutePath()).at(file.getParentFile()).runAs("svn-add");
	}

	private CommandToExecute prepare(String ... cmd) {
		return new CommandToExecute(cmd);
	}

	public int commit(String message) {
		return prepare("svn", "commit", "-m", message).at(getDir()).runAs("svn-commit");
	}

	public int update() {
		return prepare("svn", "update").at(getDir()).runAs("svn-update");
	}

	public int remove(File file) {
		return prepare("svn", "remove", file.getAbsolutePath()).at(file.getParentFile()).runAs("svn-add");
	}

}
