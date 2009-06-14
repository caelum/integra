<h2>Projects</h2>

<#list projectList as project>
	<a href="${project.name}/run?_method=post">force build</a>
	<a href="${project.name}/">view</a>
	<span class="title">${project.name}</span> -
	<#if project.lastBuild??>
		<#assign build = project.lastBuild>
		<a href="${project.name}/build/${build.buildCount}/info">build ${build.buildCount}</a>
		<#if !build.finished>
			<font color="orange">is building</font>
		<#else>
			<#if build.successSoFar>
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
		<td><input type="checkbox" name="project.buildEveryRevision" checked="true" /> do not skip revisions while building</td>
	</tr>
	<tr>
		<td><input type="checkbox" name="project.allowAutomaticStartNextRevisionWhileBuildingPrevious" checked="true" /> allow automatic start of the next revision while building the previous one</td>
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