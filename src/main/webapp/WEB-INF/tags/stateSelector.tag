<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ attribute name="cssClass" required="false" rtexprvalue="true" %>
<%@ attribute name="inputId" required="true" rtexprvalue="true" %>
<%@ attribute name="helpText" required="false" rtexprvalue="true" %>
<%@ attribute name="defaultValue" required="false" rtexprvalue="true" %>
<input id="${inputId}" 
   name="${inputId}" 
   type="text" 
   class="${cssClass}"
   placeholder="${helpText}" 
   title="${helpText}" 
   data-provide="typeahead" 
   data-items="4" 
   data-source="[&quot;AK&quot;,&quot;AL&quot;,&quot;AR&quot;,&quot;AZ&quot;,&quot;CA&quot;,&quot;CO&quot;,&quot;CT&quot;,&quot;DC&quot;,&quot;DE&quot;,&quot;FL&quot;,&quot;GA&quot;,&quot;HI&quot;,&quot;IA&quot;,&quot;ID&quot;,&quot;IL&quot;,&quot;IN&quot;,&quot;KS&quot;,&quot;KY&quot;,&quot;LA&quot;,&quot;MA&quot;,&quot;MD&quot;,&quot;ME&quot;,&quot;MI&quot;,&quot;MN&quot;,&quot;MO&quot;,&quot;MS&quot;,&quot;MT&quot;,&quot;NC&quot;,&quot;ND&quot;,&quot;NE&quot;,&quot;NH&quot;,&quot;NJ&quot;,&quot;NM&quot;,&quot;NV&quot;,&quot;NY&quot;,&quot;OH&quot;,&quot;OK&quot;,&quot;OR&quot;,&quot;PA&quot;,&quot;RI&quot;,&quot;SC&quot;,&quot;SD&quot;,&quot;TN&quot;,&quot;TX&quot;,&quot;UT&quot;,&quot;VA&quot;,&quot;VT&quot;,&quot;WA&quot;,&quot;WI&quot;,&quot;WV&quot;,&quot;WY&quot;]"
   value="${defaultValue}" 
>