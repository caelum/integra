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
package br.com.caelum.integracao.server.scm;

import java.io.File;
import java.io.PrintWriter;

/**
 * An scm implementation.
 *
 * @author guilherme silveira
 */
public interface ScmControl {

	File getDir();

	/**
	 * Returns the information between Revision and the current revision.<br/>
	 * If fromRevision is null, returns the log message "first checkout".
	 */
	Revision getCurrentRevision(Revision fromRevision, PrintWriter log) throws ScmException;

	/**
	 * Returns the next revision after this one.
	 *
	 * @param fromRevision
	 *            must be different than null
	 */
	Revision getNextRevision(Revision fromRevision, PrintWriter log) throws ScmException;

	int checkoutOrUpdate(String revision, PrintWriter log) throws ScmException;

	/**
	 * Returns the pattern of files to be ignored when zipping the project's
	 * content.<br/>
	 * i.e.: .svn, .git and so on.
	 */
	String getIgnorePattern();

	Revision extractRevision(String name, PrintWriter log, String range) throws ScmException;

}
