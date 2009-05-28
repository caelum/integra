<h2>Free clients list</h2>
<table>
	<c:forEach var="client" items="${free}">
		<tr>
			<td>${client.host }</td>
			<td>${client.port }</td>
			<td>free</td>
		</tr>
	</c:forEach>
	<c:forEach var="client" items="${locked}">
		<tr>
			<td>${client.host }</td>
			<td>${client.port }</td>
			<td>${client.currentJob }</td>
		</tr>
	</c:forEach>
</table>