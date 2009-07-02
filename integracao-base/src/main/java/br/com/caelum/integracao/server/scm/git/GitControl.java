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
package br.com.caelum.integracao.server.scm.git;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.com.caelum.integracao.command.CommandToExecute;
import br.com.caelum.integracao.server.scm.Revision;
import br.com.caelum.integracao.server.scm.ScmControl;
import br.com.caelum.integracao.server.scm.ScmException;

public class GitControl implements ScmControl {

	private final Logger logger = LoggerFactory.getLogger(GitControl.class);

	private final String uri;
	private final File baseDirectory;
	private final String baseName;

	public GitControl(String uri, File baseDirectory, String name) {
		this.uri = uri;
		this.baseDirectory = baseDirectory;
		this.baseName = name;
	}

	public int checkoutOrUpdate(String revision, PrintWriter log) throws ScmException {
		File dir = new File(baseDirectory, baseName);
		File git = new File(dir, ".git");
		if (dir.exists() && !git.exists()) {
			dir.delete();
		}
		if (git.exists()) {
			String cmds[][] = new String[][] { new String[] { "git", "checkout", "HEAD" },
					new String[] { "git", "checkout", "master" }, new String[] { "git", "pull" } };
			int partial = 0;
			for (String cmd[] : cmds) {
				partial = prepare(cmd).at(getDir()).logTo(log).run();
				if (partial != 0) {
					return partial;
				}
			}
			if (revision == null) {
				return partial;
			}
			return prepare("git", "checkout", revision).at(getDir()).logTo(log).run();
		} else {
			logger.debug("Cloning the git for the first time.");
			int partial = prepare("git", "clone", uri, baseName).at(baseDirectory).logTo(log).run();
			if (partial != 0 || revision == null) {
				return partial;
			}
			return prepare("git", "checkout", revision).at(getDir()).logTo(log).run();
		}
	}

	public File getDir() {
		return new File(baseDirectory, baseName);
	}

	public int add(File file) {
		return prepare("git", "add", file.getAbsolutePath()).at(file.getParentFile()).run();
	}

	private CommandToExecute prepare(String... cmd) {
		return new CommandToExecute(cmd);
	}

	public int commit(String message) {
		int localCommit = prepare("git", "commit", "-a", "-m", "'" + message.replace('\'', '\"') + "'").at(getDir())
				.run();
		if (localCommit != 0) {
			return localCommit;
		}
		return prepare("git", "push").at(getDir()).run();
	}

	public int remove(File file) {
		return prepare("git", "rm", file.getAbsolutePath()).at(file.getParentFile()).run();
	}

	public Revision getCurrentRevision(Revision fromRevision, PrintWriter log) throws ScmException {
		int result = checkoutOrUpdate(null, log);
		if (result != 0) {
			throw new ScmException("Unable to load data and revision information");
		}
		StringWriter writer = new StringWriter();
		prepare("git", "--no-pager", "log").logTo(writer).at(getDir()).run();
		String content = writer.getBuffer().toString();
		int pos = content.indexOf("commit ");
		String name = content.substring(pos + "commit ".length(), content.indexOf("\n", pos));
		return extractRevision(name, log, name);
	}

	private String extract(PrintWriter log, String... cmd) {
		StringWriter writer = new StringWriter();
		prepare(cmd).logTo(writer).at(getDir()).run();
		String content = writer.getBuffer().toString();
		log.println(Arrays.toString(cmd) + " resulted in: " + content);
		return content;
	}

	public Revision getNextRevision(Revision fromRevision, PrintWriter log) throws ScmException {
		int result = checkoutOrUpdate(null, log);
		if (result != 0) {
			throw new ScmException("Unable to load data and revision information.");
		}
		String revisionRange = "HEAD";
		if(fromRevision!=null) {
			revisionRange = fromRevision.getName() + "..HEAD";
		}
		String diff = extract(log, "git", "--no-pager", "log", revisionRange, "--shortstat");
		logger.debug("diff was " + diff);
		if (diff.indexOf("files changed") == -1) {
			// there was no change in the content
			return fromRevision;
		}

		log.println("\ndiff message was: " + diff + "\n");
		int start = diff.lastIndexOf("commit ", diff.lastIndexOf("Author:")) + "commit ".length();
		log.println("\nstart=\n" + start);
		String baseName = diff.substring(start, start + "0f367b2fd83947fe916c3a54da2a341e28b88d78".length());
		log.println("\ndiff basename is " +baseName + "\n");
		return extractRevision(baseName, log, baseName);
	}

	private String extractInfoForRevision(PrintWriter log, String revisionRange) throws ScmException {
		String logContent = extract(log, "git", "--no-pager", "log", revisionRange , "-v", "--date=iso");
		return logContent;
	}

	public String getIgnorePattern() {
		return ".*\\.git";
	}

	public Revision extractRevision(String name, PrintWriter log, String range) throws ScmException {
		String content = extractInfoForRevision(log, range + "^!");
		String date = content.substring(content.indexOf("Date:"), content.indexOf("\n",content.indexOf("Date:"))).trim().substring(3).trim();
		return new Revision(name, content, "", date);
	}

}
