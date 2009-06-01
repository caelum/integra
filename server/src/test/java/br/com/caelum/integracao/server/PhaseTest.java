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
import java.util.ArrayList;

import org.jmock.Expectations;
import org.junit.Before;
import org.junit.Test;

import br.com.caelum.integracao.server.plugin.Plugin;
import br.com.caelum.integracao.server.plugin.PluginToRun;
import br.com.caelum.integracao.server.project.BaseTest;

public class PhaseTest extends BaseTest {
	
	private Build build;

	@Before
	public void mockData() {
		this.build = mockery.mock(Build.class);
	}
	@Test
	public void commandRemovalShouldUpdateOtherIndexes() {
		final ExecuteCommandLine test = new ExecuteCommandLine();
		test.setPosition(1);
		ExecuteCommandLine deploy = new ExecuteCommandLine();
		deploy.setPosition(2);
		final Projects projects = mockery.mock(Projects.class);

		Phase compile = new Phase();
		compile.setCommands(new ArrayList<ExecuteCommandLine>());
		compile.getCommands().add(test);
		compile.getCommands().add(deploy);
		mockery.checking(new Expectations() {
			{
				one(projects).remove(test);
			}
		});
		compile.remove(projects, test);
		assertThat(deploy.getPosition(), is(equalTo(1)));
		mockery.assertIsSatisfied();
	}
	
	@Test
	public void shouldOnlyCreateADirIfThereAreNoCommands() throws IOException {
		Phase compile = new Phase();
		compile.setPosition(5L);
		final File dir = new File(baseDir, "custom-dir");
		mockery.checking(new Expectations() {
			{
				one(build).getFile("5"); will(returnValue(dir));
			}
		});
		compile.execute(null, build, null, null);
		assertThat(dir.exists(), is(equalTo(true)));
		mockery.assertIsSatisfied();
	}
	
	@Test
	public void shouldInvokeAfterPhaseOnAllPlugins() {
		final PluginToRun run = mockery.mock(PluginToRun.class);
		final PluginToRun second = mockery.mock(PluginToRun.class, "second");
		final Phase test = new Phase();
		final Plugin plugin = mockery.mock(Plugin.class);
		mockery.checking(new Expectations() {
			{
				one(run).setPosition(1);
				one(second).setPosition(2);
				one(run).getPlugin(); will(returnValue(plugin));
				one(second).getPlugin(); will(returnValue(plugin));
				exactly(2).of(plugin).after(build, test); will(returnValue(true));
			}
		});
		test.add(run);
		test.add(second);
		assertThat(test.runAfter(build), is(equalTo(true)));
		mockery.assertIsSatisfied();
	}
	
	@Test
	public void shouldNotInvokeNextPluginIfPreviousOneWasntCreated() {
		final PluginToRun run = mockery.mock(PluginToRun.class);
		final PluginToRun second = mockery.mock(PluginToRun.class, "second");
		final Phase test = new Phase();
		mockery.checking(new Expectations() {
			{
				one(run).setPosition(1);
				one(second).setPosition(2);
				one(run).getPlugin(); will(returnValue(null));
			}
		});
		test.add(run);
		test.add(second);
		assertThat(test.runAfter(build), is(equalTo(false)));
		mockery.assertIsSatisfied();
	}

	@Test
	public void shouldNotInvokeNextPluginIfPreviousOneReturnedFalse() {
		final PluginToRun run = mockery.mock(PluginToRun.class);
		final PluginToRun second = mockery.mock(PluginToRun.class, "second");
		final Phase test = new Phase();
		final Plugin plugin = mockery.mock(Plugin.class);
		mockery.checking(new Expectations() {
			{
				one(run).setPosition(1);
				one(second).setPosition(2);
				one(run).getPlugin(); will(returnValue(plugin));
				one(plugin).after(build, test); will(returnValue(false));
			}
		});
		test.add(run);
		test.add(second);
		assertThat(test.runAfter(build), is(equalTo(false)));
		mockery.assertIsSatisfied();
	}

}
