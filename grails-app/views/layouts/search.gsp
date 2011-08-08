<%@ page cntentType="text/html;charset=UTF-8" %>
<html>
	<head>
		<title><g:layoutTitle default="Search"/></title>
		<g:layoutHead />
		<g:render template="/css"/>
		<link rel="shortcut icon" href="${resource(dir:'images',file:'favicon.ico')}" type="image/x-icon" />
		<g:javascript library="jquery" plugin="jquery"/>
		<jqui:resources theme="medium" plugin="mumsnet"/>
		<g:javascript src="application.js"/>
		<g:javascript src="message/actions.js"/>
		<g:javascript src="mediumPopup.js"/>
		<g:javascript src="smallPopup.js"/>
	</head>
	<body>
		<div id="container">
			<g:render template="/system_menu"/>
			<g:render template="/tabs"/>
	        <g:render template="/flash"/>
	        <div class="main">
				<g:render template="menu"/>
				<div class="content">
					<div id='search-header' class="content-header">
						<div id="search-title">
							<h2>Search</h2>
			  			</div>
			  			<ol>
				  			<li>
					  			<g:remoteLink controller="export" action="wizard" params='[messageSection: "${messageSection}", ownerId: "${ownerInstance?.id}", activityId: "${activityId}", searchString: "${searchString}", groupId: "${groupInstance?.id}"]' onSuccess="launchSmallPopup(data, 'Export');">
									Export
								</g:remoteLink>
							</li>
						</ol>
						<p id="search-description">
							${searchDescription}
				  		</p>
					</div>
					<div class="content-body">
						<g:render template="/message/message_list"/>
						<g:layoutBody />
					</div>
					<div class="content-footer">
							<ul id="filter">
								<li>Show:</li>
								<li><g:link action="${messageSection}" params="${params.findAll({it.key != 'starred' && it.key != 'max' && it.key != 'offset'})}">All</g:link></li>
								<li>|</li>
								<li><g:link action="${messageSection}" params="${params.findAll({it.key != 'max' && it.key != 'offset'}) + [starred: true]}" >Starred</g:link></li>
							</ul>
							<g:if test="${params.action == 'results'}">
								<div id="page-arrows">
									<g:paginate next="Forward" prev="Back"
										 max="${grailsApplication.config.pagination.max}"
										action="${messageSection}" total="${messageInstanceTotal}" params= "${params.findAll({it.key != 'messageId'})}"/>
								</div>
							</g:if>
					</div>
				</div>
			</div>
		</div>
	</body>
</html>
