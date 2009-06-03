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
package br.com.caelum.integracao.server.plugin.copy;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.httpclient.HttpException;
import org.jmock.Expectations;
import org.junit.Before;
import org.junit.Test;

import br.com.caelum.integracao.http.Http;
import br.com.caelum.integracao.http.Method;
import br.com.caelum.integracao.server.Build;
import br.com.caelum.integracao.server.Client;
import br.com.caelum.integracao.server.Phase;
import br.com.caelum.integracao.server.Project;
import br.com.caelum.integracao.server.project.BaseTest;
import br.com.caelum.integracao.server.queue.Job;

public class CopyFilesTest extends BaseTest {
	
	private Job first;
	private Job second;

	@Before
	public void setupClients() {
		this.first = mockery.mock(Job.class, "first");
		this.second = mockery.mock(Job.class, "second");
		mockery.checking(new Expectations() {
			{
				allowing(first).getClient(); will(returnValue(createClient("uri")));
				allowing(second).getClient(); will(returnValue(createClient("second uri")));
			}
		});
	}

	@Test
	public void shouldAskForTheZippedFilesForEachDirectory() throws IOException {
		File reportDir = new File(baseDir, "reports");
		File artifactDir = new File(baseDir, "artifacts");
		final Http http = mockery.mock(Http.class);
		final String reportPath = reportDir.getAbsolutePath();
		final String artifactPath = artifactDir.getAbsolutePath();
		CopyFiles copier = new CopyFiles(http, new String[] { reportPath, artifactPath });
		final Build build = mockery.mock(Build.class);
		final Phase phase = mockery.mock(Phase.class);
		final List<Job> clients = Arrays.asList(first,second);
		final Project caelumweb = mockery.mock(Project.class);
		mockery.checking(new Expectations() {
			{
				expectHttpCall("uri");
				expectHttpCall("second uri");
				one(build).getJobsFor(phase);
				will(returnValue(clients));
				allowing(caelumweb).getName(); will(returnValue("caelumweb"));
				allowing(build).getProject(); will(returnValue(caelumweb));
			}

			private void expectHttpCall(String uri) throws HttpException, IOException {
				Method get = mockery.mock(Method.class, "method" + uri);
				one(http).post(uri + "/plugin/CopyFiles/caelumweb");
				will(returnValue(get));
				one(get).with("directory[0]", reportPath);
				one(get).with("directory[1]", artifactPath);
				one(get).send();
				one(get).getResult();
				will(returnValue(200));
			}
		});
		copier.after(build, phase);
		mockery.assertIsSatisfied();
	}

	private Client createClient(final String uri) {
		final Client client = mockery.mock(Client.class, "client" + uri);
		mockery.checking(new Expectations() {
			{
				one(client).getBaseUri(); will(returnValue(uri));
			}
		});
		return client;
	}

}
