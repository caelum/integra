<div id="form">
<form action="" method="put">
<#assign i=0>
<#list plugin.information.parameters as param>
	<div>Key <input type="text" name="key[${i}]" value="" /> = value <input type="text" name="value[${i}]" value="${plugin.get(param.key)}" /></div>
</#list>
	<div><input type="submit" value="update all" /></div>
 </form>
</div>