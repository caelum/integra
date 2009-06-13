Close statement count: ${statistics.closeStatementCount}<br/>
Collection fetch count: ${statistics.collectionFetchCount}<br/>
Collection load count: ${statistics.collectionLoadCount}<br/>
Collection update count: ${statistics.collectionUpdateCount}<br/>
Connect count: ${statistics.connectCount}<br/>
Entity delete count: ${statistics.entityDeleteCount}<br/>
Entity fetch count: ${statistics.entityFetchCount}<br/>
Entity insert count: ${statistics.entityInsertCount}<br/>
Entity load count: ${statistics.entityLoadCount}<br/>
Entity update count: ${statistics.entityUpdateCount}<br/>
Flush count: ${statistics.flushCount}<br/>

Session open count: ${statistics.sessionOpenCount}<br/>
Session close count: ${statistics.sessionCloseCount}
<#if statistics.sessionOpenCount-statistics.sessionCloseCount &gt; 1>
	<font color="red">(connection leaked)</font>
</#if><br/>

Transaction count: ${statistics.transactionCount}<br/>
Successful Transaction count: ${statistics.successfulTransactionCount}<br/>

<table>
	<tr><td>Query</td><td>Execution count</td><td>Execution max time</td><td>Average time</td></tr>
<#list statistics.queries as q>
	<#assign stat = statistics.getQueryStatistics(q)>
	<tr><td>${q}</td><td>${stat.executionCount}</td><td>${stat.executionMaxTime}</td><td>${stat.executionAvgTime}</td></tr>
</#list>
</table>
