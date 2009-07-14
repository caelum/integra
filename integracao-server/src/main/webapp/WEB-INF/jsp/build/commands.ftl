<#compress>

Commands running or already run:<br/>
<#list build.project.phases as phase>
	<h2>${phase.name}</h2>
	<table class="tabela">
		<thead>
			<tr>
				<th>id</th>
				<th>view</th>
				<th>status</th>
				<th>command</th>
				<th>scheduled</th>
				<th>client</th>
				<th>started</th>
				<th>finished</th>
			</tr>
		</thead>
		<#assign jobs=build.getJobsFor(phase)>
		<tbody>
		<#list jobs as job>
			<tr>
				<td>${job.command.id}</td>
				<td>
				<a
					href="${contextPath }/download/project/${project.name}/build/${build.buildCount}/view/${phase.name}/${job.command.name}.txt">view
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
		</tbody>
	</table>
</#list>
</#compress>
