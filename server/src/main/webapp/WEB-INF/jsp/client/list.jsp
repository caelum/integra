<h2>Clients list</h2>
<table>
	<c:forEach var="client" items="${free}">
		<tr>
			<td>${client.host }</td>
			<td>${client.port }</td>
			<td>free</td>
			<td><a href="${client.id }/deactivate">deactivate</a></td>
		</tr>
	</c:forEach>
	<c:forEach var="client" items="${locked}">
		<tr>
			<td>${client.host }</td>
			<td>${client.port }</td>
			<td><font color="red">(busy)</font> ${client.currentJob }</td>
			<td><a href="http://${client.host }:${client.port }${client.context }/job/current">ask for its job</a></td>
			<td><a href="http://${client.host }:${client.port }${client.context }/job/stop">stop</a></td>
		</tr>
	</c:forEach>
	<c:forEach var="client" items="${inactive}">
		<tr>
			<td>${client.host }</td>
			<td>${client.port }</td>
			<td>inactive</td>
			<td><a href="${client.id }/activate">activate</a></td>
		</tr>
	</c:forEach>
</table>