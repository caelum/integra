<h2>Agents list</h2>
<table class="box large">
	<thead>
		<tr>
			<th>edit</th>
			<th>active</th>
			<th>host:port</th>
			<th>labels</th>
			<th>status</th>
			<th></th>
			<th></th>
			<th></th>
		</tr>
	</thead>
	<tbody>
		<#list free as client>
			<tr>
				<td><a href="#edit_client" onclick="$('#edit_client').load('show/${client.id}')">edit</a></td>
				<td><a href="${client.id }/deactivate?_method=post">deactivate</a></td>
				<td>${client.host }:${client.port }</td>
				<td><#list client.labels as label>${label.name},</#list></td>
				<td>free and ${client.alive?string('alive', 'dead')}</td>
			</tr>
		</#list>
		<#list locked as client>
			<tr>
				<td></td>
				<td><a href="${client.id }/deactivate?_method=post">deactivate</a></td>
				<td>${client.host }:${client.port }</td>
				<td><#list client.labels as label>${label.name},</#list></td>
				<#if client.currentJob??>
					<td><font color="red">(busy)</font> #${client.currentJob.id }/${client.currentJob.build.buildCount} ${client.currentJob.command.name}</td>
				<#else>
					<td>no job!</td>
				</#if>
				<td><a href="http://${client.host }:${client.port }${client.context }/job/current">ask for its job</a></td>
				<td><a href="${client.id}/stop?_method=post">stop</a></td>
			</tr>
		</#list>
		<#list inactive as client>
			<tr>
				<td></td>
				<td><a href="${client.id }/activate?_method=post">activate</a></td>
				<td>${client.host }:${client.port }</td>
				<td><#list client.labels as label>${label.name},</#list></td>
				<td>inactive</td>
			</tr>
		</#list>
	</tbody>
</table>

<div id="edit_client">
</div>

<div id="new_client" class="box">
	<div class="subtitle">New agent</div>
	<form action="add" method="post">
		Host: <input type="text" name="client.host" value="127.0.0.1" />
		Context: <input type="text" name="client.context" value="/integracao-client" />
		Port: <input type="text" name="client.port" value="8080" />
	<input type="submit" />
	</form>
</div>
