<div id="form">
<form action="" method="put">
<#list plugin.information.parameters as param>
	<div>Key <input type="text" name="key" value="" /> = value <input type="text" name="value" value="" /></div>
</#list>
	<div><input type="submit" value="update all" /></div>
 </form>
</div>