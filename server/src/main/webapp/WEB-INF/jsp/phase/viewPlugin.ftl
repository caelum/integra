<div id="form">
<form action="plugin/${plugin.id}" method="post">
	<input type="hidden" name="_method" value="put" />
<#assign i=0>
${plugin.information.name}
<#list plugin.information.parameters as param>
	<div> <input type="hidden" name="keys[${i}]" value="${param.name}" />${param.name}:
	<#if param.type.parameterName="TEXTAREA">
	 	<textarea name="values[${i}]">${plugin.getParameter(param.name).value}</textarea> 
	<#else>
	 	<input type="text" name="values[${i}]" value="${plugin.getParameter(param.name).value}" />
	 </#if>
	 </div>
	<#assign i=i+1>
</#list>
	<div><input type="submit" value="update all" /></div>
 </form>
</div>