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
import javax.persistence.JoinTable;
import javax.persistence.Lob;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.validator.NotNull;

import br.com.caelum.integracao.server.label.Label;

@Entity
public class BuildCommand {

	@Id
	@GeneratedValue
	private Long id;

	@OneToMany
	@OrderBy("id")
	@JoinTable(name = "CommandStart")
	@Cascade(value = { CascadeType.SAVE_UPDATE, CascadeType.DELETE, CascadeType.DELETE_ORPHAN, CascadeType.REMOVE })
	private List<Command> start = new ArrayList<Command>();

	@OneToMany
	@OrderBy("id")
	@JoinTable(name = "CommandStop")
	@Cascade(value = { CascadeType.SAVE_UPDATE, CascadeType.DELETE, CascadeType.DELETE_ORPHAN, CascadeType.REMOVE })
	private List<Command> stop = new ArrayList<Command>();

	@Lob
	private String artifactsToPush = "";

	private boolean active = true;

	@NotNull
	@ManyToOne
	private Phase phase;

	@ManyToMany
	@Cascade(value = { CascadeType.SAVE_UPDATE, CascadeType.DELETE, CascadeType.DELETE_ORPHAN, CascadeType.REMOVE })
	private List<Label> labels = new ArrayList<Label>();

	BuildCommand() {
	}

	public BuildCommand(Phase phase, String[] start, String[] stop, List<Label> labels, String artifactsToPush) {
		this.artifactsToPush = artifactsToPush;
		this.phase = phase;
		this.labels = labels;
		for (String command : start) {
			this.start.add(new Command(command));
		}
		for (String command : stop) {
			this.stop.add(new Command(command));
		}
		phase.getCommands().add(this);
	}

	public String getName() {
		String name = "";
		for (Command cmd : start) {
			name += cmd.getValue() + " ";
		}
		return name;
	}

	public String getStopName() {
		if (stop == null || stop.isEmpty()) {
			return "";
		}
		String name = "";
		for (Command cmd : stop) {
			name += cmd.getValue() + " ";
		}
		return name;
	}

	public List<Command> getStartCommands() {
		return start;
	}

	public List<Command> getStopCommands() {
		return stop;
	}

	public Long getId() {
		return id;
	}

	public Phase getPhase() {
		return phase;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public List<Label> getLabels() {
		return labels;
	}

	public String getLabelsString() {
		String labels = "";
		for (Label label : this.labels) {
			labels += label.getName() + ",";
		}
		return labels;
	}

	public void deactivate() {
		this.active = false;
	}

	public boolean isActive() {
		return active;
	}

	public String getArtifactsToPushString() {
		return artifactsToPush;
	}

	public List<String> getArtifactsToPush() {
		String[] vals = artifactsToPush.split("\\s*,\\s*");
		List<String> list = new ArrayList<String>();
		for (String val : vals) {
			if (!val.trim().equals("")) {
				list.add(val);
			}
		}
		return list;
	}

	@Override
	public String toString() {
		return "{Command " + getName() + "}";
	}

}
