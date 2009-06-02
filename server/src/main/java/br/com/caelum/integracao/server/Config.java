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
package br.com.caelum.integracao.server;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import org.hibernate.annotations.CollectionOfElements;

import br.com.caelum.integracao.server.plugin.PluginInformation;
import br.com.caelum.integracao.server.plugin.build.RemoveOldBuildsInformation;
import br.com.caelum.integracao.server.plugin.copy.CopyFilesInformation;
import br.com.caelum.integracao.server.plugin.mail.SendMailInformation;

@Entity
public class Config {
	
	@Id
	@GeneratedValue
	public Long id;
	
	private String hostname = "localhost";
	
	private Integer port = 9091;
	
	private int checkInterval = 60;

	@CollectionOfElements
	private List<Class<? extends PluginInformation>> plugins;

	public void setHostname(String hostname) {
		this.hostname = hostname;
	}

	public String getHostname() {
		return hostname;
	}

	public void setPort(Integer port) {
		this.port = port;
	}

	public Integer getPort() {
		return port;
	}

	public String getUrl() {
		return getHostname() + ":" + getPort();
	}
	
	public void registerBasicPlugins() {
		getAvailablePlugins().add(CopyFilesInformation.class);
		getAvailablePlugins().add(SendMailInformation.class);
		getAvailablePlugins().add(RemoveOldBuildsInformation.class);
	}
	
	public List<Class<? extends PluginInformation>> getAvailablePlugins() {
		if(plugins==null) {
			plugins = new ArrayList<Class<? extends PluginInformation>>();
		}
		return plugins;
	}

	public void setCheckInterval(int checkInterval) {
		this.checkInterval = checkInterval;
	}

	public int getCheckInterval() {
		return checkInterval;
	}

}
