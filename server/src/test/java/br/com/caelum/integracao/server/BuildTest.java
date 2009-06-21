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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.jmock.Expectations;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import br.com.caelum.integracao.server.plugin.Plugin;
import br.com.caelum.integracao.server.plugin.PluginException;
import br.com.caelum.integracao.server.plugin.PluginToRun;
import br.com.caelum.integracao.server.project.DatabaseBasedTest;
import br.com.caelum.integracao.server.queue.Jobs;
import br.com.caelum.integracao.server.scm.Revision;
import br.com.caelum.integracao.server.scm.ScmControl;
import br.com.caelum.integracao.server.scm.ScmException;

public class BuildTest extends DatabaseBasedTest {

	private Project project;
	private List<Phase> phases;
	private ScmControl control;
	private Phase first;
	private Phase second;
	private ArrayList<PluginToRun> plugins;
	private Jobs jobs;

	@Before
	public void configProject() throws ScmException {
		this.jobs = mockery.mock(Jobs.class);
		this.project = mockery.mock(Project.class);
		this.phases = new ArrayList<Phase>();
		this.control = mockery.mock(ScmControl.class, "control");
		this.first = mockery.mock(Phase.class, "first");
		this.second = mockery.mock(Phase.class, "second");
		this.plugins = new ArrayList<PluginToRun>();
		mockery.checking(new Expectations() {
			{
				allowing(project).getBaseDir();
				will(returnValue(baseDir));
				allowing(project).getControl();
				will(returnValue(control));
				allowing(project).getPhases();
				will(returnValue(phases));
				allowing(project).getPlugins();
				will(returnValue(plugins));
				allowing(project).nextBuild(); will(returnValue(3L));
				allowing(project).nextBuild(); will(returnValue(2L));
				allowing(project).getBuildsDirectory();
				will(returnValue(baseDir));
			}
		});
	}

	@Test
	public void createsABuildWithCorrectBuildCount() {
		Build build = new Build(project);
		assertThat(build.getBuildCount(), is(equalTo(3L)));
		File dir = new File(baseDir, "build-3");
		assertThat(dir.exists(), is(equalTo(true)));
		mockery.assertIsSatisfied();
	}

	@Test
	public void createsABuildWithCorrectCheckoutInfo() throws IllegalArgumentException, SecurityException,
			InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException,
			IOException, ScmException {
		mockery.checking(new Expectations() {
			{
				allowing(project).getName();
				will(returnValue("my-horses"));
				one(control).checkoutOrUpdate(null, (PrintWriter) with(an(PrintWriter.class)));
				one(control).getCurrentRevision(null,(PrintWriter) with(an(PrintWriter.class)));
				will(returnValue("my-revision"));
			}
		});
		Build build = new Build(project);
		build.start(jobs, database);
		assertThat(build.getRevision().getName(), is(equalTo("my-revision")));
		File checkout = new File(baseDir, "build-3/checkout.txt");
		assertThat(checkout.exists(), is(equalTo(true)));
		mockery.assertIsSatisfied();
	}

	@Test
	public void keepsTheCurrentPhaseAndInvokesRunAfterOnPluginsIfNoSuccess() throws InstantiationException,
			IllegalAccessException, InvocationTargetException, NoSuchMethodException, IOException, ScmException {
		phases.add(first);
		phases.add(second);
		mockery.checking(new Expectations() {
			{
				allowing(project).getName();
				will(returnValue("my-horses"));
				one(control).checkoutOrUpdate(null,(PrintWriter) with(an(PrintWriter.class)));
				one(control).getCurrentRevision(null,(PrintWriter) with(an(PrintWriter.class)));
				will(returnValue("my-revision"));
				one(first).getCommandCount();
				will(returnValue(1));
			}
		});
		final Build build = new Build(project);
		mockery.checking(new Expectations() {
			{
				one(first).execute(build, jobs);
				one(first).runAfter(build, database);
				will(returnValue(true));
			}
		});
		build.start(jobs, database);
		assertThat(build.getCurrentPhase(), is(equalTo(0)));
		build.failed();
		build.proceed(first, database);
		assertThat(build.getCurrentPhase(), is(equalTo(0)));
		mockery.assertIsSatisfied();
	}

	@Test
	public void keepsTheCurrentPhaseIfSuccessButPluginsFail() throws InstantiationException, IllegalAccessException,
			InvocationTargetException, NoSuchMethodException, IOException, ScmException {
		phases.add(first);
		phases.add(second);
		mockery.checking(new Expectations() {
			{
				allowing(project).getName();
				will(returnValue("my-horses"));
				one(control).checkoutOrUpdate(null,(PrintWriter) with(an(PrintWriter.class)));
				one(control).getCurrentRevision(null,(PrintWriter) with(an(PrintWriter.class)));
				will(returnValue("my-revision"));
				one(first).getCommandCount();
				will(returnValue(1));
			}
		});
		final Build build = new Build(project);
		mockery.checking(new Expectations() {
			{
				one(first).execute(build, jobs);
				one(first).runAfter(build, database);
				will(returnValue(false));
			}
		});
		build.start(jobs, database);
		assertThat(build.getCurrentPhase(), is(equalTo(0)));
		build.proceed(first, database);
		assertThat(build.getCurrentPhase(), is(equalTo(0)));
		mockery.assertIsSatisfied();
	}

	@Test
	public void keepsTheCurrentPhaseIfThereAreStillCommandsToDo() throws InstantiationException,
			IllegalAccessException, InvocationTargetException, NoSuchMethodException, IOException, ScmException {
		phases.add(first);
		phases.add(second);
		mockery.checking(new Expectations() {
			{
				allowing(project).getName();
				will(returnValue("my-horses"));
				one(control).checkoutOrUpdate(null,(PrintWriter) with(an(PrintWriter.class)));
				one(control).getCurrentRevision(null,(PrintWriter) with(an(PrintWriter.class)));
				will(returnValue("my-revision"));
				one(first).getCommandCount();
				will(returnValue(2));
			}
		});
		final Build build = new Build(project);
		mockery.checking(new Expectations() {
			{
				one(first).execute(build, jobs);
			}
		});
		build.start(jobs, database);
		assertThat(build.getCurrentPhase(), is(equalTo(0)));
		build.proceed(first, database);
		assertThat(build.getCurrentPhase(), is(equalTo(0)));
		mockery.assertIsSatisfied();
	}

	@Test
	public void changesThePhaseWhenAllCommandsWereExecuted() throws InstantiationException, IllegalAccessException,
			InvocationTargetException, NoSuchMethodException, IOException, ScmException {
		phases.add(first);
		phases.add(second);
		mockery.checking(new Expectations() {
			{
				allowing(project).getName();
				will(returnValue("my-horses"));
				one(control).checkoutOrUpdate(null,(PrintWriter) with(an(PrintWriter.class)));
				one(control).getCurrentRevision(null,(PrintWriter) with(an(PrintWriter.class)));
				will(returnValue("my-revision"));
				one(first).getCommandCount();
				will(returnValue(1));
			}
		});
		final Build build = new Build(project);
		mockery.checking(new Expectations() {
			{
				one(first).execute(build, jobs);
				one(second).execute(build, jobs);
				one(first).runAfter(build, database);
				will(returnValue(true));
			}
		});
		build.start(jobs, database);
		assertThat(build.getCurrentPhase(), is(equalTo(0)));
		build.proceed(first, database);
		assertThat(build.getCurrentPhase(), is(equalTo(1)));
		mockery.assertIsSatisfied();
	}

	@Test
	public void shouldNotStartPhaseIfResultIsZero() {
		Assert.fail("not yet implemented");
	}

	@Test
	public void removeShouldEmptyItsDirectory() throws IOException {
		File buildDir = new File(baseDir, "build-3");
		buildDir.mkdirs();
		File file = new File(buildDir, "custom");
		givenA(file, "custom-file-content");
		Build build = new Build(project);
		build.remove(database);
		assertThat(file.exists(), is(equalTo(false)));
		assertThat(buildDir.exists(), is(equalTo(false)));
		mockery.assertIsSatisfied();
	}

	@Test
	public void invokesTheBeforeMethodOfAPlugin() throws InstantiationException, IllegalAccessException,
			InvocationTargetException, NoSuchMethodException, IOException, ScmException, PluginException {
		phases.add(first);
		final PluginToRun firstPlugin = mockery.mock(PluginToRun.class, "firstPlugin");
		final Plugin firstImplementation = mockery.mock(Plugin.class, "firstImplementation");
		mockery.checking(new Expectations() {
			{
				allowing(project).getName();
				will(returnValue("my-horses"));
				one(control).checkoutOrUpdate(null,(PrintWriter) with(an(PrintWriter.class)));
				one(control).getCurrentRevision(null,(PrintWriter) with(an(PrintWriter.class)));
				will(returnValue("my-revision"));
				one(firstPlugin).getPlugin(database);
				will(returnValue(firstImplementation));
			}
		});
		this.plugins.add(firstPlugin);
		final Build build = new Build(project);
		mockery.checking(new Expectations() {
			{
				one(firstImplementation).before(build); will(returnValue(true));
				one(first).execute(build, jobs);
			}
		});
		build.start(jobs, database);
		mockery.assertIsSatisfied();
	}

	@Test
	public void doesntContinueTheStartProcessIfAPluginTellsItToStop() throws InstantiationException,
			IllegalAccessException, InvocationTargetException, NoSuchMethodException, IOException, ScmException, PluginException {
		phases.add(first);
		final PluginToRun firstPlugin = mockery.mock(PluginToRun.class, "firstPlugin");
		final Plugin firstImplementation = mockery.mock(Plugin.class, "firstImplementation");
		final PluginToRun secondPlugin = mockery.mock(PluginToRun.class, "secondPlugin");
		this.plugins.add(firstPlugin);
		this.plugins.add(secondPlugin);
		mockery.checking(new Expectations() {
			{
				allowing(project).getName();
				will(returnValue("my-horses"));
				one(control).checkoutOrUpdate(with(aNull(String.class)),(PrintWriter) with(an(PrintWriter.class)));
				one(control).getCurrentRevision(with(aNull(Revision.class)),(PrintWriter) with(an(PrintWriter.class)));
				will(returnValue("my-revision"));
				one(firstPlugin).getPlugin(database);
				will(returnValue(firstImplementation));
				allowing(firstPlugin).getType();will(returnValue(BuildTest.class));
			}
		});
		final Build build = new Build(project);
		mockery.checking(new Expectations() {
			{
				one(firstImplementation).before(build); will(returnValue(false));
			}
		});
		build.start(jobs, database);
		assertThat(build.isFinished(), is(equalTo(true)));
		assertThat(build.isSuccessSoFar(), is(equalTo(false)));
		mockery.assertIsSatisfied();
	}

	@Test
	public void shouldDetectWhenStatusHasChangedFromLastBuild() {
		Build build = new Build(project);
		final Build previous = new Build(project);
		previous.failed();
		mockery.checking(new Expectations() {
			{
				allowing(project).getBuild(2L); will(returnValue(previous));
			}
		});
		assertThat(build.buildStatusChangedFromLastBuild(), is(equalTo(true)));
	}
	@Test
	public void shouldDetectWhenStatusHasNotChangedFromLastBuild() {
		Build build = new Build(project);
		final Build previous = new Build(project);
		mockery.checking(new Expectations() {
			{
				allowing(project).getBuild(2L); will(returnValue(previous));
			}
		});
		assertThat(build.buildStatusChangedFromLastBuild(), is(equalTo(false)));
	}
	@Test
	public void shouldDetectWhenStatusHasNotChangedFromLastBuildBecauseItsTheFirstBuild() {
		Build build = new Build(project);
		mockery.checking(new Expectations() {
			{
				allowing(project).getBuild(2L); will(returnValue(null));
			}
		});
		assertThat(build.buildStatusChangedFromLastBuild(), is(equalTo(true)));
	}
	
	@Test
	public void shouldReturnElapsedTimeIfNotFinished() {
		Build build = new Build(project);
		assertThat(build.getRuntime(), is(equalTo((System.currentTimeMillis()-build.getStartTime().getTimeInMillis())/(1000.0*60))));
	}
	@Test
	public void shouldReturnExecutionTimeIfFinished() {
		Build build = new Build(project);
		build.finish(false, "", null, null);
		assertThat(build.getRuntime(), is(equalTo((build.getFinishTime().getTimeInMillis()-build.getStartTime().getTimeInMillis())/(1000.0*60))));
	}
}
