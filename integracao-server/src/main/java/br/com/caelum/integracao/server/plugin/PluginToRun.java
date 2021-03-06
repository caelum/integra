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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.validator.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.com.caelum.integracao.server.Build;
import br.com.caelum.integracao.server.Phase;
import br.com.caelum.integracao.server.dao.Database;

@Entity
public class PluginToRun {

	private static final Logger logger = LoggerFactory.getLogger(PluginToRun.class);

	@Id
	@GeneratedValue
	private Long id;

	@Column(name = "pos")
	private int position;

	@ManyToOne
	@NotNull
	private RegisteredPlugin type;

	@OneToMany(mappedBy = "plugin")
	@Cascade(CascadeType.ALL)
	private List<PluginParameter> config = new ArrayList<PluginParameter>();

	public PluginToRun(RegisteredPlugin type) {
		this.type = type;
		for (Parameter param : type.getInformation().getParameters()) {
			config.add(new PluginParameter(this, param.getName(), param.getDefaultValue()));
		}
	}

	PluginToRun() {
	}

	public RegisteredPlugin getType() {
		return type;
	}

	public Long getId() {
		return id;
	}

	public void setPosition(int position) {
		this.position = position;
	}

	public Plugin getPlugin(Database db) throws PluginException {
		PluginInformation information = type.getInformation();
		return information.getPlugin(db, createParameters());
	}

	private Map<String, String> createParameters() {
		Map<String, String> map = new HashMap<String, String>();
		for (PluginParameter param : config) {
			map.put(param.getKey(), param.getValue());
		}
		return map;
	}

	public PluginParameter getParameter(String key) {
		for (PluginParameter param : this.config) {
			if (param.getKey().equals(key)) {
				return param;
			}
		}
		PluginParameter plugin = new PluginParameter(this, key, "");
		config.add(plugin);
		return plugin;
	}

	public void updateParameters(List<String> keys, List<String> values) {
		if (keys != null) {
			for (int i = 0; i < keys.size(); i++) {
				String key = keys.get(i);
				PluginParameter param = getParameter(key);
				param.setValue(values.get(i));
			}
		}
	}

	public List<PluginParameter> getConfig() {
		return config;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public boolean execute(Build build, Phase phase, Database database) {
		try {
			Plugin plugin = getPlugin(database);
			if (plugin == null || (!plugin.after(build, phase))) {
				return false;
			}
		} catch (PluginException e) {
			logger.error("Unable to run plugin " + getId() + " after a specific phase.", e);
			return false;
		}
		return true;
	}

}
