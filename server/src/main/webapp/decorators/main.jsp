<%@ taglib uri="http://www.opensymphony.com/sitemesh/decorator" prefix="decorator" %>
<%@ taglib uri="http://www.opensymphony.com/sitemesh/page" prefix="page" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>

<%@ page contentType="text/html; charset=ISO-8859-1" %>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html>

<head>
	<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1" />
	<title><decorator:title default="Integracao" /></title>
	<div id="actions">
		<a href="${pageContext.request.contextPath }/client/form">new client</a>
		<a href="${pageContext.request.contextPath }/client/list">list clients</a>
		<a href="${pageContext.request.contextPath }/project/">list projects</a>
		<a href="${pageContext.request.contextPath }/settings">settings</a>
		<br/>
		<a href="${pageContext.request.contextPath }/project/addAll?myUrl=192.168.0.2:9100">add all @ 2:9100</a>
		<a href="${pageContext.request.contextPath }/project/addAll?myUrl=localhost:9091">add all @ localhost:9091</a>
		<a href="${pageContext.request.contextPath }/project/addAll?myUrl=192.168.0.159:9091">add all @ 159:9091</a>
	</div>
	<link rel="stylesheet" type="text/css" media="all" href="/integracao/css/integracao.css" />
</head>

	<body>
		<script type="text/javascript" src="/integracao/js/jquery-1.3.2.min.js" charset="ISO-8859-1"></script>
		<div id="content">
			<decorator:body />
		</div>
	</body>

</html>