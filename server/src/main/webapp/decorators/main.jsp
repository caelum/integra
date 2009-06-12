<%@ taglib uri="http://www.opensymphony.com/sitemesh/decorator" prefix="decorator" %>
<%@ taglib uri="http://www.opensymphony.com/sitemesh/page" prefix="page" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ page contentType="text/html; charset=ISO-8859-1" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1" />
	<meta http-equiv="refresh" content="40" />
	<title><decorator:title default="Integracao" /></title>
	<div class="actions">
		<a href="${pageContext.request.contextPath }/client/form">new client</a>
		<a href="${pageContext.request.contextPath }/client/list">list clients</a>
		<a href="${pageContext.request.contextPath }/project/">list projects</a>
		<a href="${pageContext.request.contextPath }/settings">settings</a>
		<a href="${pageContext.request.contextPath }/jobs">jobs</a>
		<br/>
	</div>
	<link rel="stylesheet" type="text/css" media="all" href="/integracao/css/integracao.css" />
	<link rel="stylesheet" type="text/css" media="all" href="/integracao/css/jquery-ui-1.7.2.custom.css" />
</head>
	<body>
		<script type="text/javascript" src="/integracao/js/jquery-1.3.2.min.js" charset="ISO-8859-1"></script>
		<script type="text/javascript" src="/integracao/js/jquery-ui-1.7.2.custom.min.js" charset="ISO-8859-1"></script>
		<div id="content">
			<decorator:body />
		</div>
	</body>
</html>