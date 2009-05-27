<h2>${build.revision}</h2>
<ul>
	<c:forEach var="file" items="${build.content}">
		<li>(<a
			href="${pageContext.request.contextPath }/project/${project.name}/${build.revision}/${file.name }">view
		</a>) ${file.name }</li>
	</c:forEach>
</ul>