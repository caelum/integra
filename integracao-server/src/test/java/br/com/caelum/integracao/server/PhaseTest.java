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
import java.util.Arrays;

import org.jmock.Expectations;
import org.junit.Before;
import org.junit.Test;

import br.com.caelum.integracao.server.dao.Database;
import br.com.caelum.integracao.server.plugin.PluginException;
import br.com.caelum.integracao.server.plugin.PluginToRun;
import br.com.caelum.integracao.server.project.BaseTest;
import br.com.caelum.integracao.server.queue.Jobs;
import br.com.caelum.integracao.server.scm.ScmControl;

public class PhaseTest extends BaseTest {
	
	private Build build;
	private Jobs jobs;
	private Database database;

	@Before
	public void mockData() {
		this.build = mockery.mock(Build.class);
		this.jobs = mockery.mock(Jobs.class);
		this.database =mockery.mock(Database.class);
		mockery.checking(new Expectations() {
			{
				allowing(build).getProject(); will(returnValue(new Project(ScmControl.class, "custom", baseDir, "custom")));
			}
		});
	}

	
	@Test
	public void shouldOnlyCreateADirIfThereAreNoCommands() throws IOException {
		Phase compile = new Phase("compile");
		final File dir = new File(baseDir, "custom-dir");
		mockery.checking(new Expectations() {
			{
				one(build).getFile("compile"); will(returnValue(dir));
			}
		});
		compile.execute(build, jobs);
		assertThat(dir.exists(), is(equalTo(true)));
		mockery.assertIsSatisfied();
	}
	
	@Test
	public void shouldInvokeAfterPhaseOnAllPlugins() throws PluginException {
		final PluginToRun run = mockery.mock(PluginToRun.class);
		final PluginToRun second = mockery.mock(PluginToRun.class, "second");
		final Phase test = new Phase();
		mockery.checking(new Expectations() {
			{
				one(run).setPosition(1);
				one(second).setPosition(2);
				one(run).execute(build, test, database); will(returnValue(true));
				one(second).execute(build, test,database); will(returnValue(true));
			}
		});
		test.add(run);
		test.add(second);
		assertThat(test.runAfter(build,database), is(equalTo(true)));
		mockery.assertIsSatisfied();
	}
	
	@Test
	public void shouldNotInvokeNextPluginIfPreviousOneReturnedFalse() throws PluginException {
		final PluginToRun run = mockery.mock(PluginToRun.class);
		final PluginToRun second = mockery.mock(PluginToRun.class, "second");
		final Phase test = new Phase();
		mockery.checking(new Expectations() {
			{
				one(run).setPosition(1);
				one(second).setPosition(2);
				one(run).execute(build, test, database); will(returnValue(false));
			}
		});
		test.add(run);
		test.add(second);
		assertThat(test.runAfter(build,database), is(equalTo(false)));
		mockery.assertIsSatisfied();
	}
	
	@Test
	public void executionShouldCreateAJobForEachCommand() throws IOException {
		final BuildCommand first = mockery.mock(BuildCommand.class, "firstJob");
		final BuildCommand second = mockery.mock(BuildCommand.class, "secondJob");
		
		Phase compileTwice = new Phase("compile");
		compileTwice.setCommands(Arrays.asList(first,second));
		mockery.checking(new Expectations() {
			{
				one(first).isActive(); will(returnValue(true));
				one(second).isActive(); will(returnValue(true));
				one(jobs).add(with((IntegracaoMatchers.jobFor(build, first))));
				one(jobs).add(with((IntegracaoMatchers.jobFor(build, second))));
				one(build).getFile("compile"); will(returnValue(new File(baseDir, "compile")));
			}
		});
		compileTwice.execute(build, jobs);
		mockery.assertIsSatisfied();
	}

	
	@Test
	public void executionShouldNotCreateAJobForInactiveCommands() throws IOException {
		final BuildCommand first = mockery.mock(BuildCommand.class, "firstJob");
		
		Phase compileTwice = new Phase("compile");
		compileTwice.setCommands(Arrays.asList(first));
		mockery.checking(new Expectations() {
			{
				one(first).isActive(); will(returnValue(false));
				one(build).getFile("compile"); will(returnValue(new File(baseDir, "compile")));
			}
		});
		compileTwice.execute(build, jobs);
		mockery.assertIsSatisfied();
	}

}
