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

import java.io.File;
import java.io.PrintWriter;
import java.util.List;

public class AntJunitReportFileCreator {

	private final List<File> reportsDir;

	public AntJunitReportFileCreator(List<File> reportsDir) {
		this.reportsDir = reportsDir;
	}

	public void create(PrintWriter out, File outputDir) {
		out.println("<project name=\"junit\" default=\"report\">");
		out.println("\t<target name=\"report\">");
		out.println("\t<junitreport todir=\"./reports\">");
		for (File reportDir : reportsDir) {
			out.println("\t\t<fileset dir=\"" + reportDir.getAbsolutePath() + "\">");
			out.println("\t\t\t<include name=\"TEST-*.xml\"/>");
			out.println("\t\t</fileset>");
		}
		out.println("\t\t<report format=\"frames\" todir=\"" + outputDir + "\"/>");
		out.println("\t</junitreport>");
		out.println("\t</target>");
		out.println("</project>");
	}

}
