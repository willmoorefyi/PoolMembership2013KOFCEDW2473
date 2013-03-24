<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ attribute name="headerTitle" required="false" rtexprvalue="true" %>
<%@ attribute name="style" required="false" rtexprvalue="true" %>

<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1"/>
<title>${headerTitle}</title>

<link href="/bootstrap/css/bootstrap.min.css" rel="stylesheet"/>
<link href="/style/${empty style ? 'doc-kofc.css' : style}" rel="stylesheet"/>
<link href="/style/doc-shared.css" rel="stylesheet"/>

<script src="//ajax.googleapis.com/ajax/libs/jquery/1.9.0/jquery.min.js"><jsp:text/></script>
<script src="/js/json2.js"><jsp:text/></script>
<script src="/bootstrap/js/bootstrap.js"><jsp:text/></script>
<script src="/js/jquery.placeholder.min.js"><jsp:text/></script>