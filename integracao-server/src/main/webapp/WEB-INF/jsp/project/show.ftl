<h2>${project.name}</h2>
	Last build: ${project.lastBuildTime.time?datetime }<br />
	
	<form action="run" method="post">
		<input type="submit" value="Force Build" />
		<a href="#specific_revision" onclick="$('#specific_revision').toggle();">(force specific revision)</a><br/>
		<div style="display: none;" id="specific_revision">
			<input name="revision" value="" />
		</div>
	</form>
<div class="box" id="info">
	Uri: ${project.uri }<br />
	Basedir: ${project.buildsDirectory.absolutePath }<br />
	Control: ${project.controlType.name } <br />
	<div class="plugin" id="info_plugins" >
		<div class="plugin_title">Plugins</div>
		<#list project.plugins as plugin>
			<span class="plugin_title">${plugin.type.information.name}</span>
			<span class="commands">
				(<a href="#plugin_${plugin.id }" onclick="$('#plugin_${plugin.id}').load('plugin/${plugin.id}/snippet')">config</a>)
				(<a href="plugin/${plugin.id}?_method=DELETE">remove</a>)
			</span><br/>
			<span id="plugin_${plugin.id }"></span>
		</#list>
		<form action="plugin" method="post">
			<input type="hidden" name="project.name" value="${project.name }" />
			<select name="registered.id">
				<#list plugins as plugin>
					<#if plugin.information.appliesForAProject()><option value="${plugin.id }">${plugin.information.name }</option></#if>
				</#list>
			</select>
			<input type="submit" value="add" />
		</form>
	</div>
</div>

<table class="large">
	<tr>
		<#list project.phases as phase>
			<td>
			<div class="phase">
				<div class="phase_title">${phase.name } (${phase.position })</div>
				<#list phase.commands as cmd>
				<div class="command">
					start: ${cmd.name }<br/>
					<#if cmd.stopName??>stop: ${cmd.stopName}<br/></#if>
					<#if cmd.artifactsToPush?? && cmd.artifactsToPush?size!=0>artifacts:
						<#list cmd.artifactsToPush as artifact>
							${artifact},
						</#list>
						<br/>
					</#if>
					<#if cmd.labels?size!=0>
						labels: "<#list cmd.labels as label>${label.name},</#list>"<br/>
					</#if>
					(<a href="#command_${cmd.id}" onclick="$('#command_${cmd.id }').load('command/${cmd.id}/snippet')">edit</a>)
					(<a href="command/${cmd.id}?_method=DELETE" style="color: red">remove</a>)
					<div id="command_${cmd.id}"></div>
				</div>
				</#list>
				<br/>
				<a href="#new_command_${phase.id}" onclick="$('#new_command_${phase.id}').toggle();">new command</a>
				<div id="new_command_${phase.id}" class="command formulario" style="display: none;">
					<form action="command" method="post">
						<input type="hidden" name="phase.id" value="${phase.id }" />
						(start) <input type="text" name="startCommand" value="" size="5" /> <br/>
						(stop) <input type="text" name="stopCommand" value="" size="5" /> <br/>
						(artifacts to push) <input type="text" name="artifactsToPush" value="" size="20" /> <br/>
						(labels required @ agent) <textarea name="labels" cols="40" rows="5"></textarea>
						<input type="submit" value="add" />
					</form>
				</div>
				<div class="plugin, formulario">
					<div class="plugin_title"><a href="#plugins_for_phase_${phase.id}" onclick="$('#plugins_for_phase_${phase.id}').toggle();">Plugins</a></div>
					<div id="plugins_for_phase_${phase.id}" style="display: none;">
						<#list phase.plugins as plugin>
						<div class="plugin">
								<span class="plugin_title">${plugin.type.information.name }</span>
								<span class="commands">
									(<a href="#plugin_${plugin.id}" onclick="$('#plugin_${plugin.id }').load('plugin/${plugin.id}/snippet')">config</a>)
									(<a href="phase/${phase.id}/plugin/${plugin.id}?_method=DELETE">remove</a>)
								</span>
								<div id="plugin_${plugin.id }"></div>
						</div>
						</#list>
						<form action="phase/plugin" method="post">
							<input type="hidden" name="phase.id" value="${phase.id }" />
							<select name="registered.id">
								<#list plugins as plugin>
									<#if plugin.information.appliesForAPhase()><option value="${plugin.id }">${plugin.information.name }</option></#if>
								</#list>
							</select>
							<input type="submit" value="add" />
						</form>
					</div>
				</div>
			</div>
			</td>
			<td> ----> </td>
		</#list>
		<td>
			<div class="phase">
				<div class="subtitle">New phase</div>
				<form action="phase" method="post" class="formulario">
					<input type="hidden" name="project.name" value="${project.name }" />
					Name: <input size="10" name="phase.name" value="test" />
					Directories to copy to server: <input size="10" name="phase.directoriesToCopy" value="target" />
					<input type="checkbox" name="phase.manual" /> require manual move 
					<input type="submit" value="add" />
				</form>
			</div>
		</td>
	</tr>
</table>
<table>
	<#list project.lastBuilds as build>
		<tr id="build_${build.id}">
			<td><a
				href="build/${build.buildCount}/info">results</a>
			</td>
			<td>build-${build.buildCount}</td>
			<td>
				'${build.revisionName }'
			</td>
			<td>
			<#if !build.finished>
				<font color="orange">building</font>
			<#else>
				<#if build.successSoFar>
					<font color="green">win</font>
				<#else>
					<font color="red">fail</font>
				</#if>
			</#if>
			</td>
			<td>
				<#assign first = true>
				<#list project.phases as phase>

					<#if !first>
						<#if build.canManuallyActivate(last, phase) & build.hasSucceeded(last)>
							<a href="build/${build.buildCount}/manual?_method=put"><img src="../../images/right.gif" style="width:41px;"></a>
						</#if>
					</#if>
					<#assign first = false>
					<#assign last = phase>

					<#if build.hasSucceeded(phase)>
						<img src="../../images/ok.gif" />
					<#elseif build.isRunning(phase)>
						<img src="../../images/recycle.gif" />
					<#elseif !build.hasRun(phase)>
						<img src="../../images/empty.gif" />
					<#else>
						<img src="../../images/notok.gif" />
					</#if>
				</#list>
			</td>
		</tr>
	</#list>
</table>
