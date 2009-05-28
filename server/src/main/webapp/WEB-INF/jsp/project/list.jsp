<c:forEach var="project" items="${projectList}">
	<h2>${project.name}</h2>
	Uri: ${project.uri }<br />
	Basedir: ${project.buildsDirectory.absolutePath }<br />
	Actions: <a href="run?project.name=${project.name }">run</a><br/>
	Last build: ${project.lastBuild.time }<br/>

	<table>
		<tr>
			<c:forEach var="phase" items="${project.phases }">
				<td>${phase.id }</td>
				<td>--></td>
			</c:forEach>
			<td><a href="phase?_method=post&project.name=${project.name }&phase.id=unnamed">new phase</a></td>
		</tr>
		<tr>
			<c:forEach var="phase" items="${project.phases }">
				<td>
				<ul>
					<c:forEach var="cmd" items="${phase.commands}">
						<li>${cmd.name }</li>
					</c:forEach>
					<li><a href="command?_method=post&project.name=${project.name }&phase.position=${phase.phasePosition}">new command</a></li>
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