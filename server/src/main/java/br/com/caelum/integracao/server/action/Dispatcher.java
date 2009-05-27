package br.com.caelum.integracao.server.action;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

import br.com.caelum.integracao.server.scm.svn.CommandToExecute;

public class Dispatcher {

	static final String ZIP_FILE = "ZIP_FILE";
	static final String EXECUTE = "EXECUTE";
	private final Socket socket;
	private DataOutputStream output;

	public Dispatcher(String host, int port) throws UnknownHostException, IOException {
		this.socket = new Socket(host, port);
		this.output = new DataOutputStream(socket.getOutputStream());
	}

	public void send(File dir) throws IOException {
		int result = new CommandToExecute("zip", "-r", "zipped.zip", dir.getName()).at(dir.getParentFile()).runAs("zip");
		if(result!=0) {
			throw new IOException("Unable to zip " + dir.getAbsolutePath() + " : "  + result);
		}
		File zip = new File(dir.getParentFile(), "zipped.zip");
		FileInputStream fis = new FileInputStream(zip);
		output.writeUTF(ZIP_FILE);
		output.writeLong(zip.length());
		while(true) {
			int b = fis.read();
			if(b==-1) {
				break;
			}
			output.write(b);
		}
		fis.close();
	}
	
	public void execute(String ...command) throws IOException {
		output.writeUTF(EXECUTE);
		output.writeLong(command.length);
		for(String part : command) {
			output.writeUTF(part);
		}
	}

}
