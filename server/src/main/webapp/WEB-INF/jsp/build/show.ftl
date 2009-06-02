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


<ul>
	<#list content as file>
		<#if file.directory>
			<li>(<a
				href="${contextPath }/project/${project.name}/build/${build.buildCount}?filename=${currentPath}${file.name }">view
			</a>) ${file.name }</li>
		<#else>
			<li>(<a
				href="${contextPath }/download/project/${project.name}/build/${build.buildCount}?filename=${currentPath}${file.name }">view
			</a>) ${file.name }</li>
		</#if>
	</#list>
</ul>