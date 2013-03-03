<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ attribute name="inputId" required="true" rtexprvalue="true" %>
<%@ attribute name="inputValue" required="true" rtexprvalue="true" %>
<%@ attribute name="inputName" required="true" rtexprvalue="true" %>
<%@ attribute name="inputText" required="true" rtexprvalue="true" %>
<div class="control-group span10">
   <label class="radio"><input id="${inputId}" value="${inputValue}" name="${inputName}" type="radio"/>${inputText}</label>
</div>