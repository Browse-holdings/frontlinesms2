<%@ page contentType="text/html;charset=UTF-8"%>
<html>
	<head>
		<title><g:layoutTitle default="Messages"/></title>
		<g:layoutHead/>
		<r:require module="messages"/>
		<g:render template="/includes"/>
		<g:javascript>
			$(function() {  
			   disablePaginationControls();
			});
		</g:javascript>
	</head>
	<body id="messages-tab">
		<div id="header">
			<div id="notifications">
				<g:render template="/system_notifications"/>
				<g:render template="/flash"/>
			</div>
			<g:render template="/system_menu"/>
			<g:render template="/tabs"/>
		</div>
		<div id="main">
			<g:render template="../message/menu" plugin="${grailsApplication.config.frontlinesms2.plugin}"/>
			<div id="content">
				<g:render template="../message/message_list"/>
				<g:layoutBody/>
			    <g:render template="../message/message_details" />
			</div>
		</div>
		<r:layoutResources/>
	</body>
</html>
