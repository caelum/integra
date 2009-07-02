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
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import br.com.caelum.integracao.AtDirectoryTest;
import br.com.caelum.integracao.command.CommandToExecute;
import br.com.caelum.integracao.server.scm.Revision;
import br.com.caelum.integracao.server.scm.ScmException;

public class GitControlTest extends AtDirectoryTest {

	private File myGitDir;
	private PrintWriter log;

	@Before
	public void configGit() throws IOException, ScmException {
		this.myGitDir = new File(baseDir, "cruise");
		prepare("git", "init").at(myGitDir).run();
		File file = new File(myGitDir, "sample-file");
		givenA(file, "misc content\n");
		prepare("git", "add", file.getAbsolutePath()).at(myGitDir).run();
		prepare("git", "commit", "-m", "ha").at(myGitDir).run();

		this.log = new PrintWriter(new StringWriter());
	}

	private CommandToExecute prepare(String... cmd) {
		return new CommandToExecute(cmd);
	}

	@Test
	public void shouldCommitAndReceiveUpdate() throws IOException, ScmException {

		GitControl control1 = new GitControl(myGitDir.getAbsolutePath(), baseDir, "checking-in");
		Assert.assertEquals(0, control1.checkoutOrUpdate(null, log));
		File file = new File(control1.getDir(), "test-file");
		givenA(file, "second file content");

		GitControl control2 = new GitControl(myGitDir.getAbsolutePath(), baseDir, "checking-out");
		Assert.assertEquals(0, control2.checkoutOrUpdate(null, log));

		Assert.assertEquals(0, control1.add(file));
		Assert.assertEquals(0, control1.commit("commiting test file"));
		control2.checkoutOrUpdate(null, log);
		File found = new File(control2.getDir(), "test-file");
		Assert.assertTrue(found.exists());
		Assert.assertEquals("second file content", contentOf(found));
		control2.remove(found);
		control2.commit("removed test file");
		control1.checkoutOrUpdate(null, log);
		Assert.assertFalse(file.exists());
	}

	@Test
	public void shouldBeAbleToCheckoutHeadIfUsingNextRevisionWithNullRevision() throws ScmException {
		GitControl control1 = new GitControl(myGitDir.getAbsolutePath(), baseDir, "checking-in");
		Revision revision = control1.getNextRevision(null, log);
		File found = new File(control1.getDir(), "sample-file");
		Assert.assertTrue(found.exists());

		String expectedRevision = currentRevision();
		Assert.assertEquals(expectedRevision, revision.getName());

	}


	@Test
	public void shouldBeAbleToCheckoutNextIfUsingNextRevision() throws ScmException, IOException {
		GitControl control1 = new GitControl(myGitDir.getAbsolutePath(), baseDir, "checking-in");
		Assert.assertEquals(0, control1.checkoutOrUpdate(null, log));
		File file = new File(control1.getDir(), "test-file");
		givenA(file, "second file content");

		GitControl control2 = new GitControl(myGitDir.getAbsolutePath(), baseDir, "checking-out");
		Revision previous = control2.getNextRevision(null, log);

		Assert.assertEquals(0, control1.add(file));
		Assert.assertEquals(0, control1.commit("commiting test file"));

		Revision current = control2.getNextRevision(previous, log);

		String expectedRevision = currentRevision();
		Assert.assertEquals(expectedRevision, current.getName());

	}

	@Test
	public void shouldReturnTheSameRevisionIfNothingWasChanged() throws ScmException, IOException {
		GitControl control1 = new GitControl(myGitDir.getAbsolutePath(), baseDir, "checking-in");
		Assert.assertEquals(0, control1.checkoutOrUpdate(null, log));

		GitControl control2 = new GitControl(myGitDir.getAbsolutePath(), baseDir, "checking-out");
		Revision previous = control2.getNextRevision(null, log);
		Revision current = control2.getNextRevision(previous, log);
		Assert.assertEquals(previous, current);

	}

	private String currentRevision() {
		StringWriter writer = new StringWriter();
		PrintWriter temp = new PrintWriter(writer);
		Assert.assertEquals(0,prepare("git", "--no-pager", "log").at(myGitDir).logTo(temp).run());
		String content = writer.getBuffer().toString();
		content = content.substring(content.indexOf("\n")+1);
		return content.substring("commit ".length(), content.indexOf("\n"));
	}

}
