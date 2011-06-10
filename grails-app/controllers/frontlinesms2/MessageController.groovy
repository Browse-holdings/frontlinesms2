package frontlinesms2

class MessageController {
	static allowedMethods = [save: "POST", update: "POST", delete: "POST"]

	def index = {
		redirect(action: "inbox", params: params)
	}

	def show = {
		def messageInstance = Fmessage.get(params.id)
		def ownerInstance
		if(params.messageSection == 'poll') {
			ownerInstance = Poll.get(params.ownerId)
		} else if(params.messageSection == 'poll'){
			ownerInstance = Folder.get(params.ownerId)
		} else {
			params.messageSection = 'inbox'
		}
		println params
		messageInstance.updateDisplaySrc()
		if(!messageInstance.read) {
			messageInstance.read = true
			messageInstance.save()
		}
		render view:params.messageSection,
				model:[messageInstance: messageInstance,
						folderInstanceList: Folder.findAll(),
						pollInstanceList: Poll.findAll(),
					ownerInstance: ownerInstance] << list()
	}

    def inbox = {
		def messageInstanceList = Fmessage.getInboxMessages()
		def latestMessage
		messageInstanceList.each {
			if(!latestMessage || it.dateCreated > latestMessage.dateCreated) {
				latestMessage = it
			}
		}
		params.id = latestMessage?.id
		params.messageSection = 'inbox'
		if(params.id) {
			redirect(action:'show', params:params)
		} else {
			[messageSection: 'inbox',
				folderInstanceList: Folder.findAll(),
				pollInstanceList: Poll.findAll()]
		}
    }

    def sent = {
		params.inbound = false
		[messageSection:'sent'] << list()
    }

	def poll = {
		println "at poll"
		def ownerInstance = Poll.get(params.ownerId)
		def messageInstanceList = ownerInstance.messages
		def latestMessage
		messageInstanceList.each {
			if(!latestMessage) {
				latestMessage = it
			} else{
				if(it.dateCreated.compareTo(latestMessage.dateCreated) < 0) {
					latestMessage = it
				}
			}
		}
		params.id = latestMessage?.id
		if(params.id) {
			redirect(action:'show', params:params)
		} else {
			[ownerInstance: ownerInstance,
				folderInstanceList: Folder.findAll(),
				pollInstanceList: Poll.findAll()]
		}
	}
	
	def folder = {
		def ownerInstance = Folder.get(params.ownerId)
		def messageInstanceList = ownerInstance.messages
		def latestMessage
		messageInstanceList.each {
			if(!latestMessage || it.dateCreated > latestMessage.dateCreated) {
				latestMessage = it
			}
		}

		if(params.flashMessage) {
			flash.message = params.flashMessage
		}

		params.id = latestMessage?.id
		if(params.id) {
			redirect(action:'show', params:params)
		} else {
			[ownerInstance: ownerInstance,
				folderInstanceList: Folder.findAll(),
				pollInstanceList: Poll.findAll()]
		}
	}
	
	def list = {
		def messageInstanceList
		if(params.messageSection == 'inbox') {
			messageInstanceList = Fmessage.getInboxMessages().each { it.updateDisplaySrc()}
			[messageInstanceList: messageInstanceList,
				messageSection: 'inbox',
				messageInstanceTotal: Fmessage.getInboxMessages().size()]
		} else if(params.messageSection == 'poll') {
			def ownerInstance = Poll.get(params.ownerId)
		 	messageInstanceList = ownerInstance.messages
			messageInstanceList.each{ it.updateDisplaySrc() }
			[messageInstanceList: messageInstanceList,
					messageSection: 'poll',
					messageInstanceTotal: ownerInstance.messages.size(),
					ownerInstance: ownerInstance,
					pollInstanceList: Poll.findAll(),
					responseList: ownerInstance.responseStats]
		}else if(params.messageSection == 'folder') {
			def ownerInstance = Folder.get(params.ownerId)
		 	messageInstanceList = ownerInstance.messages
			messageInstanceList.each{ it.updateDisplaySrc() }
			[messageInstanceList: messageInstanceList,
					messageSection: 'folder',
					messageInstanceTotal: ownerInstance.messages.size(),
					ownerInstance: ownerInstance,
					folderInstanceList: Folder.findAll(),
					pollInstanceList: Poll.findAll()]
		}else {
			[folderInstanceList: Folder.findAll(),
				pollInstanceList: Poll.findAll()]
		}
		
	}

	def move = {
		def messageOwner
		if(params.messageSection == 'poll') {
			messageOwner = Poll.get(params.ownerId)
		} else {
			messageOwner = Folder.get(params.ownerId)
		}
		def messageInstance = Fmessage.get(params.id)
		if(messageOwner instanceof Poll){
			def unknownResponse = messageOwner.getResponses().find { it.value == 'Unknown'}
			unknownResponse.addToMessages(Fmessage.get(params.id)).save(failOnError: true, flush: true)
		} else {
			messageOwner.addToMessages(Fmessage.get(params.id)).save(failOnError: true, flush: true)
		}
		
		flash.message = "${message(code: 'default.updated.message', args: [message(code: 'message.label', default: 'Fmessage'), Fmessage.get(params.id).id])}"
		redirect(action: "show", params: params)
	}

	def changeResponse = {
		def responseInstance = PollResponse.get(params.responseId)
		def messageInstance = Fmessage.get(params.id)
		responseInstance.addToMessages(messageInstance).save(failOnError: true, flush: true)
		flash.message = "${message(code: 'default.updated.message', args: [message(code: 'message.label', default: 'Fmessage'), messageInstance.id])}"
		redirect(action: "show", params: params)
	}
	
	def deleteMessage = {
		def messageInstance = Fmessage.get(params.id)
		messageInstance.toDelete()
		messageInstance.save(failOnError: true, flush: true)
		Fmessage.get(params.id).messageOwner?.refresh()
		flash.message = "${message(code: 'default.deleted.message', args: [message(code: 'message.label', default: 'Fmessage'), messageInstance.id])}"
		redirect(action: params.messageSection, params:params)
	}
}
