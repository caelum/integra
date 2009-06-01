<#list projectList as project>
	<h2>${project.name}</h2>
	Uri: ${project.uri }<br />
	Basedir: ${project.buildsDirectory.absolutePath }<br />
	Control: ${project.controlType.name } <br />
	Actions: <a href="run?project.name=${project.name }">run</a>
	<br />
	Last build: ${project.lastBuild.time?datetime }<br />

	<table>
		<tr>
			<#list project.phases as phase>
				<td>${phase.name } (${phase.position })</td>
				<td>--></td>
			</#list>
			<td>
			<form action="phase" method="post"><input type="hidden"
				name="project.name" value="${project.name }" /> <input size="5"
				name="phase.name" value="unnamed" /> <input type="submit"
				value="new phase" /></form>
			</td>
		</tr>
		<tr>
			<#list project.phases as phase>
				<td>
				<table>
					<#list phase.commands as cmd>
						<tr>
							<td>${cmd.name }</td>
							<td>(<a href="command/${cmd.id}?_method=DELETE">remove</a>)</td>
						</tr>
					</#list>
					<tr>
						<td>
						<form action="command" method="post"><input type="hidden"
							name="phase.id" value="${phase.id }" /> <input type="text"
							name="command" value="" size="5" /> <input type="submit"
							value="new command" /></form>
						</td>
					</tr>
					<#list phase.plugins as plugin>
						<tr>
							<td>${plugin.type.simpleName }
							<div id="plugin_${plugin.id }"></div>
							</td>
							<td>(<a href="#plugin_${plugin.id }" onclick="$('#plugin_${plugin.id }').load('plugin/${plugin.id}')">config</a>)</td>
							<td>(<a href="plugin/${plugin.id}?_method=DELETE">remove</a>)</td>
						</tr>
					</#list>
					<tr>
						<td>
						<form action="plugin" method="post"><input type="hidden"
							name="phase.id" value="${phase.id }" /> <select
							name="pluginType">
							<#list plugins as plugin>
								<option value="${plugin.name }">${plugin.simpleName }</option>
							</#list>
						</select> <input type="submit" value="add" /></form>
						</td>
					</tr>
				</table>
				</td>
				<td></td>
			</#list>
		</tr>
	</table>
	<table>
		<#list project.builds as build>
			<tr>
				<td><a
					href="${project.name}/build/${build.buildCount}?filename=">results</a>
				</td>
				<td>build-${build.buildCount}</td>
				<td>revision '${build.revision!'unknown' }'</td>
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
			</tr>
		</#list>
	</table>
</#list>

<form action="" method="post">
<table>
	<tr>
		<td>Uri: <input name="project.uri" /></td>
	</tr>
	<tr>
		<td>Name: <input name="project.name" /></td>
	</tr>
	<tr>
		<td>Base directory: <input name="baseDir" /></td>
	</tr>
	<tr>
		<td><input type="submit"></td>
	</tr>
</table>
</form>