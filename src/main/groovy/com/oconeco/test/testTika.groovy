package com.oconeco.test

import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.apache.tika.config.TikaConfig
import org.apache.tika.exception.TikaException
import org.apache.tika.io.TikaInputStream
import org.apache.tika.metadata.Metadata
import org.apache.tika.parser.AutoDetectParser
import org.apache.tika.parser.ParseContext
import org.apache.tika.sax.BodyContentHandler
import org.apache.tika.sax.TeeContentHandler
import org.apache.tika.sax.ToXMLContentHandler
import org.jsoup.Connection
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.safety.Safelist
import org.xml.sax.SAXException

Logger log = LogManager.getLogger(this.class.name);
log.info "starting script (${this.class.name})..."
List<String> urls = ['https://stvfd.org/', 'https://guides.grails.org/grails-vs-nodejs/guide/index.html']

TikaConfig tikaConfig = new TikaConfig()
int MAX_BODY_CHARS = 1024*1024*50

urls.each { String u ->
    URL url = new URL(u)
    try (TikaInputStream tikaInputStream = TikaInputStream.get(url)) {
        Metadata metadata = new Metadata()

        String mimetype = tikaConfig.getDetector().detect(tikaInputStream, metadata)

        ToXMLContentHandler structureHandler = new ToXMLContentHandler()
        BodyContentHandler textHandler = new BodyContentHandler(MAX_BODY_CHARS)
        TeeContentHandler teeContentHandler = new TeeContentHandler(structureHandler, textHandler)

        ParseContext context = new ParseContext()

        AutoDetectParser parser = new AutoDetectParser()
//        parser.parse(tikaInputStream, teeContentHandler, metadata, context)
        parser.parse(tikaInputStream, textHandler, metadata, context)

        String body = textHandler.toString()
        log.info "Body: \n$body"
        log.info "next..."

    } catch (IOException ioException) {
        log.warn "IO Exception: $ioException "
    } catch (SAXException e) {
        log.warn "SAX Exception: $e"
    } catch (TikaException e) {
        log.warn "Tika Exception: $e"
    }
    log.info "done with url:$u"
}

log.info "done?"
