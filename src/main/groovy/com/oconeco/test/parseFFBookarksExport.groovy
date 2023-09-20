package com.oconeco.test

import groovy.json.JsonSlurper
import groovy.transform.Field
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

@Field
Logger log = LogManager.getLogger(this.class.name);
log.info "starting script (${this.class.name})..."

File ffBookmarkHtml = new File('/home/sean/Desktop/seanoc5.ff-bookmarks.html')
File ffBookmarkJson = new File('/home/sean/Desktop/seanoc5-ff.bookmarks-2023-09-17.json')

//JsonSlurper slurper =
JsonSlurper slurper = new JsonSlurper()
def json = slurper.parse(ffBookmarkJson)

//long lm = (json.lastModified) / 1000
//Date lastModified = new Date(lm)
List allPlaces = []
List<Map> children = json.children
children.each {
    String title = it.title
    def c2 = getChildItems(it)
    def typeGroup = c2.groupBy {it.type}
    log.info "$title) descendants:${c2.size()}"
    typeGroup.each {String type, List items ->
        log.info "\t\t$type) ${items.size()}"
    }
    List places = typeGroup.get('text/x-moz-place')
    allPlaces.addAll(places)
}
log.info "Allplaces: ${allPlaces.size()}"

Map tagLinks = [:].withDefault {[]}
allPlaces.each {Map place ->
    if(place.tags){
        List<String> tags = place.tags.split(',')
        tags.each{String t ->
            tagLinks[t] << place
        }
    }
}
tagLinks.keySet().each { String key ->
    def vals = tagLinks.get(key)
    log.info "Tag links for key($key): ${vals.size()}"
}

log.info "Done...?"


List<Map> getChildItems(Map child) {
    String title = child.title
    List l = [child]
    if(child.children) {
        l.addAll(child.children)
        child.children?.each {
            def grandChildren = getChildItems(it)
            l.addAll(grandChildren)
        }
    } else {
        log.debug "no children, hit leaf node?"
    }
    return l
}
