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
	private final String baseName;
	private final File baseDirectory;

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

	public Revision getCurrentRevision(Revision revision, PrintWriter log) throws ScmException {
		String content = extract(log, "svn", "info", uri, "--non-interactive");
		int pos = content.indexOf("Last Changed Rev: ");
		String name = content.substring(pos + "Last Changed Rev: ".length(), content.indexOf("\n", pos));

		String from = "";
		if (revision != null) {
			from = "" + (Long.parseLong(revision.getName()) + 1) + ":";
		}
		return extractRevision(name, log, from + "HEAD");
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

	public Revision getNextRevision(Revision revision, PrintWriter log) throws ScmException {

		String checkRange = (Long.parseLong(revision.getName())+1)+ ":HEAD";
		String diff = extract(log, "svn", "log", uri, "-r", checkRange, "-v", "--non-interactive");
		if(diff.indexOf("Changed paths:")==-1) {
			// there was no change in the content
			return revision;
		}

		int start = diff.lastIndexOf("r", diff.indexOf("|")) +1;
		int end = diff.indexOf( " ", start);
		String name = diff.substring(start,end);
		Revision nextRevision = extractRevision(name, log, name);
		return nextRevision;

	}

	public Revision extractRevision(String name, PrintWriter log, String range) throws ScmException {
		return new Revision(name, extractInfoForRevision(log, range), "", "");
	}

	public String getIgnorePattern() {
		return ".*\\.svn";
	}

}
