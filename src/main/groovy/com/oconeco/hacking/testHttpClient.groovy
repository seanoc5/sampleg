package com.oconeco.hacking

import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.core.Logger

Logger log = LogManager.getLogger(this.class.name);
log.info "Starting script: ${this.class.name}..."

String searchUrl = 'https://sayhili.com/rules/'

// Specify the URL you want to send the request to
 URL url = new URL(searchUrl);

 // Open a connection to the URL
 HttpURLConnection connection = (HttpURLConnection) url.openConnection();

 // Set the request method (GET, POST, etc.)
 connection.setRequestMethod("GET");

 // Set the user-agent header
// connection.setRequestProperty("User-Agent", "CourageousSearch");

 // Get the response code
 int responseCode = connection.getResponseCode();
 System.out.println("Response Code: " + responseCode);

 // Read the response from the server
 BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
 String line;
 StringBuilder response = new StringBuilder();

 while ((line = reader.readLine()) != null) {
     response.append(line);
 }

 reader.close();

 // Print the response
 System.out.println("Response: " + response.toString());

 // Close the connection
 connection.disconnect();

log.info "Done...?"
