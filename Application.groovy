package OAIPMHConsumer

@Grab(group='org.codehaus.groovy.modules.http-builder', module='http-builder', version='0.5.1' )

import groovyx.net.http.*
import groovyx.net.http.HTTPBuilder
import groovyx.net.http.ContentType

class Consumer {

     /**
     * Receive a number of records from an OAI-PHM server
     * @param url The main server's url 
     * @param path The to the api from the main url
     * @param query A Map containing the parameters for the api
     * @param records The numbers of records to be printed
     */
      static def getRecords(String url, String path, Map query, int records) {
       try
       {
	def http = new HTTPBuilder(url)
	http.get( path: path, query: query )
	{ resp, xml ->

	  // Find all the xml record tags containing a header that is not marked as deleted
	  // and print the title for each
	  xml."**".findAll{ it.name().equals("record") && !it.header.@status.text().contains("deleted")}.each
	  {
	        if(records > 0){
                println "counter:" + (records--)
		println "title: " + it.metadata.dc.title.text()
		}
	  }

	def token = xml."**".find{ it.name().equals("resumptionToken") }

	// Check if the number of records has not been met and if there is
	// a token continue the request from the server using that token
	if(!token.isEmpty() && records > 0 ){
		getRecords(url,path, [verb: 'ListRecords', metadataPrefix: 'oai_dc', resumptionToken: token], records)
	}

	}

	} catch ( HttpResponseException ex ) {
	  // default failure handler throws exception:
	  println "Unexpected reponse error: ${ex.statusCode}"
	  }
}

}

// Receiving 500 records
def cons = new Consumer()
cons.getRecords('http://eprints.ucm.es/','cgi/oai2', [verb: 'ListRecords', metadataPrefix: 'oai_dc'], 500)

