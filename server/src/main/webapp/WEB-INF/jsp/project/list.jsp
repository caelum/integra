<c:forEach var="project" items="${projectList}">
	<h2>${project.name}</h2>
	Uri: ${project.uri }<br />
	Basedir: ${project.buildsDirectory.absolutePath }<br />
	Control: ${project.controlType.name } <br />
	Actions: <a href="run?project.name=${project.name }">run</a>
	<br />
	Last build: ${project.lastBuild.time }<br />

	<table>
		<tr>
			<c:forEach var="phase" items="${project.phases }">
				<td>${phase.name } (${phase.position })</td>
				<td>--></td>
			</c:forEach>
			<td>
			<form action="phase" method="post"><input type="hidden"
				name="project.name" value="${project.name }" /> <input size="5"
				name="phase.name" value="unnamed" /> <input type="submit"
				value="new phase" /></form>
			</td>
		</tr>
		<tr>
			<c:forEach var="phase" items="${project.phases }">
				<td>
				<table>
					<c:forEach var="cmd" items="${phase.commands}">
						<tr>
							<td>${cmd.name }</td>
							<td>(<a href="command/${cmd.id}?_method=DELETE">remove</a>)</td>
						</tr>
					</c:forEach>
					<tr>
						<td>
						<form action="command" method="post"><input type="hidden"
							name="phase.id" value="${phase.id }" /> <input type="text"
							name="command" value="" size="5" /> <input type="submit"
							value="new command" /></form>
						</td>
					</tr>
					<c:forEach var="plugin" items="${phase.plugins}">
						<tr>
							<td>${plugin.type.name }</td>
							<td>(<a href="plugin/${plugin.id}?_method=DELETE">remove</a>)</td>
						</tr>
					</c:forEach>
					<tr>
						<td>
						<form action="plugin" method="post"><input type="hidden"
							name="phase.id" value="${phase.id }" /> <select
							name="pluginType">
							<c:forEach var="plugin" items="${plugins }">
								<option value="${plugin.name }">${plugin.simpleName }</option>
							</c:forEach>
						</select> <input type="submit" value="add" /></form>
						</td>
					</tr>
				</table>
				</td>
				<td></td>
			</c:forEach>
		</tr>
	</table>
	<table>
		<c:forEach var="build" items="${project.builds }">
			<tr>
				<td><a
					href="${pageContext.request.contextPath }/project/${project.name}/${build.buildCount}?filename=">results</a>
				</td>
				<td>build-${build.buildCount}</td>
				<td>revision '${build.revision }'</td>
				<td><c:if test="${not build.finished }">
					<font color="orange">building... who knows?</font>
				</c:if> <c:if test="${build.finished }">
					<c:if test="${build.successSoFar }">
						<font color="green">success</font>
					</c:if>
					<c:if test="${not build.successSoFar }">
						<font color="red">fail</font>
					</c:if>
				</c:if></td>
			</tr>
		</c:forEach>
	</table>
</c:forEach>

<form action="" method="post">
<table>
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