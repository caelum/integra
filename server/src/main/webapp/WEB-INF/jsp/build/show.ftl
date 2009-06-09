<#compress>
<h2>build-${build.buildCount } - revision '${build.revisionName}'</h2>
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

<div id="tabs">
     <ul>
<#list build.tabs as tab>
         <li><a href="${contextPath }/project/${project.name}/build/${build.buildCount}/view/${tab.path}"><span>${tab.name}</span></a></li>
</#list>
     </ul>
</div>

<div class="box">
	Log:
	<#if build.revision??>
		<pre>${build.revision.message?html}</pre>
	<#else>
		unknown
	</#if>
</div>

Current phase: ${build.currentPhase } <br />
Base directory: ${build.baseDirectory.absolutePath } <br />
Sucess so far: ${build.successSoFar?string } <br />
Finished: ${build.finished?string } <br />
Started at: ${build.startTime.time?datetime } <br />
<#if build.finishTime??>
	Finished at: ${build.finishTime.time?datetime} <br />
</#if>
<br />
Commands running or already run:<br/>
<#list build.project.phases as phase>
	<h2>${phase.name}</h2>
	<table>
		<tr>
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
			<td>
			<a
				href="${contextPath }/project/${project.name}/build/${build.buildCount}/view/">view
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
				${job.client.baseUri}
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

<ul>
	<#if content??>
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
	<#else>
		no content found
	</#if>
</ul>
</#compress>
