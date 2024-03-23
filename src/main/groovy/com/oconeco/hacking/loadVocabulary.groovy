package com.oconeco.hacking

import groovy.sql.GroovyRowResult
import groovy.sql.Sql
import groovy.transform.Field
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

import java.sql.Timestamp

@Field
Logger log = LogManager.getLogger(this.class.name);

log.info "Starting ${this.class.name}..."

def dbUrl = "jdbc:postgresql://localhost:5432/cm_dev"
def dbUser = System.getenv('dbuser')
def dbPassword = System.getenv('dbpass')
if (dbUrl && dbPassword) {

    int BATCH_SIZE = 200

    // Initialize a database connection
    Sql sql = Sql.newInstance(dbUrl, dbUser, dbPassword, "org.postgresql.Driver")

    GroovyRowResult row = sql.firstRow("select max(id) as max from term")
    Long maxId = row.max ?: 1
    // Define SQL query to insert data
    //def insertQuery = "INSERT INTO aaa (text, lemmas) VALUES (?, ?)"
    def insertQuery = "INSERT INTO term (id, version, text, lemmas, language, date_created, last_updated) VALUES (?, ?, ?, ?, ?, ?, ?)"
    Date myDate = new Date()
    Timestamp ts = myDate.toTimestamp()

    File dictFile = new File('/home/sean/work/vocabulary/12dicts-6.0.2/Lemmatized/2+2+3lem.txt')
//    File dictFile = new File('D:/work/12dicts-6/Lemmatized/2+2+3lem.txt')
    log.info "Reading file: $dictFile..."
    int lineNo = 0
    int batchNo = 0

    if (dictFile.canRead()) {
        String termLine = ''
        dictFile.eachLine {
            lineNo++
            batchNo++
            if (it.startsWith(' ')) {
                String lemmas = it
                maxId++
                log.info "$lineNo) Term: $termLine -- lemmas:$lemmas"
//            List data = [termLine.trim(), lemmas.trim()]
                List data = [maxId, 1, termLine.trim(), lemmas.trim(), 'english', ts, ts]
                def rc = insertTermData(sql, insertQuery, data)
                termLine = ''       // set to blank to skip writing after lemma (wait for next line) -- assumes only one-possible lemma line after term line

            } else if (termLine) {
                log.debug "\t\tcurrent term line: $it"
                log.info "$lineNo) term:$termLine  (no lemmas)"
                maxId++
                List data = [maxId, 1, termLine.trim(), null, 'english', ts, ts]
//            List data = [termLine.trim(), null]
                def rc = insertTermData(sql, insertQuery, data)
                termLine = it
            } else {
                termLine = it
                log.debug "\t\tsetting termline from blank (either first, or just after a lemma line)"
            }
            if (lineNo % BATCH_SIZE == 0) {
                log.info "$lineNo) commit batch ($batchNo)"
            }
        }
    }
} else {
    log.error "Missing env variable(s): dbuser:($dbUser) or dbpass (redacted) -- cancelling"
}

def insertTermData(Sql sql, def insertQuery, List data) {
    List<List<Object>> rc = sql.executeInsert(insertQuery, data)
    log.info "result: $rc"
    return rc

}
