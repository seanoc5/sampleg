package com.oconeco.hacking

import net.dankito.readability4j.Article
import net.dankito.readability4j.Readability4J
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

Logger log = LogManager.getLogger(this.class.name);
log.info "starting script (${this.class.name})..."
List<String> urls = ['https://stvfd.org/', 'https://guides.grails.org/grails-vs-nodejs/guide/index.html']


urls.each { String url ->
    Readability4J readability4J = new Readability4J(resultDoc.uri, html); // url is just needed to resolve relative urls
    Article article = readability4J.parse();

    // returns extracted content in a <div> element
    String extractedContentHtml = article.getContent();
    // to get content wrapped in <html> tags and encoding set to UTF-8, see chapter 'Output encoding'
    String extractedContentHtmlWithUtf8Encoding = article.getContentWithUtf8Encoding();
    String extractedContentPlainText = article.getTextContent();
    String title = article.getTitle();
    String byline = article.getByline();            // not working...?
    String excerpt = article.getExcerpt();

}

println "Body size: ${body.size()}"


log.info "done?"
