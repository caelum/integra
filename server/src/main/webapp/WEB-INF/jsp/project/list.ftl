<h2>Projects</h2>

<#list projectList as project>
	<a href="${project.name}/">view</a>
	<span class="title">${project.name}</span> -
	<#if project.lastBuild??>
		build ${project.lastBuild.buildCount}
		<#if !project.lastBuild.finished>
			<font color="orange">is building</font>
		<#else>
			<#if project.lastBuild.successSoFar>
				<font color="green">was a success</font>
			<#else>
				<font color="red">failed</font>
			</#if>
		</#if>
	<#else>
		<font color="orange">was not built yet</font>
	</#if>
	<br/>
</#list>

<br/><br/>

<form action="" method="post" class="formulario">
<table>
	<tr>
		<td>Scm: <select name="scmType">
		<option value="br.com.caelum.integracao.server.scm.svn.SvnControl">svn</option>
		<option value="br.com.caelum.integracao.server.scm.git.GitControl">git</option>
		</select></td>
	</tr>
	<tr>
		<td>Uri: <input name="project.uri" /></td>
	</tr>
	<tr>
		<td>Name: <input name="project.name" /></td>
	</tr>
	<tr>
		<td>Base directory: <input name="baseDir" /></td>
	</tr>
	<tr>
		<td><input type="submit"></td>
	</tr>
</table>
</form>