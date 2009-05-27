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
package br.com.caelum.integracao.server.action;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

import br.com.caelum.integracao.CommandToExecute;

public class Dispatcher {

	static final String ZIP_FILE = "ZIP_FILE";
	static final String EXECUTE = "EXECUTE";
	private final Socket socket;
	private DataOutputStream output;
	private final int id;
	
	static int uniqueCount = 0;

	public Dispatcher(String host, int port) throws UnknownHostException, IOException {
		this.socket = new Socket(host, port);
		this.output = new DataOutputStream(socket.getOutputStream());
		this.id = ++uniqueCount;
	}

	public Dispatcher send(File dir) throws IOException {
		int result = new CommandToExecute("zip", "-r", "zipped.zip", dir.getName()).at(dir.getParentFile()).runAs("zip");
		if(result!=0) {
			throw new IOException("Unable to zip " + dir.getAbsolutePath() + " : "  + result);
		}
		File zip = new File(dir.getParentFile(), "zipped.zip");
		FileInputStream fis = new FileInputStream(zip);
		output.writeUTF(ZIP_FILE);
		output.writeUTF("count-" + id);
		output.writeLong(zip.length());
		while(true) {
			int b = fis.read();
			if(b==-1) {
				break;
			}
			output.write(b);
		}
		fis.close();
		return this;
	}
	
	public Dispatcher execute(String ...command) throws IOException {
		output.writeUTF(EXECUTE);
		output.writeUTF("count-" + id);
		output.writeLong(command.length);
		for(String part : command) {
			output.writeUTF(part);
		}
		return this;
	}
	
	public void close() throws IOException {
		socket.close();
	}

}
