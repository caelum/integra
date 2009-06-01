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
package br.com.caelum.integracao.server.plugin;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;

import org.hibernate.validator.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Entity
public class PluginToRun {
	
	private static final Logger logger = LoggerFactory.getLogger(PluginToRun.class);

	@Id
	@GeneratedValue
	private Long id;

	@Column(name = "pos")
	private int position;

	@NotNull
	private Class<? extends PluginInformation> type;
	
	@OneToMany(mappedBy="plugin")
	private List<PluginParameter> config;
	
	public PluginToRun(Class<? extends PluginInformation> type) {
		this.type = type;
	}
	
	protected PluginToRun() {
	}
	
	public Class<? extends PluginInformation> getType() {
		return type;
	}
	
	public Long getId() {
		return id;
	}
	
	public void setPosition(int position) {
		this.position = position;
	}
	
	public Plugin getPlugin() {
		try {
			PluginInformation plugin = type.getDeclaredConstructor().newInstance();
			return plugin.getPlugin(createParameters());
		} catch (Exception e) {
			logger.error("Unable to instantiate the plugin " + type.getName(), e);
			return null;
		}
	}

	private Map<String, String> createParameters() {
		Map<String, String> map = new HashMap<String, String>();
		for(PluginParameter param : config) {
			map.put(param.getKey(), param.getValue());
		}
		return map;
	}
	
	public PluginInformation getInformation() throws IllegalArgumentException, SecurityException, InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		return type.getConstructor().newInstance();
	}
	
	public String get(String key) {
		for(PluginParameter param : this.config) {
			if(param.getKey().equals(key)) {
				return param.getValue();
			}
		}
		return "";
	}

}
