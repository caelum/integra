<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<html>
<c:if test="${job.running}">
	Building ${job.project.name} @ ${job.project.uri}<br />
	Start time: ${job.start.time }<br/>
	Time so far: ${job.time / 60} minutes ${job.time % 60} seconds<br/>
	Thread name: ${job.thread.name} <br/>
	Thread alive: ${job.thread.alive }<br />
	Stacktrace: <br />
	<textarea cols="120" rows="20"><c:forEach var="trace"
		items="${job.thread.stackTrace}">${trace.className}.${trace.methodName }:${trace.lineNumber }
</c:forEach></textarea>
</c:if>
<c:if test="${not job.running}">
	No jobs running
</c:if>
</html>