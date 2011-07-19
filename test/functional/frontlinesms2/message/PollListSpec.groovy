package frontlinesms2.message

import frontlinesms2.*

class PollListSpec extends frontlinesms2.poll.PollGebSpec {
	def 'poll message list is displayed'() {
		given:
			createTestPolls()
			createTestMessages()
		when:
			to PollListPage
			def pollMessageSources = $('#messages tbody tr td:nth-child(3)')*.text()
		then:
			at PollListPage
			pollMessageSources == ['Alice', 'Bob']
		cleanup:
			deleteTestPolls()
			deleteTestMessages()
	}

	def "message's poll details are shown in list"() {
		given:
			createTestPolls()
			createTestMessages()
		when:
			go "message/poll/${Poll.findByTitle('Football Teams').id}/show/${Fmessage.findBySrc('Bob').id}"
			def rowContents = $('#messages tbody tr:nth-child(2) td')*.text()
		then:
			rowContents[2] == 'Bob'
			rowContents[3] == 'manchester ("I like manchester")'
			rowContents[4] ==~ /[0-9]{2}-[A-Z][a-z]{2}-[0-9]{4} [0-9]{2}:[0-9]{2}/
		cleanup:
			deleteTestPolls()
			deleteTestMessages()
	}

	def "poll details are shown in header"() {
		given:
			createTestPolls()
			createTestMessages()
		when:
			go "message/poll/${Poll.findByTitle('Football Teams').id}/show/${Fmessage.findBySrc('Bob').id}"
			def pollTitle = $('#poll-title').text()
			def statsLabels = $('#poll-stats tbody tr td:first-child')*.text()
			def statsNums = $('#poll-stats tbody tr td:nth-child(2)')*.text()
			def statsPercents = $('#poll-stats tbody tr td:nth-child(3)')*.text()
		then:
			pollTitle == 'Football Teams'
			statsLabels == ['Unknown', 'manchester', 'barcelona']
			statsNums == ['0', '2', '0']
			statsPercents == ['(0%)', '(100%)', '(0%)']
		cleanup:
			deleteTestPolls()
			deleteTestMessages()
	}

	def 'selected poll is highlighted'() {
		given:
			createTestPolls()
			createTestMessages()
		when:
			go "message/poll/${Poll.findByTitle('Football Teams').id}/show/${Fmessage.findBySrc('Bob').id}"
		then:
			selectedMenuItem.text() == 'Football Teams'
		cleanup:
			deleteTestPolls()
			deleteTestMessages()
	}

	def "reply option should not be available for messages listed in poll section"() {
		given:
			createTestPolls()
			createTestMessages()
		when:
			go "message/poll/${Poll.findByTitle('Football Teams').id}/show/${Fmessage.findBySrc('Bob').id}"
		then:
		    !$('a', text:'Reply')
		cleanup:
			deleteTestPolls()
			deleteTestMessages()
	}

	def "should filter poll response messages for starred and unstarred messages"() {
		given:
			createTestPolls()
			createTestMessages()
		when:
			go "message/poll/${Poll.findByTitle('Football Teams').id}/show/${Fmessage.findBySrc('Bob').id}"
		then:
			$("#messages tbody tr").size() == 2
		when:
			$('a', text:'Starred').click()
			waitFor {$("#messages tbody tr").size() == 1}
		then:
			$("#messages tbody tr")[0].find("td:nth-child(3)").text() == 'Bob'
		when:
			$('a', text:'All').click()
			waitFor {$("#messages tbody tr").size() == 2}
		then:
			$("#messages tbody tr").collect {it.find("td:nth-child(3)").text()}.containsAll(['Bob', 'Alice'])
		cleanup:
			deleteTestPolls()
			deleteTestMessages()
	}
	
	def "forward option should not be available for messages listed in poll section"() {
		given:
			createTestPolls()
			createTestMessages()
		when:
				go "message/poll/${Poll.findByTitle('Football Teams').id}/show/${Fmessage.findBySrc('Bob').id}"
		then:
		    !$('a', text:'Foward')
		cleanup:
			deleteTestPolls()
			deleteTestMessages()
	}
	
	def "should only display message details when one message is checked"() {
		given:
			createTestPolls()
			createTestMessages()
		when:
			go "message/poll/${Poll.findByTitle('Football Teams').id}/show/${Fmessage.findBySrc('Bob').id}"
			$("#message")[1].click()
			$("#message")[2].click()
		then:
			$('#message-details p:nth-child(1)').text() == "2 messages selected"
		when:
			$("#message")[2].click()
			def message = Fmessage.findBySrc('Alice')
		then:
			$('#message-details p:nth-child(1)').text() == message.src
			$('#message-details p:nth-child(4)').text() == message.text
		
		cleanup:
			deleteTestPolls()
			deleteTestMessages()
	}

	def "should display message count when multiple messages are selected"() {
		given:
			createTestPolls()
			createTestMessages()
		when:
			go "message/poll/${Poll.findByTitle('Football Teams').id}/show/${Fmessage.findBySrc('Bob').id}"
			$("#message")[1].click()
			$("#message")[2].click()
		then:
			$('#message-details p:nth-child(1)').text() == "2 messages selected"
		cleanup:
			deleteTestPolls()
			deleteTestMessages()
	}
	
	def "'Reply All' button appears for multiple selected messages and works"() {
		given:
			createTestPolls()
			createTestMessages()
			new Contact(name: 'Alice', primaryMobile: 'Alice').save(failOnError:true)
			new Contact(name: 'June', primaryMobile: '+254778899').save(failOnError:true)
		when:
			go "message/poll/${Poll.findByTitle('Football Teams').id}/show/${Fmessage.findBySrc('Bob').id}"
			$("#message")[1].click()
			$("#message")[2].click()
			waitFor {$('#message-details div.buttons').text().contains("Reply All")}
			def btnReply = $('#message-details div.buttons a')[0]
		then:
			btnReply
		when:
			btnReply.click()
			waitFor {$('div#tabs-1').displayed}
			$("div#tabs-1 .next").click()
		then:
			$('input', value:'Alice').getAttribute('checked')
			$('input', value:'Bob').getAttribute('checked')
			!$('input', value:'June').getAttribute('checked')
			
		cleanup:
			deleteTestPolls()
			deleteTestMessages()
			deleteTestContacts()
	}
}

class PollListPage extends geb.Page {
 	static url = "message/poll/${Poll.findByTitle('Football Teams').id}/show/${Fmessage.findBySrc('Bob').id}"
	static at = {
		title.endsWith('Poll')
	}
	static content = {
		selectedMenuItem { $('#messages-menu .selected') }
		messagesList { $('#messages-submenu') }
	}
}


