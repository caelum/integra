<h2>${project.name}</h2>
<div class="box">
Uri: ${project.uri }<br />
Basedir: ${project.buildsDirectory.absolutePath }<br />
Control: ${project.controlType.name } <br />
Actions: <a href="run?_method=post">force build</a>
<br />
Last build: ${project.lastBuildTime.time?datetime }<br />
</div>
<div class="plugin">
	<div class="plugin_title">Plugins</div>
	<#list project.plugins as plugin>
		<span class="plugin_title">${plugin.type.information.name }</span>
		<span class="commands">
			(<a href="#plugin_${plugin.id }" onclick="$('#plugin_${plugin.id }').load('plugin/${plugin.id}')">config</a>)
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

<table>
	<tr>
		<#list project.phases as phase>
			<td>
			<div class="phase">
				<div class="phase_title">${phase.name } (${phase.position })</div>
				<#list phase.commands as cmd>
				<div class="command">
					${cmd.name }
					<#if cmd.stopName??>(stop: ${cmd.stopName})</#if>
					"<#list cmd.labels as label>${label.name},</#list>"
					(<a href="command/${cmd.id}?_method=DELETE">remove</a>)
				</div>
				</#list>
				<div class="command formulario">
					<form action="command" method="post">
						<input type="hidden" name="phase.id" value="${phase.id }" />
						(start) <input type="text" name="startCommand" value="" size="5" /> <br/>
						(stop) <input type="text" name="stopCommand" value="" size="5" /> <br/>
						(labels) <textarea name="labels"></textarea>
						<input type="submit" value="add" />
					</form>
				</div>
				<#list phase.plugins as plugin>
				<div class="plugin">
						<span class="plugin_title">${plugin.type.information.name }</span>
						<span class="commands">
							(<a href="#plugin_${plugin.id }" onclick="$('#plugin_${plugin.id }').load('plugin/${plugin.id}')">config</a>)
							(<a href="phase/${phase.id}/plugin/${plugin.id}?_method=DELETE">remove</a>)
						</span>
						<div id="plugin_${plugin.id }"></div>
				</div>
				</#list>
				<div class="plugin, formulario">
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
			</td>
			<td> --> </td>
		</#list>
		<td>
		<form action="phase" method="post" class="formulario"><input type="hidden"
			name="project.name" value="${project.name }" />
			Name: <input size="10" name="phase.name" value="unnamed" />
			Directories: <input size="10" name="phase.directoriesToCopy" value="" />
			<input type="submit"
			value="new phase" /></form>
		</td>
	</tr>
</table>
<table>
	<#list project.builds as build>
		<tr>
			<td><a
				href="build/${build.buildCount}/info">results</a>
			</td>
			<td>build-${build.buildCount}</td>
			<td>
				revision '${build.revisionName }'
			</td>
			<td>
			<#if !build.finished>
				<font color="orange">building... who knows?</font>
			<#else>
				<#if build.successSoFar>
					<font color="green">success</font>
				<#else>
					<font color="red">fail</font>
				</#if>
			</#if>
			</td>
			<td>
				<#list project.phases as phase>
					<div class="phase" style="
						<#if build.hasRun(phase)>
							<#if build.hasSucceeded(phase)>
								background-color: green;
							<#else>
								background-color: red;
							</#if>
						<#elseif build.isRunning(phase)>
							background-color: orange;
						<#else>
							background-color: gray;
						</#if>
						float: left;
						">
						<div class="phase_title">${phase.name}</div>
					</div>
				</#list>
			</td>
		</tr>
	</#list>
</table>
