<#compress>

Commands running or already run:<br/>
<#list build.project.phases as phase>
	<h2>${phase.name}</h2>
	<table>
		<tr>
			<td>id</td>
			<td>view</td>
			<td>status</td>
			<td>command</td>
			<td>scheduled</td>
			<td>client</td>
			<td>started</td>
			<td>finished</td>
		</tr>
	<#assign jobs=build.getJobsFor(phase)>
	<#list jobs as job>
		<tr>
			<td>${job.command.id}</td>
			<td>
			<a
				href="${contextPath }/project/${project.name}/build/${build.buildCount}/view/${job.command.id}">view
			</a>
			</td>
			<td>
			<#if job.finishTime??>
				<#if job.success>
					<font color="green">success</font>
				<#else>
					<font color="red">fail</font>
				</#if>
			</#if>
			</td>
			<td>${job.command.name}</td>
			<td>${job.schedulingTime.time?datetime}</td>
			<td>
			<#if job.client??>
				${job.client.host}
			</#if>
			</td>
			<td>
			<#if job.startTime??>
				${job.startTime.time?datetime}
			</#if>
			</td>
			<td>
			<#if job.finishTime??>
				${job.finishTime.time?datetime}
			</#if>
			</td>
			<td>
			<#if job.finishTime??>
				${job.runtime} seconds
			</#if>
			</td>
		</tr>
	</#list>
	</table>
</#list>
</#compress>
