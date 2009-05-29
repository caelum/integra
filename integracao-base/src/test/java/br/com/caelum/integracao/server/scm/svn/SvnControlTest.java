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
package br.com.caelum.integracao.server.scm.svn;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;

import br.com.caelum.integracao.AtDirectoryTest;

public class SvnControlTest extends AtDirectoryTest {

	@Test
	public void shouldCommitAndReceiveUpdate() throws IOException {
		SvnControl control1 = new SvnControl("svn+ssh://192.168.0.2/svn/caelum/how-to/trunk/apostilas", baseDir, "apostilas-1");
		Assert.assertEquals(0,control1.checkout(new File(this.baseDir, "control1-checkout")));
		
		SvnControl control2 = new SvnControl("svn+ssh://192.168.0.2/svn/caelum/how-to/trunk/apostilas", baseDir, "apostilas-2");
		Assert.assertEquals(0,control2.checkout(new File(this.baseDir, "control2-checkout")));
		
		File file = new File(control1.getDir(), "test-file");
		givenA(file, "misc content");
		
		control1.add(file);
		control1.commit("commiting test file");
		control2.update();
		File found = new File(control2.getDir(), "test-file");
		Assert.assertTrue(found.exists());
		String content = new BufferedReader(new FileReader(found)).readLine();
		Assert.assertEquals("misc content", content);
		control2.remove(found);
		control2.commit("removed test file");
		control1.update();
		Assert.assertFalse(file.exists());
	}

}
