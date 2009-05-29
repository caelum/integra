<c:forEach var="project" items="${projectList}">
	<h2>${project.name}</h2>
	Uri: ${project.uri }<br />
	Basedir: ${project.buildsDirectory.absolutePath }<br />
	Actions: <a href="run?project.name=${project.name }">run</a>
	<br />
	Last build: ${project.lastBuild.time }<br />

	<table>
		<tr>
			<c:forEach var="phase" items="${project.phases }">
				<td>${phase.position }</td>
				<td>--></td>
			</c:forEach>
			<td>
			<form action="phase" method="post"><input type="hidden"
				name="project.name" value="${project.name }" />
				<input type="hidden"
				name="phase.name" value="${unnamed }" />
				<input type="submit"
				value="new phase" /></form>
			</td>
		</tr>
		<tr>
			<c:forEach var="phase" items="${project.phases }">
				<td>
				<ul>
					<c:forEach var="cmd" items="${phase.commands}">
						<li>${cmd.name } (<a href="command/${cmd.id}?_method=DELETE">remove</a>)</li>
					</c:forEach>
					<li>
					<form action="command" method="post"><input type="hidden"
						name="phase.id" value="${phase.id }" /> <input type="text"
						name="command" value="" length="10" /> <input type="submit"
						value="new command" /></form>
				</ul>
				</td>
				<td></td>
			</c:forEach>
		</tr>
	</table>
	<ul>
		<c:forEach var="build" items="${project.builds }">
			<li>(<a
				href="${pageContext.request.contextPath }/project/${project.name}/${build.buildCount}?filename=">results</a>)
			build-${build.buildCount} : revision '${build.revision }'</li>
		</c:forEach>
	</ul>
</c:forEach>

<form action="new">Uri: <input name="project.uri" /> Name: <input
	name="project.name" /> Base directory: <input name="project.baseDir" />
</form>
