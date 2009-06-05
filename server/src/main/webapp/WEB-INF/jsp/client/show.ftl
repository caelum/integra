<#compress>

<form action="${client.id}" method="post" class="formulario">

	<input type="hidden" name="_method" value="put"/>

	Host: <input type="text" name="client.host" value="${client.host}" />
	Context: <input type="text" name="client.context" value="${client.context}}" />
	Port: <input type="text" name="client.port" value="${client.port}}" />
	
	<textarea name="tags">
	<#list client.tags as tag>${tag.name},</#list>
	</textarea>
	
	<input type="submit" value="update" />
	
</form>

</#compress>