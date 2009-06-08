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
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.com.caelum.integracao.command.CommandToExecute;
import br.com.caelum.integracao.server.Build;
import br.com.caelum.integracao.server.ExecuteCommandLine;
import br.com.caelum.integracao.server.Phase;
import br.com.caelum.integracao.server.plugin.Plugin;

/**
 * Copies files from another machine to the server.
 * 
 * @author guilherme silveira
 */
public class JUnitReport implements Plugin {

	private final Logger logger = LoggerFactory.getLogger(JUnitReport.class);
	private final String dir;

	public JUnitReport(String dir) {
		this.dir = dir;
	}

	public boolean after(Build build, Phase phase) {

		try {
			List<File> reportsDir = new ArrayList<File>();
			for (ExecuteCommandLine command : phase.getCommands()) {
				File reportDir = build.getFile(phase.getName() + "/" + command.getId() + "/" + dir);
				reportsDir.add(reportDir);
			}
			// executes the ant task to generate the junit report from all those
			// reports
			logger.debug("Creating junit report generation xml file");
			build.getFile(phase.getName()+ "/junit/reports").mkdirs();
			File xml = build.getFile(phase.getName() + "/junit/report-generate.xml");
			FileWriter writer = new FileWriter(xml);
			PrintWriter out = new PrintWriter(writer, true);
			new AntJunitReportFileCreator(reportsDir).create(out, build.getFile(phase.getName() + "/junit/"));
			out.close();
			writer.close();
			logger.debug("Executing junit task to generate reports on dirs " + reportsDir);
			int result = new CommandToExecute("ant", "-f", xml.getAbsolutePath()).logTo(build.getFile(phase.getName() + "/junit/report-output.txt")).at(xml.getParentFile()).run();
			return result==0;
		} catch (IOException e) {
			logger.error("Unable to create reports for project " + build.getProject().getName(), e);
			return false;
		}
	}

	public boolean before(Build build) {
		return true;
	}

}
