package br.com.caelum.integracao.server.scm.svn;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;

import br.com.caelum.integracao.command.CommandToExecute;
import br.com.caelum.integracao.server.scm.Revision;
import br.com.caelum.integracao.server.scm.ScmControl;
import br.com.caelum.integracao.server.scm.ScmException;

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

	public int checkoutOrUpdate(String revision, PrintWriter writer) throws ScmException {
		if (revision == null) {
			revision = "HEAD";
		}
		try {
			if (new File(baseDirectory, ".svn").exists()) {
				return update(revision, writer);
			} else {
				return prepare("svn", "co", uri, baseName, "-r", revision, "--non-interactive").at(baseDir).logTo(writer)
						.run();
			}
		} catch (IOException e) {
			throw new ScmException("Unable to checkout or update from svn " + uri, e);
		}
	}

	public File getDir() {
		return baseDirectory;
	}

	public int add(File file) {
		return prepare("svn", "add", file.getAbsolutePath()).at(file.getParentFile()).run();
	}

	private CommandToExecute prepare(String... cmd) {
		return new CommandToExecute(cmd);
	}

	public int commit(String message) {
		return prepare("svn", "commit", "-m", message).at(getDir()).run();
	}

	public int update(String revision, Writer writer) throws IOException {
		return prepare("svn", "update", "-r", revision, "--non-interactive").at(getDir()).logTo(writer).run();
	}

	public int remove(File file) {
		return prepare("svn", "remove", file.getAbsolutePath()).at(file.getParentFile()).run();
	}

	public Revision getCurrentRevision(Revision fromRevision, PrintWriter log) throws ScmException {
		String content = extract(log, "svn", "info", uri, "--non-interactive");
		int pos = content.indexOf("Last Changed Rev: ");
		String name = content.substring(pos + "Last Changed Rev: ".length(), content.indexOf("\n", pos));

		String from = "";
		if (fromRevision != null) {
			from = "" + (Long.parseLong(fromRevision.getName()) + 1) + ":";
		}
		String revisionRange = from + "HEAD";
		String logContent = extractInfoForRevision(log, revisionRange);
		return new Revision(name, logContent, "");
	}

	private String extractInfoForRevision(PrintWriter log, String revisionRange) throws ScmException {
		String logContent = extract(log, "svn", "log", uri, "-r", revisionRange, "-v", "--non-interactive");
		return logContent;
	}

	private String extract(PrintWriter log, String... cmd) throws ScmException {
		StringWriter writer = new StringWriter();
		prepare(cmd).logTo(writer).at(getDir()).run();
		String content = writer.getBuffer().toString();
		log.print(content);
		return content;
	}

	public Revision getNextRevision(Revision fromRevision, PrintWriter log) throws ScmException {
		
		String checkRange = (Long.parseLong(fromRevision.getName())+1)+ ":HEAD";
		String diff = extract(log, "svn", "log", uri, "-r", checkRange, "-v", "--non-interactive");
		if(diff.indexOf("Changed paths:")==-1) {
			// there was no change in the content
			return fromRevision;
		}
		
		int start = diff.lastIndexOf("r", diff.indexOf("|")) +1;
		int end = diff.indexOf( " ", start);
		String name = diff.substring(start,end);
		Revision nextRevision = new Revision(name, extractInfoForRevision(log, name), "");
		return nextRevision;
		
	}

	public String getIgnorePattern() {
		return ".*\\.svn";
	}
	
}
