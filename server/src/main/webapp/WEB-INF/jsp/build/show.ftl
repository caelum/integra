<ul>
	<#if content??>
	<#list content as file>
		<#if !file.hidden>
			<#if file.directory>
				<li>(<a
					href="${contextPath }/project/${project.name}/build/${build.buildCount}/view/${currentPath}${file.name }">view
				</a>) ${file.name }</li>
			<#else>
				<li>(<a
					href="${contextPath }/download/project/${project.name}/build/${build.buildCount}/view/${currentPath}${file.name }">view
				</a>) ${file.name }</li>
			</#if>
		</#if>
	</#list>
	<#else>
		no content found
	</#if>
</ul>
