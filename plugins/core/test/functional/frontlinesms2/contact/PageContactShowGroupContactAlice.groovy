package frontlinesms2.contact

import frontlinesms2.*

class PageContactShowGroupContactAlice extends PageContactShow {
	static getUrl() { "group/show/${Group.findByName('Excellent').id}/contact/show/${Contact.findByName('Alice').id}" }
}