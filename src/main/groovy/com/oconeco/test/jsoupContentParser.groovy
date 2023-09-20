package com.oconeco.test

import groovy.transform.Field
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.jsoup.Connection
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.select.Elements

@Field
Logger log = LogManager.getLogger(this.class.name);
log.info "starting script (${this.class.name})..."
List<String> urls = [
        'https://andres.jaimes.net/java/recommended-libraries/',
        'https://guides.grails.org/grails-vs-nodejs/guide/index.html',
        'https://www.tutorialspoint.com/groovy/groovy_switch_statement.htm',
//        'https://stvfd.org/',
]

Document.OutputSettings settings = new Document.OutputSettings()
settings.prettyPrint(false)

 urls.each { String url ->
    Connection.Response resp = Jsoup.connect(url)
            .timeout(3000).followRedirects(true)
            .execute()

    Document htmlDoc = resp.parse()
    def body = htmlDoc.body()

    String title = htmlDoc.title()
    Elements links = htmlDoc.select("a[href]")
    Elements headings = htmlDoc.select("h1, h2, h3, h4, h5, h6, h7")

    Elements bodyChildren = body.children()
    def foo = findContentElement(bodyChildren)

}

def seemsLikeNavLink(Element link){
    def siblings = link.siblingNodes()
    int sibLinks = 0
    int sibContent = 0
    siblings.each{
        if(it instanceof Element){
            Element e = (Element)it
//            if(e.tagName().toLowerCase()=='a'
        }
    }
}


def findContentElement(List<Element> childElements, int level = 1) {
    int i = 0
    def contentElements = []
    childElements.each { Element child ->
        i++
        String childTagName = child.tagName()
        log.info "bodychild $i) ${childTagName}"
        if (isContentElement(child)) {
            contentElements << childElements
            String childText = getText(child)
            log.info "Body child? --> $childTagName"
        } else {
            def subchildren = child.children()
            def foundCEs = findContentElement(subchildren, level++)
            if (foundCEs) {
                contentElements << foundCEs
            } else {
                log.info "no child elements found: $childTagName"
            }
        }
    }
    return contentElements
}

/**
 * Check if this node looks like content as opposed to header/footer/nav
 * todo -- more logic/code, this is a placeholder
 * @param el
 */
def isContentElement(Element el) {
    boolean istn = false
    boolean viableTag = isViableTextTag(el)
    def tnList = el.textNodes()
    if (tnList) {
        def tnContent = tnList.findAll {
            it.text() > '' && it.parent().tag
        }.join('\n').trim()
        if (tnContent.size() > MIN_CONTENT_SIZE) {
            istn = true
        }
    } else {
        log.debug "no text node list: ${el.tagName()}"
    }
    return istn
}


List<Element> parseSections(Element body) {
    def allBodyTags = body.select('*')
    List<Element> currentTagsList = []
    List<Element> sections = []
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
        } else {
            currentTagsList << el
        }

    }
    log.info "sections size: ${sections.size()}"
    return sections
}

println "Body size: ${body.size()}"


log.info "done?"


String getText(Element el) {
    String text = el.text()
}

@Field
int MIN_CONTENT_SIZE = 10

boolean isViableTextTag(Element el) {
    boolean viable = false
    String tagName = el.tagName().toLowerCase()
    switch (tagName) {
        case 'a':
            viable = false
            break
        default:
            viable = true
    }
    return viable
}


def findHeaderTag(Element el){
    return null
}

def findFooterTag(Element el){
    return null
}

def findLeftNavTag(Element el){
    return null
}

def findRightNavTag(Element el){
    return null
}

