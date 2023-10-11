package com.oconeco.hacking
// Database connection information
def dbUrl = "jdbc:postgresql://localhost:5432/cm_dev"
def dbUser = "sean"
def dbPassword = System.getenv('dbpass')


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


