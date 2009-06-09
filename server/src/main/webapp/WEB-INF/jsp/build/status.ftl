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
