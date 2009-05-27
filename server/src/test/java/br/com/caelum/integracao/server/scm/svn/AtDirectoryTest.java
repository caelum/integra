package br.com.caelum.integracao.server.scm.svn;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.junit.After;
import org.junit.Before;

public class AtDirectoryTest {

	protected File baseDir;

	@Before
	public void setup() {
		this.baseDir = new File(".tmp");
		clear(baseDir);
		this.baseDir.mkdirs();
	}

	@After
	public void remove() {
		clear(baseDir);
	}

	private void clear(File dir) {
		if(!dir.exists()) {
			return;
		}
		for(File f: dir.listFiles()) {
			if(f.isDirectory()) {
				clear(f);
			} else {
				f.delete();
			}
		}
		dir.delete();
	}

	protected void givenA(File file, String content) throws IOException {
		FileWriter writer = new FileWriter(file);
		writer.write(content);
		writer.close();
	}

}
