<c:forEach var="project" items="${projectList}">
	<h2>${project.name}</h2>
	Uri: ${project.uri }<br/>
	Uri: ${project.buildsDirectory.absolutePath }<br/>
	<ul>
		<c:forEach var="build" items="${project.builds }">
			<li>(<a
				href="${pageContext.request.contextPath }/project/${project.name}/${build.buildCount}?filename=">results</a>)
			build-${build.buildCount} : revision '${build.revision }'</li>
		</c:forEach>
	</ul>
</c:forEach>
