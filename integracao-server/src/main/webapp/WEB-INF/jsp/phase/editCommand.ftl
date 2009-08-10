<div id="edit_command_${command.id}" class="command formulario">
	<form action="command/${command.id}" method="post">
		<input type="hidden" name="_method" value="put" />
		<input type="hidden" name="command.id" value="${command.id}" />
		(start) <input type="text" name="startCommand" value="${command.name}" size="5" /> <br/>
		(stop) <input type="text" name="stopCommand" value="${command.stopName}" size="5" /> <br/>
		(artifacts to push) <input type="text" name="artifactsToPush" value="${command.artifactsToPushString}" size="20" /> <br/>
		(labels required @ agent) <textarea name="labels" cols="40" rows="5"><#list command.labels as label>${label.name},</#list></textarea>
		<input type="submit" value="save" />
	</form>
</div>