<h2>build-${build.buildCount } - revision '${build.revision}'</h2>
<h2>
<#if !build.finished>
	<font color="orange">building... who knows?</font>
<#else>
	<#if build.successSoFar>
		<font color="green">success</font>
	<#else>
		<font color="red">fail</font>
	</#if>
</#if>
</h2>

Current phase: ${build.currentPhase } <br />
Base directory: ${build.baseDirectory.absolutePath } <br />
Sucess so far: ${build.successSoFar?string } <br />
Finished: ${build.finished?string } <br />
Started at: ${build.startTime.time?datetime } <br />
<#if build.finishTime??>
	Finished at: ${build.finishTime.time?datetime} <br />
</#if>
<br />
Commands finished so far:
<#list build.executedCommandsFromThisPhase as cmd>
${cmd },
</#list>
<br />
Commands running or already run:<br/>
<#list build.project.phases as phase>
	${phase.name}
	<table>
	<#assign clients=build.getClientsFor(phase)>
	<#list clients as client>
		<tr><td>${client.executedCommand.position}</td><td>${client.executedCommand.name}</td><td>${client.client.baseUri}</td></tr>
	</#list>
	</table>
</#list>

<ul>
	<#list content as file>
		<#if file.directory>
			<li>(<a
				href="${contextPath }/project/${project.name}/build/${build.buildCount}/view/${currentPath}${file.name }">view
			</a>) ${file.name }</li>
		<#else>
			<li>(<a
				href="${contextPath }/download/project/${project.name}/build/${build.buildCount}/view/${currentPath}${file.name }">view
			</a>) ${file.name }</li>
		</#if>
	</#list>
</ul>