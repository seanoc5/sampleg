package com.oconeco.hacking

import groovy.sql.GroovyRowResult
import groovy.sql.Sql
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

import java.sql.Timestamp


Logger log = LogManager.getLogger(this.class.name);

log.info "Starting script ${this.class.name}"

// Database connection information
def dbUrl = "jdbc:postgresql://localhost:5432/cm_dev"
def dbUser = "sean"
def dbPassword = System.getenv('dbpass')


// Initialize a database connection
Sql sql = Sql.newInstance(dbUrl, dbUser, dbPassword, "org.postgresql.Driver")

GroovyRowResult row = sql.firstRow("select max(id) as max from term")
def maxId = row.max
// Define SQL query to insert data
def insertQuery = """
        INSERT INTO term (id, version, text, language, date_created, last_updated)
        VALUES (?, ?, ?, ?, ?, ?)
    """

// Data to be inserted
def data = ['test', 'foo', 'bar']

Date myDate = new Date()
Timestamp ts = myDate.toTimestamp()

// Insert data into the database
data.each { String t ->
    maxId++
    List<List<Object>> rc = sql.executeInsert(insertQuery, [maxId, 1, t, 'english', ts, ts])
    log.info "result: $rc"
}

log.info("Records inserted successfully!")


