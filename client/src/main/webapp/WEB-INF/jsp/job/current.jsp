<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<html>
<c:if test="${not empty job}">
	Building ${job.project.name} @ ${job.project.uri}<br />
	Thread ${job.thread.alive }<br />
	Stacktrace: <br />
	<textarea><c:forEach var="trace"
		items="${job.thread.stackTrace}">${trace.className}.${trace.methodName }:${trace.lineNumber }
</c:forEach></textarea>
</c:if>
<c:if test="${empty job}">
	No jobs running
</c:if>
</html>