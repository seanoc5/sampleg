package com.oconeco.hacking

import org.apache.http.HttpResponse
import org.apache.http.client.HttpClient
import org.apache.http.client.methods.HttpGet
import org.apache.http.impl.client.HttpClients
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.core.Logger
import org.apache.tika.config.TikaConfig
import org.apache.tika.io.TikaInputStream
import org.apache.tika.metadata.Metadata

Logger log = LogManager.getLogger(this.class.name);
log.info "Starting script: ${this.class.name}..."

// Create an HttpClient
HttpClient httpClient = HttpClients.createDefault();

// Specify the URL you want to send the request to
String url = "https://sayhili.com/rules/";

// Create an HttpGet request
HttpGet httpGet = new HttpGet(url);

// Set the user-agent header
httpGet.addHeader("User-Agent", "CourageousSearch");
TikaConfig tikaConfig = new TikaConfig()

try {
    // Execute the request and get the response
    HttpResponse response = httpClient.execute(httpGet);

    // Get the response code
    int statusCode = response.getStatusLine().getStatusCode();
    System.out.println("Response Code: " + statusCode);

//    InputStreamReader isr = new InputStreamReader(response.getEntity().getContent())
    InputStream inputStream = new BufferedInputStream(response.getEntity().getContent())

    // Read the response from the server
//    BufferedReader reader = new BufferedReader(isr);

    TikaInputStream tikaInputStream = TikaInputStream.get(inputStream)

    Metadata metadata = new Metadata()

    //TikaInputStream sets the TikaCoreProperties.RESOURCE_NAME_KEY when initialized with a file or path
    String mimetype = tikaConfig.getDetector().detect(tikaInputStream, metadata)
    if (mimetype) {
        log.debug "\t\tresult doc mimetype($mimetype) detected by tika in fetchContent()..."
    } else {
        log.warn "Tika detected NO VALID mimetype ($mimetype) found for resultdoc($resultDoc), setting to '$UNKNOWN' and continuing to throw it at tika to parse (bad idea??)... "
        mimetype = UNKNOWN
    }

//
//    String line;
//    StringBuilder responseContent = new StringBuilder();
//
//    while ((line = reader.readLine()) != null) {
//        responseContent.append(line);
//    }
//
//    reader.close();

    // Print the response
    System.out.println("Response: " + responseContent.toString());

} catch (IOException e) {
    e.printStackTrace();
}


log.info "Done...?"
