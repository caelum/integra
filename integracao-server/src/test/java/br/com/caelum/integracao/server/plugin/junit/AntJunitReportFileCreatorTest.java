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
package br.com.caelum.integracao.server.plugin.junit;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;

import org.junit.Test;

public class AntJunitReportFileCreatorTest {

	@Test
	public void shouldCreateContentForAllDirectories() {
		String content = 	"<project name=\"junit\" default=\"report\">\n" + 
								"\t<target name=\"report\">\n" +
								"\t<junitreport todir=\"./reports\">\n" +
								"\t\t<fileset dir=\"/tmp/first/dir\">\n"+
								"\t\t\t<include name=\"TEST-*.xml\"/>\n" +
								"\t\t</fileset>\n" + 
								"\t\t<fileset dir=\"/tmp/second/dir\">\n"+
								"\t\t\t<include name=\"TEST-*.xml\"/>\n" +
								"\t\t</fileset>\n" + 
								"\t\t<report format=\"frames\" todir=\"/tmp/output/dir/anywhere\"/>\n" +
								"\t</junitreport>\n"+
								"\t</target>\n" +
							"</project>\n";
		StringWriter out = new StringWriter();
		File outputDir = new File("/tmp/output/dir/anywhere");
		File firstDir = new File("/tmp/first/dir");
		File secondDir = new File("/tmp/second/dir");
		new AntJunitReportFileCreator(Arrays.asList(firstDir, secondDir)).create(new PrintWriter(out, true), outputDir);
		assertThat(out.getBuffer().toString(), is(equalTo(content)));
	}

}
