<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<html>
<c:if test="${not empty project}">
	Building ${project.name} @ ${project.uri}<br />
	Thread ${thread.alive }<br />
	Stacktrace: <br />
	<textarea><c:forEach var="trace"
		items="${thread.stackTrace}">${trace.declaringClass}.${trace.methodName }:${trace.lineNumber }
</c:forEach></textarea>
</c:if>
<c:if test="${empty project}">
	No jobs running
</c:if>
</html>