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
package br.com.caelum.integracao.client;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import br.com.caelum.integracao.client.command.Command;
import br.com.caelum.integracao.client.command.Commands;
import br.com.caelum.integracao.client.command.Execute;
import br.com.caelum.integracao.client.command.ReceiveZip;

public class Client {
	
	private ServerSocket clientSocket;

	public Client() throws IOException {
		this.clientSocket = new ServerSocket();
		this.clientSocket.bind(null);
	}

	public Client(int port) throws IOException {
		this.clientSocket = new ServerSocket(port);
	}

	public int getPort() {
		return this.clientSocket.getLocalPort();
	}

	public void waitForNewServer() throws IOException {
		final Socket server = this.clientSocket.accept();
		new Thread(new Runnable() {
			public void run() {
				Commands cmd = new Commands();
				cmd.register(ReceiveZip.class);
				cmd.register(Execute.class);
				try {
					DataInputStream input = new DataInputStream(server.getInputStream());
					while(true) {
						String commandReceived = input.readUTF();
						if(cmd.equals("DISCONNECT")) {
							System.out.println("Disconnecting from server");
							server.close();
							break;
						}
						Command result = cmd.commandFor(commandReceived);
						if(result==null) {
							throw new IllegalArgumentException("Unable to parse " + commandReceived);
						}
						result.execute(input);
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}).start();
	}
}
