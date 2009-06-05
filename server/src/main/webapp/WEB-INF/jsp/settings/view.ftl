<div id="form">
	<form action="settings" method="POST">
		Hostname: <input name="config.hostname" value="${config.hostname }" /><br/>
		Port: <input name="config.port" value="${config.port }" /><br/>
		Check interval: <input name="config.checkInterval" value="${config.checkInterval }" /><br/>
		Maximum time for a job (minutes): <input name="config.maximumTimeForAJob" value="${config.maximumTimeForAJob }" /><br/>
		<input type="submit" value="update" />
	</form>
</div>

<ul>
<#list config.availablePlugins as registered>
	<li>${registered.type.simpleName}</li>
</#list>
</ul>
