package com.oconeco.test

import groovy.transform.Field
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.jsoup.Connection
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.safety.Safelist

@Field
Logger log = LogManager.getLogger(this.class.name);
log.info "starting script (${this.class.name})..."
List<String> urls = [
        'https://andres.jaimes.net/java/recommended-libraries/',
        'https://guides.grails.org/grails-vs-nodejs/guide/index.html',
        'https://www.tutorialspoint.com/groovy/groovy_switch_statement.htm',
        'https://stvfd.org/',
]

Document.OutputSettings settings = new Document.OutputSettings()
settings.prettyPrint(false)

urls.each { String url ->
    log.info "Process link: $url"
    Connection.Response resp = Jsoup.connect(url)
            .timeout(3000).followRedirects(true)
            .execute()

    String htmlStr = resp.body()

    Document htmlDoc = resp.parse()
    def head = htmlDoc.head()
    def body = htmlDoc.body()

    String bodyText = body.text()
    log.debug "body text:\n$bodyText"

    String cleanBodyText = Jsoup.clean(htmlStr, "", Safelist.none(), settings)
    log.debug "cleaned body text:\n$cleanBodyText"

    def title = htmlDoc.title()
    def links = htmlDoc.select("a[href]")
    def headings = htmlDoc.select("h1, h2, h3, h4, h5, h6, h7")        //doc.select("h1, h2, h3, h4, h5, h6, h7").eachText()
    log.info "\t\tTitle:$title"
    log.info "\t\tLinks size:${links.size()}"
    log.info "\t\tHeadings size:${headings.size()}"
}

