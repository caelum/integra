<#list jobList as job>
	<h2>${job.command.phase.project.name}</h2>
	Build revision: ${job.build.revision }<br />
	Build count: ${job.build.buildCount }<br />
	Command: ${job.command.name}<br/>
</#list>