package br.com.caelum.integracao.server.scm.svn;

import java.io.File;
import java.io.StringWriter;

import br.com.caelum.integracao.CommandToExecute;
import br.com.caelum.integracao.server.scm.ScmControl;

public class SvnControl implements ScmControl {

	private final String uri;
	private final File baseDir;
	private String baseName;
	private File baseDirectory;
	private final File buildDirectory;

	public SvnControl(String uri, File baseDir, String baseName, File buildDir) {
		this.uri = uri;
		this.baseDir = baseDir;
		this.baseName = baseName;
		this.buildDirectory = buildDir;
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

	public File getBuildFileForCurrentRevision(String name) {
		String revision = getRevision();
		File revisionDirectory = new File(this.buildDirectory, "build-" + revision);
		revisionDirectory.mkdirs();
		return new File(revisionDirectory, name);
	}

	public String getRevision() {
		StringWriter writer = new StringWriter();
		prepare("svn", "info").logTo(writer).at(getDir()).runAs("svn-info");
		String content = writer.getBuffer().toString();
		int pos = content.indexOf("Last Changed Rev: ");
		return content.substring(pos+ "Last Changed Rev: ".length(), content.indexOf("\n", pos));
	}

}
