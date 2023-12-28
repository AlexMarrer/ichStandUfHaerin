package Pruefung2.Logic;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrInputDocument;

import Pruefung2.Data;


public class DataLogic implements IDataLogic {

	private final static String solrURL = "http://localhost:8983/solr/SolrTestCore";

	private final static SolrClient solrClient = new HttpSolrClient.Builder(solrURL).build();
	
	@Override
	public void createData(List<String> words) throws InterruptedException {
	    Random randomWord = new Random();
	    int oldInt = 10000; // Miau
	    int totalDocs = 1000000;
	    ExecutorService threads = Executors.newFixedThreadPool(8);

	    for (int i = 0; i < totalDocs; i += oldInt) {
	        final int start = i;
	        final int end = Math.min(i + oldInt, totalDocs);

	        threads.submit(() -> {
	            ArrayList<SolrInputDocument> threadDocuments = new ArrayList<>();
	            for (int index = start; index < end; index++) {
	                StringBuilder text = new StringBuilder();
	                StringBuilder title = new StringBuilder();
	                int randomTextLength = randomWord.nextInt(501) + 1000;
	                for (int j = 0; j < randomTextLength; j++) {
	                    String word = words.get(randomWord.nextInt(words.size()));
	                    text.append(word).append(" ");
	                    if (j < 5) {
	                        title.append(word).append(" ");
	                    }
	                }

	                Data data = new Data(index, title.toString(), text.toString());
	                SolrInputDocument document = new SolrInputDocument();
	                document.addField("id", data.getId());
	                document.addField("title", data.getTitle());
	                document.addField("text", data.getText());

	                threadDocuments.add(document);
	            }

	            try {
	                solrClient.add(threadDocuments);
	                solrClient.commit();
	            } catch (SolrServerException | IOException e) {
	                e.printStackTrace();
	            }
	        });
	    }

	    threads.shutdown();
	    threads.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
	}

	@Override
	public void getData() throws SolrServerException, IOException {

		SolrQuery query = new SolrQuery();
		query.setQuery("*:*");
		query.setRows(5);

		QueryResponse response = solrClient.query(query);
		for (SolrDocument doc : response.getResults()) {
			System.out.println(doc);
		}
	}
	
	@Override
	public void closeSolr() throws IOException {
		solrClient.close();		
	}
}