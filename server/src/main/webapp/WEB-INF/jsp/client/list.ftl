<h2>Clients list</h2>
<table>
	<#list free as client>
		<tr>
			<td>${client.host }</td>
			<td>${client.port }</td>
			<td>free</td>
			<td>${client.alive?string('alive', 'dead')}</td>
			<td><a href="${client.id }/deactivate">deactivate</a></td>
		</tr>
	</#list>
	<#list locked as client>
		<tr>
			<td>${client.host }</td>
			<td>${client.port }</td>
			<td><font color="red">(busy)</font> ${client.currentJob }</td>
			<td><a href="http://${client.host }:${client.port }${client.context }/job/current">ask for its job</a></td>
			<td><a href="http://${client.host }:${client.port }${client.context }/job/stop">stop</a></td>
		</tr>
	</#list>
	<#list inactive as client>
		<tr>
			<td>${client.host }</td>
			<td>${client.port }</td>
			<td>inactive</td>
			<td><a href="${client.id }/activate">activate</a></td>
		</tr>
	</#list>
</table>