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
package br.com.caelum.integracao.server;

import java.io.File;
import java.io.IOException;

import org.jmock.Expectations;
import org.junit.Test;

import br.com.caelum.integracao.server.log.LogFile;
import br.com.caelum.integracao.server.project.BaseTest;
import br.com.caelum.integracao.server.scm.Revision;
import br.com.caelum.integracao.server.scm.ScmException;
import br.com.caelum.integracao.server.scm.svn.SvnControl;
import static org.hamcrest.MatcherAssert.assertThat;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

public class ProjectTest extends BaseTest {

	@Test
	public void newlyCreatedProjectsHaveBuildCountZero() {
		Project p = new Project();
		assertThat(p.getBuildCount(), is(equalTo(0L)));
	}

	@Test
	public void buildCreatesANewBuild() {
		Project p = new Project();
		p.setBuildCount(10L);
		Build build = p.build();
		assertThat(build.getBuildCount(), is(equalTo(11L)));
		assertThat(p.getBuildCount(), is(equalTo(11L)));
		assertThat(build.getProject(), is(equalTo(p)));
	}

	@Test
	public void setsAPhaseCountWhenAddingANewPhase() {
		Project p = new Project();
		Phase first = new Phase();
		p.add(first);
		assertThat(first.getPosition(), is(equalTo(0L)));
		Phase second = new Phase();
		p.add(second);
		assertThat(second.getPosition(), is(equalTo(1L)));
	}

	@Test
	public void setsTheProjectWhenAddingANewPhase() {
		Project p = new Project();
		Phase first = new Phase();
		p.add(first);
		assertThat(first.getProject(), is(equalTo(p)));
	}

	@Test
	public void findsABuildByItsBuildCount() {
		Project p = new Project();
		Build first = p.build();
		Build second = p.build();
		assertThat(p.getBuild(first.getBuildCount()), is(equalTo(first)));
		assertThat(p.getBuild(second.getBuildCount()), is(equalTo(second)));
		assertThat(p.getBuild(first.getBuildCount() + 5), is(equalTo(null)));
	}

	@Test
	public void useBuildsDirectoryAndBuildsIt() {
		Project p = new Project();
		p.setBaseDir(baseDir);
		File builds = p.getBuildsDirectory();
		assertThat(builds, is(equalTo(new File(baseDir, "builds"))));
		assertThat(builds.exists(), is(equalTo(true)));
	}

	@Test
	public void returnsTheSameRevisionIfThereIsNoNewRevision() throws IOException, ScmException {
		final Revision old = mockery.mock(Revision.class);
		final SvnControl control = mockery.mock(SvnControl.class);
		final Builds builds = mockery.mock(Builds.class);
		final LogFile log = new LogFile(new File(baseDir, "tmp.log"));
		final Project p = new Project();
		p.setLastRevisionBuilt(old);
		mockery.checking(new Expectations() {
			{
				one(control).getNextRevision(old, log.getWriter()); will(returnValue(old));
				allowing(old).getName(); will(returnValue("revision-name"));
				one(builds).contains(p, "revision-name"); will(returnValue(old));
			}
		});
		Revision found = p.extractNextRevision( control, builds, log);
		assertThat(found, is(equalTo(old)));
		mockery.assertIsSatisfied();
	}


	@Test
	public void savesTheNewRevisionIfThereIsANewRevision() throws IOException, ScmException {
		final Revision old = mockery.mock(Revision.class);
		final Revision next = mockery.mock(Revision.class, "next");
		final SvnControl control = mockery.mock(SvnControl.class);
		final Builds builds = mockery.mock(Builds.class);
		final LogFile log = new LogFile(new File(baseDir, "tmp.log"));
		final Project p = new Project();
		p.setLastRevisionBuilt(old);
		mockery.checking(new Expectations() {
			{
				one(control).getNextRevision(old, log.getWriter()); will(returnValue(next));
				allowing(next).getName(); will(returnValue("revision-name"));
				one(builds).contains(p, "revision-name"); will(returnValue(null));
				one(builds).register(next);
			}
		});
		Revision found = p.extractNextRevision(control, builds, log);
		assertThat(found, is(equalTo(next)));
		mockery.assertIsSatisfied();
	}


	@Test
	public void asksForCurrentRevisionIfNotSupposedToBuildEveryRevision() throws IOException, ScmException {
		final Revision old = mockery.mock(Revision.class);
		final Revision next = mockery.mock(Revision.class, "next");
		final SvnControl control = mockery.mock(SvnControl.class);
		final Builds builds = mockery.mock(Builds.class);
		final LogFile log = new LogFile(new File(baseDir, "tmp.log"));
		final Project p = new Project();
		p.setLastRevisionBuilt(old);
		mockery.checking(new Expectations() {
			{
				one(control).getCurrentRevision(old, log.getWriter()); will(returnValue(next));
				allowing(next).getName(); will(returnValue("revision-name"));
				one(builds).contains(p, "revision-name"); will(returnValue(null));
				one(builds).register(next);
			}
		});
		Revision found = p.extractNextRevision(control, builds, log);
		assertThat(found, is(equalTo(next)));
		mockery.assertIsSatisfied();
	}

}
