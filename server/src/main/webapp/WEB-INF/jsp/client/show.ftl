<#compress>

<form action="${client.id}" method="post" class="formulario">

	<input type="hidden" name="_method" value="put"/>

	Host: <input type="text" name="client.host" value="${client.host}" /><br/>
	Context: <input type="text" name="client.context" value="${client.context}" /><br/>
	Port: <input type="text" name="client.port" value="${client.port}" /><br/>
	
	<textarea name="labels">
	<#list client.labels as label>${label.name},</#list>
	</textarea>
	<br/>
	<input type="submit" value="update" />
	
</form>

</#compress>