<div id="form">
<form action="plugin/${plugin.id}" method="post">
	<input type="hidden" name="_method" value="put" />
<#assign i=0>
${information.name}
<#list information.parameters as param>
	<div> <input type="hidden" name="keys[${i}]" value="${param.name}" />${param.name}:
	<#if param.type.parameterName="TEXTAREA">
	 	<textarea name="values[${i}]">${param.defaultValue}</textarea> 
	<#else>
	 	<input type="text" name="values[${i}]" value="${param.defaultValue}" />
	 </#if>
	 </div>
	<#assign i=i+1>
</#list>
	<div><input type="submit" value="update all" /></div>
 </form>
</div>