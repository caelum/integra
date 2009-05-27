package br.com.caelum.integracao.server.action;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import br.com.caelum.integracao.server.scm.svn.AtDirectoryTest;
import br.com.caelum.integracao.server.scm.svn.CommandToExecute;

public class DispatcherTest extends AtDirectoryTest {

	private ServerSocket server;
	private AtomicBoolean atomicSent;

	@Before
	public void createServer() throws IOException {
		this.server = new ServerSocket();
		server.bind(null);
		this.atomicSent = new AtomicBoolean(false);
	}

	@Test
	public void shouldZipTheContentsOfADirAndSend() throws IOException {
		File originalFile = new File(baseDir, "test-content");
		givenA(originalFile, "conteudo aqui");
		prepareToRun(new Runnable() {
			public void run() {
				try {
					new Dispatcher("localhost", server.getLocalPort()).send(baseDir);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		Socket client = server.accept();
		InputStream input = client.getInputStream();
		waitForData();
		DataInputStream stream = new DataInputStream(input);
		Assert.assertEquals(Dispatcher.ZIP_FILE, stream.readUTF());
		long length = stream.readLong();
		File f = new File(baseDir, "my.zip");
		FileOutputStream w = new FileOutputStream(f);
		while(length--!=0) {
			w.write(stream.read());
		}
		w.flush();
		w.close();
		File outputDir = new File(baseDir, "outputDir");
		outputDir.mkdirs();
		Assert.assertEquals(0,new CommandToExecute("unzip",f.getAbsolutePath()).at(outputDir).runAs("unzip"));
		Assert.assertEquals("conteudo aqui", contentFrom(new File(new File(outputDir, baseDir.getName()),"test-content")));
	}

	private void waitForData() {
		while(!atomicSent.get()) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	private void prepareToRun(final Runnable runnable) {
		Thread t = new Thread(new Runnable() {
			public void run() {
				// nasty, should receive a signal when to start
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				runnable.run();
				atomicSent.set(true);
			}
		});
		t.start();
	}

	@Test
	public void shouldExecuteACommand() throws IOException {
		prepareToRun(new Runnable() {
			public void run() {
				try {
					new Dispatcher("localhost", server.getLocalPort()).execute("run", "from", "commandLine");
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		Socket client = server.accept();
		InputStream input = client.getInputStream();
		waitForData();
		DataInputStream stream = new DataInputStream(input);
		Assert.assertEquals(Dispatcher.EXECUTE, stream.readUTF());
		Assert.assertEquals(3, stream.readLong());
		Assert.assertEquals("run", stream.readUTF());
		Assert.assertEquals("from", stream.readUTF());
		Assert.assertEquals("commandLine", stream.readUTF());
	}

	private String contentFrom(File f) throws FileNotFoundException, IOException {
		return new BufferedReader(new FileReader(f)).readLine();
	}

}
