package com.oconeco.hacking

import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

// Database connection information
def dbUrl = "jdbc:postgresql://localhost:5432/cm_dev"
def dbUser = "sean"
def dbPassword = System.getenv('dbpass')

Logger log = LogManager.getLogger(this.class.name);

log.info "Starting ${this.class.name}..."

File dictFile = new File('D:/work/12dicts-6/Lemmatized/2+2+3lem.txt')
log.info "Reading file: $dictFile..."
int lineNo = 0
if(dictFile.canRead()){
    String termLine = ''
    dictFile.eachLine {
        lineNo++
        if(it.startsWith(' ')){
            String lemmas = it
            log.info "$lineNo) Term: $termLine -- lemmas:$lemmas"
            termLine = ''       // set to blank to skip writing after lemma (wait for next line) -- assumes only one-possible lemma line after term line
        } else if(termLine){
            log.debug "\t\tcurrent term line: $it"
            log.info "$lineNo) term:$termLine  (no lemmas)"
            termLine = it
        } else {
            termLine = it
            log.debug "\t\tsetting termline from blank (either first, or just after a lemma line)"
        }
    }
}



// Initialize a database connection
def sql = Sql.newInstance(dbUrl, dbUser, dbPassword, "org.postgresql.Driver")

try {
    // Define SQL query to insert data
    def insertQuery = """
        INSERT INTO term (column1, column2, column3)
        VALUES (?, ?, ?)
    """

    // Data to be inserted
    def data = [
        [value1, value2, value3],
        [value4, value5, value6],
        // Add more rows as needed
    ]

    // Insert data into the database
    data.each { row ->
        sql.executeInsert(insertQuery, row)
    }

    println("Records inserted successfully!")
} catch (Exception e) {
    println("Error: ${e.message}")
} finally {
    // Close the database connection
    sql.close()
}


