<h2>build-${build.buildCount } - revision '${build.revision}'</h2>

Current phase: ${build.currentPhase }<br/>
Base directory: ${build.baseDirectory.absolutePath }<br/>
Sucess so far: ${build.successSoFar }<br/>
Finished: ${build.finished }<br/>
Commands finished so far:
<c:forEach var="cmd" items="${build.executedCommandsFromThisPhase}">
${cmd },
</c:forEach><br/>

<ul>
	<c:forEach var="file" items="${content}">
		<c:if test="${file.directory }">
		<li>(<a
			href="${pageContext.request.contextPath }/project/${project.name}/${build.buildCount}?filename=${currentPath}${file.name }">view
		</a>) ${file.name }</li>
		</c:if>
		<c:if test="${not file.directory }">
			<li>(<a
				href="${pageContext.request.contextPath }/download/project/${project.name}/${build.buildCount}?filename=${currentPath}${file.name }">view
			</a>) ${file.name }</li>
		</c:if>
	</c:forEach>
</ul>