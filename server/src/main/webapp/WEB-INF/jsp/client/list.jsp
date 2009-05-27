<h2>Free clients list</h2>
<ul>
<c:forEach var="client" items="${clientList}">
	<li>${client.host}:${client.port }</li>
</c:forEach>
</ul>