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
package br.com.caelum.integracao.server.project;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.jmock.Expectations;
import org.junit.Before;
import org.junit.Test;

import br.com.caelum.integracao.server.Clients;
import br.com.caelum.integracao.server.Phase;
import br.com.caelum.integracao.server.Project;
import br.com.caelum.integracao.server.scm.ScmControl;

public class BuildTest extends BaseTest {

	private Project project;
	private Clients clients;
	private List<Phase> phases;
	private ScmControl control;
	private Phase first;
	private Phase second;

	@Before
	public void configProject() throws InstantiationException, IllegalAccessException, InvocationTargetException,
			NoSuchMethodException {
		this.project = mockery.mock(Project.class);
		this.clients = mockery.mock(Clients.class);
		this.phases = new ArrayList<Phase>();
		this.control = mockery.mock(ScmControl.class, "control");
		this.first = mockery.mock(Phase.class, "first");
		this.second = mockery.mock(Phase.class, "second");
		mockery.checking(new Expectations() {
			{
				allowing(project).getControl();
				will(returnValue(control));
				allowing(project).getPhases();
				will(returnValue(phases));
			}
		});
	}

	@Test
	public void createsABuildWithCorrectBuildCount() {
		mockery.checking(new Expectations() {
			{
				one(project).nextBuild();
				will(returnValue(3L));
				one(project).getBuildsDirectory();
				will(returnValue(baseDir));
			}
		});
		Build build = new Build(project);
		assertThat(build.getBuildCount(), is(equalTo(3L)));
		File dir = new File(baseDir, "build-3");
		assertThat(dir.exists(), is(equalTo(true)));
		mockery.assertIsSatisfied();
	}

	@Test
	public void createsABuildWithCorrectCheckoutInfo() throws IllegalArgumentException, SecurityException,
			InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException,
			IOException {
		mockery.checking(new Expectations() {
			{
				one(project).nextBuild();
				will(returnValue(3L));
				allowing(project).getName();
				will(returnValue("my-horses"));
				one(control).checkout((File) with(an(File.class)));
				one(control).getRevision();
				will(returnValue("my-revision"));
				allowing(project).getBuildsDirectory();
				will(returnValue(baseDir));
			}
		});
		Build build = new Build(project);
		build.start(clients);
		assertThat(build.getRevision(), is(equalTo("my-revision")));
		File checkout = new File(baseDir, "build-3/checkout");
		assertThat(checkout.exists(), is(equalTo(true)));
		mockery.assertIsSatisfied();
	}

	@Test
	public void keepsTheCurrentPhaseIfNoSuccess() throws InstantiationException, IllegalAccessException,
			InvocationTargetException, NoSuchMethodException, IOException {
		phases.add(first);
		phases.add(second);
		mockery.checking(new Expectations() {
			{
				one(project).nextBuild();
				will(returnValue(3L));
				allowing(project).getName();
				will(returnValue("my-horses"));
				one(control).checkout((File) with(an(File.class)));
				one(control).getRevision();
				will(returnValue("my-revision"));
				allowing(project).getBuildsDirectory();
				will(returnValue(baseDir));
				one(first).getCommandCount(); will(returnValue(1));
			}
		});
		final Build build = new Build(project);
		mockery.checking(new Expectations() {
			{
				one(first).execute(control, build, clients);
			}
		});
		build.start(clients);
		assertThat(build.getCurrentPhase(), is(equalTo(0)));
		build.finish(0, 0, "no-result", false, clients);
		assertThat(build.getCurrentPhase(), is(equalTo(0)));
		mockery.assertIsSatisfied();
	}

	@Test
	public void keepsTheCurrentPhaseIfThereAreStillCommandsToDo() throws InstantiationException,
			IllegalAccessException, InvocationTargetException, NoSuchMethodException, IOException {
		phases.add(first);
		phases.add(second);
		mockery.checking(new Expectations() {
			{
				one(project).nextBuild();
				will(returnValue(3L));
				allowing(project).getName();
				will(returnValue("my-horses"));
				one(control).checkout((File) with(an(File.class)));
				one(control).getRevision();
				will(returnValue("my-revision"));
				allowing(project).getBuildsDirectory();
				will(returnValue(baseDir));
				one(first).getCommandCount(); will(returnValue(2));
			}
		});
		final Build build = new Build(project);
		mockery.checking(new Expectations() {
			{
				one(first).execute(control, build, clients);
			}
		});
		build.start(clients);
		assertThat(build.getCurrentPhase(), is(equalTo(0)));
		build.finish(0, 0, "no-result", true, clients);
		assertThat(build.getCurrentPhase(), is(equalTo(0)));
		mockery.assertIsSatisfied();
	}

	@Test
	public void changesThePhaseWhenAllCommandsWereExecuted() throws InstantiationException, IllegalAccessException,
			InvocationTargetException, NoSuchMethodException, IOException {
		phases.add(first);
		phases.add(second);
		mockery.checking(new Expectations() {
			{
				one(project).nextBuild();
				will(returnValue(3L));
				allowing(project).getName();
				will(returnValue("my-horses"));
				one(control).checkout((File) with(an(File.class)));
				one(control).getRevision();
				will(returnValue("my-revision"));
				allowing(project).getBuildsDirectory();
				will(returnValue(baseDir));
				one(first).getCommandCount(); will(returnValue(1));
			}
		});
		final Build build = new Build(project);
		mockery.checking(new Expectations() {
			{
				one(first).execute(control, build, clients);
				one(second).execute(control, build, clients);
			}
		});
		build.start(clients);
		assertThat(build.getCurrentPhase(), is(equalTo(0)));
		build.finish(0, 0, "no-result", true, clients);
		assertThat(build.getCurrentPhase(), is(equalTo(1)));
		mockery.assertIsSatisfied();
	}

}
