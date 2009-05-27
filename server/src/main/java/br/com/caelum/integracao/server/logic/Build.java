package br.com.caelum.integracao.server.logic;

import java.io.File;

public class Build {
	
	private final File baseDirectory;

	public Build(File baseDirectory) {
		this.baseDirectory = baseDirectory;
	}

	public String getRevision() {
		return baseDirectory.getName();
	}

}
