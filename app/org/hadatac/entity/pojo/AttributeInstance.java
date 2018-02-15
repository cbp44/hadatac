package org.hadatac.entity.pojo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.text.WordUtils;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrRequest;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocumentList;
import org.hadatac.console.models.FacetHandler;
import org.hadatac.console.models.Pivot;
import org.hadatac.console.models.Facet;
import org.hadatac.utils.Collections;

import com.typesafe.config.ConfigFactory;

public class AttributeInstance extends HADatAcThing implements Comparable<AttributeInstance> {

	static String className = "sio:Attribute";

	public AttributeInstance () {}
	
	@Override
	public boolean equals(Object o) {
		if((o instanceof AttributeInstance) && (((AttributeInstance)o).getUri().equals(this.getUri()))) {
			return true;
		} else {
			return false;
		}
	}

	@Override
	public int hashCode() {
		return getUri().hashCode();
	}

	public long getNumberFromSolr(Facet facet, FacetHandler facetHandler) {
		System.out.println("\nAttributeInstance facet: " + facet.toSolrQuery());
		
		SolrQuery query = new SolrQuery();
		String strQuery = facetHandler.getTempSolrQuery(facet);
		System.out.println("\nstrQuery: " + strQuery);
		query.setQuery(strQuery);
		query.setRows(0);
		query.setFacet(false);

		try {
			SolrClient solr = new HttpSolrClient.Builder(
					ConfigFactory.load().getString("hadatac.solr.data") 
					+ Collections.DATA_ACQUISITION).build();
			QueryResponse queryResponse = solr.query(query, SolrRequest.METHOD.POST);
			solr.close();
			SolrDocumentList results = queryResponse.getResults();
			return results.getNumFound();
		} catch (Exception e) {
			System.out.println("[ERROR] AttributeInstance.getNumberFromSolr() - Exception message: " + e.getMessage());
		}

		return -1;
	}
	
	public Map<HADatAcThing, List<HADatAcThing>> getTargetFacets(
			Facet facet, FacetHandler facetHandler) {
		System.out.println("\nAttributeInstance getTargetFacets facet: " + facet.toSolrQuery());
		
		SolrQuery query = new SolrQuery();
		String strQuery = facetHandler.getTempSolrQuery(facet);
		System.out.println("strQuery: " + strQuery);
		query.setQuery(strQuery);
		query.setRows(0);
		query.setFacet(true);
		query.setFacetLimit(-1);
		query.setParam("json.facet", "{ "
				+ "characteristic_uri_str:{ "
				+ "type: terms, "
				+ "field: characteristic_uri_str, "
				+ "limit: 1000}}");

		try {
			SolrClient solr = new HttpSolrClient.Builder(
					ConfigFactory.load().getString("hadatac.solr.data") 
					+ Collections.DATA_ACQUISITION).build();
			QueryResponse queryResponse = solr.query(query, SolrRequest.METHOD.POST);
			solr.close();
			Pivot pivot = Measurement.parseFacetResults(queryResponse);
			return parsePivot(pivot, facet);
		} catch (Exception e) {
			System.out.println("[ERROR] AttributeInstance.getTargetFacets() - Exception message: " + e.getMessage());
		}

		return null;
	}
	
	private Map<HADatAcThing, List<HADatAcThing>> parsePivot(Pivot pivot, Facet facet) {
		Map<HADatAcThing, List<HADatAcThing>> results = new HashMap<HADatAcThing, List<HADatAcThing>>();
		for (Pivot pivot_ent : pivot.children) {
			AttributeInstance attrib = new AttributeInstance();
			attrib.setUri(pivot_ent.value);
			attrib.setLabel(WordUtils.capitalize(Attribute.find(pivot_ent.value).getLabel()));
			attrib.setCount(pivot_ent.count);
			attrib.setField("characteristic_uri_str");
			
			if (!results.containsKey(attrib)) {
				List<HADatAcThing> children = new ArrayList<HADatAcThing>();
				results.put(attrib, children);
			}
			
			Facet subFacet = facet.getChildById(attrib.getUri());
			subFacet.putFacet("characteristic_uri_str", attrib.getUri());
		}
		
		return results;
	}

	@Override
	public int compareTo(AttributeInstance another) {
		if (this.getLabel() != null && another.getLabel() != null) {
			return this.getLabel().compareTo(another.getLabel());
		}
		return this.getUri().compareTo(another.getUri());
	}
}
