<c:forEach var="project" items="${projectList}">
	<h2>${project.name}</h2>
	<ul>
		<c:forEach var="build" items="${project.builds }">
			<li>(<a
				href="${pageContext.request.contextPath }/project/${project.name}/${build.revision}">results</a>)
			${build.revision }</li>
		</c:forEach>
	</ul>
</c:forEach>
