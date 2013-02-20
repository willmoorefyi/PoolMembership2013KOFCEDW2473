<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ attribute name="modalId" required="true" rtexprvalue="true" type="java.lang.String" %>
<%@ attribute name="titleElemId" required="false" rtexprvalue="true" type="java.lang.String" %>
<%@ attribute name="titleText" required="false" rtexprvalue="true" type="java.lang.String" %>
<%@ attribute name="bodyElemId" required="false" rtexprvalue="true" type="java.lang.String" %>
<%@ attribute name="closeAllowed" required="false" rtexprvalue="true" type="java.lang.Boolean" %>
<%@ attribute name="confirmElemId" required="false" rtexprvalue="true" type="java.lang.String" %>
<%@ attribute name="confirmText" required="false" rtexprvalue="true" type="java.lang.String" %>

<c:if test="${titleElemId == null}"><c:set var="titleElemId" value="modalHeader" /></c:if>
<c:if test="${bodyElemId == null}"><c:set var="bodyElemId" value="modalBody" /></c:if>
<c:if test="${closeAllowed == null}"><c:set var="closeAllowed" value="true" /></c:if>
<c:if test="${confirmElemId == null}"><c:set var="confirmElemId" value="modalConfirm" /></c:if>
<c:if test="${confirmText == null}"><c:set var="confirmText" value="true" /></c:if>

<div id="${modalId}" class="modal hide fade" tabindex="-1" role="dialog" aria-labelledby="${titleElemId}" aria-hidden="true">
	<div class="modal-header">
		<c:if test="${closeAllowed}"><button type="button" class="close" data-dismiss="modal" aria-hidden="true">x</button></c:if>
		<h3 id="${titleElemId}">${titleText}</h3>
	</div>
	<div class="modal-body">
		<p id="${bodyElemId}"><jsp:doBody/></p>
	</div>
	<div class="modal-footer">
      <c:if test="${closeAllowed}"><button class="btn" data-dismiss="modal" aria-hidden="true">Close</button></c:if>
		<button id="${confirmElemId}" class="btn btn-primary">${confirmText}</button>
	</div>
</div>