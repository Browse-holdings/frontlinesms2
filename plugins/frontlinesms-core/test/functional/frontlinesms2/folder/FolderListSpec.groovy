package frontlinesms2.folder

import frontlinesms2.*
import java.text.SimpleDateFormat
import frontlinesms2.message.*
import frontlinesms2.popup.*
import spock.lang.*

class FolderListSpec extends FolderBaseSpec {
	def trashService = new TrashService()
	private def DATE_FORMAT = new SimpleDateFormat("dd MMMM, yyyy hh:mm a", Locale.US)

	def 'folder message list is displayed'() {
		given:
			createTestFolders()
			createTestMessages()
		when:
			to PageMessageFolder, Folder.findByName('Work')
		then:
			messageList.messageCount() == 2
			messageList.messageSource(0) == 'Jane'
			messageList.messageSource(1) == 'Max'
	}

	def 'no message is selected when a folder is first loaded'() {
		given:
			createTestFolders()
			createTestMessages()
		when:
			to PageMessageFolder, Folder.findByName('Work')
		then:
			singleMessageDetails.noneSelected
	}

	def "message's folder details are shown in list"() {
		given:
			createTestFolders()
			createTestMessages()
			def folderId = remote { Folder.findByName("Work").id }
		when:
			to PageMessageFolder, folderId
		then:
			messageList.messageSource(1) == 'Max'
			messageList.messageText(1) == 'I will be late'
			messageList.messageDate(1)
	}

	def 'selected folder is highlighted'() {
		given:
			createTestFolders()
		when:
			to PageMessageFolder, remote { Folder.findByName('Work').id }
		then:
			bodyMenu.selected == 'work'
	}

	def "should be able to reply for messages listed in the folder section"() {
		setup:
			createTestFolders()
			createTestMessages()
			def data = remote {
				def f = Folder.findByName("Work")
				[folder:f.id, message:f.messages[0].id] }
		when:
			to PageMessageFolder, data.folder, data.message
			singleMessageDetails.reply.jquery.trigger("click")
		then:
			waitFor { at QuickMessageDialog }
	}

	def "should filter folder messages for starred and unstarred messages"() {
		given:
			createTestFolders()
			createTestMessages()
		when:
			to PageMessageFolder, remote { Folder.findByName('Work').id },
					remote { Fmessage.findBySrc('Max').id }
		then:
			messageList.messageCount() == 2
		when:
			footer.showStarred.jquery.trigger("click")
		then:
			waitFor { messageList.messageCount() == 2 }
			messageList.messageSource(1) == 'Max'
		when:
			footer.showAll.jquery.trigger("click")
		then:
			waitFor { messageList.messageCount() == 2 }
			messageList.messageSource(0) == 'Jane'
			messageList.messageSource(1) == 'Max'
	}

	def "should autopopulate the message body when 'forward' is clicked"() {
		setup:
			createTestFolders()
			createTestMessages()
		when:
			to PageMessageFolder, remote { Folder.findByName('Work').id }, remote { Fmessage.findBySrc('Max').id }
			singleMessageDetails.forward.jquery.trigger("click")
		then:
			waitFor { at QuickMessageDialog }
			at QuickMessageDialog
			waitFor { compose.textArea.text() == "I will be late" }
	}

	def "message count displayed when multiple messages are selected"() {
		given:
			createTestFolders()
			createTestMessages()
		when:
			to PageMessageFolder, remote { Folder.findByName('Work').id }
			messageList.toggleSelect(0)
			waitFor("slow") { singleMessageDetails.displayed }
			messageList.toggleSelect(1)
		then:
			waitFor { multipleMessageDetails.displayed }
			waitFor { multipleMessageDetails.checkedMessageCount == "2 messages selected" }
	}
	
	def "'Reply All' button appears for multiple selected messages and works"() {
		given:
			createTestFolders()
			createTestMessages()
			remote {
				new Contact(name: 'Alice', mobile: 'Alice').save(failOnError:true, flush:true)
				new Contact(name: 'June', mobile: '+254778899').save(failOnError:true, flush:true)
				null }
		when:
			to PageMessageFolder, remote { Folder.findByName('Work').id }
			messageList.toggleSelect(0)
			messageList.toggleSelect(1)
			multipleMessageDetails.replyAll.jquery.trigger("click")
		then:
			waitFor { at QuickMessageDialog }
	}

	def "can rename a folder"() {
		given:
			createTestFolders()
			createTestMessages()
		when:
			to PageMessageFolder, remote { Folder.findByName('Work').id }
			folderMoreActions.value("rename")
			waitFor { at RenameFolderDialog }
			folderName.equals("Work")
			folderName.value("New Work")
			ok.jquery.trigger("click")
			to PageMessageFolder
		then:
			folderLinks*.text().every { it.startsWith('New Work') || it.startsWith('Projects') || it == "Create new folder" }
	}

	def 'Errors are displayed when creating a folder with an already existing folders name'() {
			given:
				createTestFolders()
				createTestMessages()
			when:
				to PageMessageFolder
				bodyMenu.newFolder.jquery.trigger("click")
			then:
				waitFor { at CreateFolderPopup }
			when:
				folderName.value("Work")
				ok.jquery.trigger("click")
			then:
				at CreateFolderPopup
				waitFor { errorPanel.text()?.toLowerCase() == "used folder name" }
	}

	def "display error when renaming a folder with a blank name"() {
		given:
			createTestFolders()
			createTestMessages()
		when:
			to PageMessageFolder, remote { Folder.findByName('Work').id } 
			folderMoreActions.value("rename")
			waitFor { at RenameFolderDialog }
			folderName.equals("Work")
			folderName.value("")
			ok.jquery.trigger("click")
		then:
			waitFor { errorPanel.text()?.toLowerCase() == "folder name cannot be blank" }
	}

	def "can delete a folder"() {
		setup:
			createTestFolders()
		when:
			to PageMessageFolder, remote { Folder.findByName("Work").id }
			folderMoreActions.value("delete")
			waitFor { at DeleteFolderPopup }
			popupTitle.equals("delete folder")
		then:
			ok.jquery.trigger("click")
			at PageMessageFolder
			waitFor { bodyMenu.selected == "inbox" }
			!bodyMenu.folderLinks*.text().containsAll('Work')
	}
	
	def "deleted folders show up in the trash section"() {
		setup:
			def folderId = deleteFolder()
		when:
			at PageMessageFolder
			to PageMessageTrash, remote { Trash.findByObjectId(folderId).id }
		then:
			messageList.messageSource(0) == 'Work'
			messageList.messageText(0) == '2 message(s)'
			DATE_FORMAT.format(messageList.messageDate(0)) == DATE_FORMAT.format(remote { Trash.findByObjectId(folderId).dateCreated })
			senderDetails == 'Work folder'
			DATE_FORMAT.format(date) == DATE_FORMAT.format(remote { Trash.findByObjectId(folderId).dateCreated })
	}

	def "clicking on empty trash permanently deletes a folder"() {
		setup:
			def folderId = deleteFolder()
		when:
			at PageMessageFolder
			to PageMessageTrash, remote { Trash.findByObjectId(folderId).id }
			trashMoreActions.value("empty-trash")
			waitFor { at EmptyTrashPopup }
		then:
			popupTitle.equals("empty trash?")
		when:
			ok.jquery.trigger("click")
			waitFor { title.toLowerCase().contains("inbox") }
		then:
			at PageMessageInbox
			bodyMenu.folderLinks*.text().any { it.startsWith "Projects" }
	}

	def "filter folder messages by incoming messages should not show new outgoing messages"() {
		given:
			createTestFolders()
			createTestMessages()
			createOutgoingMessage()
		when:
			to PageMessageFolder,
					remote { Folder.findByName('Projects').id },
					remote { Fmessage.findBySrc('Patrick').id }
		then:
			messageList.messageCount() == 3
		when:
			footer.showIncoming.click()
		then:
			waitFor { messageList.messageCount() == 2 }
		then:
			!messageList.newMessageNotification.displayed
	}
	
	def deleteFolder() {
		createTestFolders()
		createTestMessages()
		remote {
			def folder = Folder.findByName("Work")
			trashService.sendToTrash(folder)
			folder.id
		}
	}
}

