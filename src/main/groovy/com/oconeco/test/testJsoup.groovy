package com.oconeco.test

import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.jsoup.Connection
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.safety.Safelist

Logger log = LogManager.getLogger(this.class.name);
log.info "starting script (${this.class.name})..."
List<String> urls = ['https://stvfd.org/', 'https://guides.grails.org/grails-vs-nodejs/guide/index.html']

Document.OutputSettings settings = new Document.OutputSettings()
settings.prettyPrint(false)

urls.each { String url ->
    Connection.Response resp = Jsoup.connect(url)
            .timeout(3000).followRedirects(true)
            .execute()
//    Document htmlJsoup = resp.parse()

// save original(ish?) text of HTML doc
    String htmlStr = resp.body()

    Document htmlDoc = resp.parse()

    def head = htmlDoc.head()

    def body = htmlDoc.body()
    String bodyText = body.text()
    log.debug "body text:\n$bodyText"

    String cleanBodyText = Jsoup.clean(htmlStr, "", Safelist.none(), settings)
    log.info "cleaned body text:\n$cleanBodyText"


    def title = htmlDoc.title()
    def links = htmlDoc.select("a[href]")
    def headings = htmlDoc.select("h1, h2, h3, h4, h5, h6, h7")        //doc.select("h1, h2, h3, h4, h5, h6, h7").eachText()


    def allBodyTags = body.select('*')
    List currentTagsList = []
    List sections = []
    allBodyTags.each { Element el ->
        String tagName = el.tagName().toLowerCase()
        if (tagName ==~ /h\d/) {
            log.info "found heading, making this a section break...: $el"
            if (currentTagsList) {
                sections << currentTagsList
                currentTagsList = []
            } else {
                log.info "empty tagList (first section?) sections count:${sections.size()} for el:$el (heading?)"
            }
        }

/*
    List<TextNode> textNodes = el.textNodes()
    if (textNodes) {
        currentTagsList << el
    } else {
        log.info "No textNodes for el:$el"
    }
*/

    }
}

println "Body size: ${body.size()}"


log.info "done?"
