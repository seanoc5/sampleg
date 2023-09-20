package com.oconeco.test

import net.dankito.readability4j.Article
import net.dankito.readability4j.Readability4J
import net.dankito.readability4j.model.ReadabilityOptions
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

Logger log = LogManager.getLogger(this.class.name);
log.info "starting script (${this.class.name})..."
List<String> urls = [
        'https://andres.jaimes.net/java/recommended-libraries/',
        'https://guides.grails.org/grails-vs-nodejs/guide/index.html',
        'https://www.tutorialspoint.com/groovy/groovy_switch_statement.htm',
//        'https://stvfd.org/',
]

urls.each {
    log.info "processing url:$it"
    URL url = new URL(it)
    String html = url.getText()

    ReadabilityOptions options = new ReadabilityOptions();
    Readability4J readability4J = new Readability4J(it, html); // url is just needed to resolve relative urls
    Article article = readability4J.parse();

    // returns extracted content in a <div> element
    String extractedContentHtml = article.getContent();
    // to get content wrapped in <html> tags and encoding set to UTF-8, see chapter 'Output encoding'
    String extractedContentHtmlWithUtf8Encoding = article.getContentWithUtf8Encoding();
    String extractedContentPlainText = article.getTextContent();
    String title = article.getTitle();
    String byline = article.getByline();            // not working...?
    String excerpt = article.getExcerpt();

    log.info "\t\tTitle: $title"
    log.info "\t\tBy Line: $byline"
    log.info "\t\texcerpt: $excerpt"
    log.info "-------------------------"
}
log.info "done?"
