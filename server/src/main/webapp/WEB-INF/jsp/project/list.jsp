<c:forEach var="project" items="${projectList}">
	<h2>${project.name}</h2>
	<ul>
	<c:forEach var="build" items="${project.builds }">
		<li>${build.revision }</li>
	</c:forEach>
	</ul>
</c:forEach>
