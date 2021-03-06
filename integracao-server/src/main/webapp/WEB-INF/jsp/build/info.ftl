<#compress>
<h2>build-${build.buildCount } - revision '${build.revisionName}'</h2>
<pre>${build.resultMessage!''}</pre>

<#if !build.finished>
	<font color="orange">elapsed time: ${build.runtime} minutes</font>
<#else>
	<#if build.successSoFar>
		<font color="green">finish time: ${build.runtime} minutes</font>
	<#else>
		<font color="red">finish time: ${build.runtime} minutes</font>
	</#if>
</#if>
</h2>

<div id="tabs">
     <ul>
         <li><a href="#tabContent" onclick="$('#tabContent').attr('src','${contextPath }/project/${project.name}/build/${build.buildCount}/status');"><span>Status</span></a></li>
         <li><a href="#tabContent" onclick="$('#tabContent').attr('src','${contextPath }/project/${project.name}/build/${build.buildCount}/commands');"><span>Commands</span></a></li>
         <li><a href="#tabContent" onclick="$('#tabContent').attr('src','${contextPath }/project/${project.name}/build/${build.buildCount}/view/');"><span>Output</span></a></li>
<#list build.tabs as tab>
         <li><a href="#tabContent" onclick="$('#tabContent').attr('src','${contextPath }/download/project/${project.name}/build/${build.buildCount}/view/${tab.path}');"><span>${tab.name}</span></a></li>
</#list>
     </ul>
</div>
<iframe id="tabContent" src="${contextPath }/project/${project.name}/build/${build.buildCount}/status">
</iframe>

</#compress>
