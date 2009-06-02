<div id="form">
	<form action="settings" method="POST">
		Hostname: <input name="config.hostname" value="${config.hostname }" /><br/>
		Port: <input name="config.port" value="${config.port }" /><br/>
		Check interval: <input name="config.checkInterval" value="${config.checkInterval }" /><br/>
		<input type="submit" value="update" />
	</form>
</div>