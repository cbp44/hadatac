package org.hadatac.console.controllers.metadataacquisition;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import play.mvc.Controller;
import play.mvc.Result;

import org.hadatac.console.views.html.metadataacquisition.*;
import org.hadatac.entity.pojo.Measurement;
import org.hadatac.metadata.loader.URIUtils;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFactory;
import org.apache.jena.query.ResultSetRewindable;
import org.apache.jena.sparql.engine.http.QueryExceptionHTTP;
import org.hadatac.console.controllers.AuthApplication;
import org.hadatac.console.controllers.triplestore.UserManagement;
import org.hadatac.console.http.SPARQLUtils;
import org.hadatac.utils.CollectionUtil;
import org.hadatac.utils.NameSpaces;

import be.objectify.deadbolt.java.actions.Group;
import be.objectify.deadbolt.java.actions.Restrict;

public class ViewSubject extends Controller {

	public static Map<String, List<String>> findSubjectIndicators(String study_uri, String subject_uri) {
		String indicatorQuery = "";
		if (study_uri.startsWith("http")) {
			study_uri = "<" + study_uri + ">";
		}
		if (subject_uri.startsWith("http")) {
			subject_uri = "<" + subject_uri + ">";
		}
		indicatorQuery += NameSpaces.getInstance().printSparqlNameSpaceList();
		indicatorQuery += "SELECT ?subjectIndicator ?label ?comment WHERE { "
				+ "?subjectIndicator rdfs:subClassOf hasco:StudyIndicator . "
				+ "?subjectIndicator rdfs:label ?label . "
				+ "?subjectIndicator rdfs:comment ?comment . }";
		
		ResultSetRewindable resultsrwIndc = SPARQLUtils.select(
                CollectionUtil.getCollectionPath(CollectionUtil.Collection.METADATA_SPARQL), indicatorQuery);

		Map<String, String> indicatorMap = new HashMap<String, String>();
		String indicatorLabel = "";
		while (resultsrwIndc.hasNext()) {
			QuerySolution soln = resultsrwIndc.next();
			indicatorLabel = soln.get("label").toString();
			indicatorMap.put(soln.get("subjectIndicator").toString(), indicatorLabel);		
		}
		Map<String, String> indicatorMapSorted = new TreeMap<String, String>(indicatorMap);
		Map<String, List<String>> indicatorValues = new HashMap<String, List<String>>();

		for(Map.Entry<String, String> entry : indicatorMapSorted.entrySet()){
			String parentIndicatorUri = entry.getKey();
			String indvIndicatorQuery = "";
			indvIndicatorQuery += NameSpaces.getInstance().printSparqlNameSpaceList();
			indvIndicatorQuery += "SELECT DISTINCT ?label ?uri WHERE { "
					+ "?schemaUri hasco:isSchemaOf " + study_uri + " . "
					+ "?schemaAttribute hasco:partOfSchema ?schemaUri . "
					+ "?schemaAttribute hasco:hasAttribute ?uri . " 
					+ "?uri rdfs:subClassOf* <" + parentIndicatorUri + "> . "
					+ "?uri rdfs:label ?label . "
					+ "}";
			
			try {		
				ResultSetRewindable resultsrwIndvInd = SPARQLUtils.select(
		                CollectionUtil.getCollectionPath(CollectionUtil.Collection.METADATA_SPARQL), indvIndicatorQuery);
				
				List<String> listIndicatorLabel = new ArrayList<String>();
				while (resultsrwIndvInd.hasNext()) {
					QuerySolution soln = resultsrwIndvInd.next();
					if(Measurement.findForViews(UserManagement.getCurrentUserUri(), study_uri, 
							URIUtils.convertToWholeURI(subject_uri), 
							soln.get("uri").toString(), true).getDocumentSize() > 0){
						listIndicatorLabel.add(soln.get("label").toString());
					}
				}
				indicatorValues.put(entry.getValue().toString(), listIndicatorLabel);
			} catch (QueryExceptionHTTP e) {
				e.printStackTrace();
			}
		}
		return indicatorValues;
	}

	public static String findBasicHTML(String subject_uri) {
		if (subject_uri == null || subject_uri.equals("")) {
			return null;
		}
		ResultSetRewindable resultsrw = findSubjectBasic(subject_uri); 
		if (resultsrw == null) {
			return null;
		}
		String html = "";

		if (resultsrw.hasNext()) {
			QuerySolution soln = resultsrw.next();
			String pid = "";
			String subjectLabel = "";
			String subjectTypeLabel = "";
			String cohortLabel = "";
			String studyUri = "";
			
			if (soln.get("pid") != null) {
			    pid = soln.get("pid").toString();
			}
			if (soln.get("subjectLabel") != null) {
			    subjectLabel = soln.get("subjectLabel").toString();
            }
			if (soln.get("subjectTypeLabel") != null) {
			    subjectTypeLabel = soln.get("subjectTypeLabel").toString();
            }
			if (soln.get("cohortLabel") != null) {
			    cohortLabel = soln.get("cohortLabel").toString();
            }
			if (soln.get("studyUri") != null) {
			    studyUri = soln.get("studyUri").toString();
            }
			
			html += "<table>";
			html += "<tr> <td><b>Original ID &nbsp; &nbsp;</b></td> <td>" + pid + "</td></tr>";
			html += "<tr> <td><b>Internal ID</b></td> <td>" + subjectLabel + "</td></tr>";
			html += "<tr> <td><b>Type</b></td> <td>" + subjectTypeLabel + "</td></tr>";
			html += "<tr> <td><b>Cohort</b></td> <td>" + cohortLabel + "</td></tr>";
			html += "<tr> <td> &nbsp;</td> <td> &nbsp;</td></tr>";
			try {
				html += "<tr> <td></td> <td><a href='/hadatac/metadataacquisitions/viewSubject?study_uri=" 
						+ URLEncoder.encode(studyUri, "utf-8") 
						+ "&subject_uri=" + URLEncoder.encode(subject_uri, "utf-8") 
						+ "'>(More info about object)</a></td></tr>";
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
			html += "</table>";
		}

		return html;
	}

	public static ResultSetRewindable findSubjectBasic(String subject_uri) {
		String subjectQueryString = "";
		if (subject_uri == null || subject_uri.equals("")) {
			return null;
		}
		if (subject_uri.indexOf("http") != -1) {
			subject_uri = "<" + subject_uri + ">";
		}
		
		subjectQueryString += NameSpaces.getInstance().printSparqlNameSpaceList();
		subjectQueryString += "SELECT ?pid ?subjectTypeLabel ?subjectLabel ?cohortLabel ?studyUri WHERE { "
				+ subject_uri + " hasco:originalID ?pid . "
				+ subject_uri + " <http://hadatac.org/ont/hasco/isMemberOf> ?cohort . "
				+ "?cohort <http://hadatac.org/ont/hasco/isMemberOf> ?studyUri . "
				+ "?cohort rdfs:label ?cohortLabel . "
				+ "OPTIONAL { " + subject_uri + " rdfs:label ?subjectLabel } . "
				+ "OPTIONAL { " + subject_uri + " a ?subjectType . "
				+ "			  ?subjectType rdfs:label ?subjectTypeLabel } . "
				+ "}";
		
		ResultSetRewindable resultsrw = SPARQLUtils.select(
                CollectionUtil.getCollectionPath(CollectionUtil.Collection.METADATA_SPARQL), subjectQueryString);
		
		return resultsrw;
	}		

	public static Map<String, List<String>> findBasic(String subject_uri) {
		//System.out.println("in findBasic (subject_uri): '" + subject_uri + "'" );
		if (subject_uri == null || subject_uri.equals("")) {
			return null;
		}
		ResultSetRewindable resultsrw = findSubjectBasic(subject_uri); 
		if (resultsrw == null) {
			return null;
		}
		Map<String, List<String>> subjectResult = new HashMap<String, List<String>>();
		List<String> values; // = new ArrayList<String>();

		while (resultsrw.hasNext()) {
			QuerySolution soln = resultsrw.next();
			//System.out.println("HERE IS THE RAW SOLN*********" + soln.toString());
			values = new ArrayList<String>();
			values.add("Pid: " + soln.get("pid").toString());
			values.add("Label: " + soln.get("subjectLabel").toString());
			values.add("Type: " + soln.get("subjectTypeLabel").toString());
			values.add("Cohort: " + soln.get("cohortLabel").toString());
//			values.add("Study: " + soln.get("studyLabel").toString());
			subjectResult.put(subject_uri, values);
			//System.out.println("THIS IS SUBROW*********" + subjectResult);	
		}

		return subjectResult;
	}

	public static Map<String, String> findSubjectIndicatorsUri(String study_uri) {
		String indicatorQuery = "";
		if (study_uri.startsWith("http")) {
			study_uri = "<" + study_uri + ">";
		}
		indicatorQuery += NameSpaces.getInstance().printSparqlNameSpaceList();
		indicatorQuery += "SELECT ?subjectIndicator ?label ?comment WHERE { "
				+ "?subjectIndicator rdfs:subClassOf hasco:StudyIndicator . "
				+ "?subjectIndicator rdfs:label ?label . "
				+ "?subjectIndicator rdfs:comment ?comment . }";
		Map<String, String> indicatorMap = new HashMap<String, String>();
		String indicatorLabel = "";
		try {			
			ResultSetRewindable resultsrwIndc = SPARQLUtils.select(
                    CollectionUtil.getCollectionPath(CollectionUtil.Collection.METADATA_SPARQL), indicatorQuery);
			
			while (resultsrwIndc.hasNext()) {
				QuerySolution soln = resultsrwIndc.next();
				indicatorLabel = soln.get("label").toString();
				indicatorMap.put(soln.get("subjectIndicator").toString(),indicatorLabel);		
			}
		} catch (QueryExceptionHTTP e) {
			e.printStackTrace();
		}
		Map<String, String> indicatorMapSorted = new TreeMap<String, String>(indicatorMap);
		Map<String, String> indicatorUris = new HashMap<String, String>();

		for(Map.Entry<String, String> entry : indicatorMapSorted.entrySet()){
			String parentIndicatorUri = entry.getKey();
			String indvIndicatorQuery = "";
			indvIndicatorQuery += NameSpaces.getInstance().printSparqlNameSpaceList();
			indvIndicatorQuery += "SELECT DISTINCT ?label ?uri WHERE { "
					+ "?schemaUri hasco:isSchemaOf " + study_uri + " . "
					+ "?schemaAttribute hasco:partOfSchema ?schemaUri . "
					+ "?schemaAttribute hasco:hasAttribute ?uri . " 
					+ "?uri rdfs:subClassOf* <" + parentIndicatorUri + "> . "
					+ "?uri rdfs:label ?label . "
					+ "}";

			try {				
				ResultSetRewindable resultsrwIndvInd = SPARQLUtils.select(
		                CollectionUtil.getCollectionPath(CollectionUtil.Collection.METADATA_SPARQL), indvIndicatorQuery);
				
				while (resultsrwIndvInd.hasNext()) {
					QuerySolution soln = resultsrwIndvInd.next();
					indicatorUris.put(soln.get("label").toString(), soln.get("uri").toString());
				}
			} catch (QueryExceptionHTTP e) {
				e.printStackTrace();
			}
		}

		return indicatorUris;
	}

	public static Map<String, List<String>> findSampleMap(String subject_uri) {
		String sampleQueryString = "";
		if (subject_uri.startsWith("http")) {
			subject_uri = "<" + subject_uri + ">";
		}

		sampleQueryString += NameSpaces.getInstance().printSparqlNameSpaceList();
		sampleQueryString += "SELECT ?sampleUri ?subjectLabel ?sampleType ?sampleLabel ?cohortLabel ?comment WHERE { "
				+	subject_uri	+	"	<http://hadatac.org/ont/hasco/isMemberOf>	?cohort . "
				+ "?sampleUri	<http://hadatac.org/ont/hasco/hasObjectScope> " + subject_uri + " . "
				+ "?sampleUri	rdf:type	<http://semanticscience.org/resource/Sample> . "
				+ "?sampleUri	rdfs:label	?comment . "
				+ "?cohort	rdfs:label	?cohortLabel . "
				+ "OPTIONAL { " + subject_uri + " rdfs:label ?subjectLabel } . "
				+ "OPTIONAL { ?sampleUri rdfs:label ?sampleLabel } . "
				+ "OPTIONAL { ?sampleUri a ?sampleType  } . "
				+ "}";
		
		ResultSetRewindable resultsrw = SPARQLUtils.select(
                CollectionUtil.getCollectionPath(CollectionUtil.Collection.METADATA_SPARQL), sampleQueryString);

		Map<String, List<String>> sampleResult = new HashMap<String, List<String>>();
		List<String> values = new ArrayList<String>();

		while (resultsrw.hasNext()) {
			QuerySolution soln = resultsrw.next();
			//System.out.println("HERE IS THE RAW SOLN*********" + soln.toString());
			values = new ArrayList<String>();
			values.add("Label: " + soln.get("sampleLabel").toString());
			values.add("Type: " + URIUtils.replaceNameSpaceEx(soln.get("sampleType").toString()));
			values.add("Sample Of: " + URIUtils.replaceNameSpaceEx(soln.get("subjectLabel").toString()));
			sampleResult.put(URIUtils.replaceNameSpaceEx(soln.get("sampleUri").toString()), values);
			//System.out.println("THIS IS SUBROW*********" + sampleResult);	
		}

		return sampleResult;
	}

	public static List<String> findSample(String subject_uri) {
		String sampleQueryString = "";
		sampleQueryString += NameSpaces.getInstance().printSparqlNameSpaceList();
		sampleQueryString += "SELECT * WHERE { "
				+ "?s <http://hadatac.org/ont/hasco/hasObjectScope> " + subject_uri + " . "
				+ "}";
		
		ResultSetRewindable resultsrw = SPARQLUtils.select(
                CollectionUtil.getCollectionPath(CollectionUtil.Collection.METADATA_SPARQL), sampleQueryString);

		List<String> sampleResult = new ArrayList<String>();

		while (resultsrw.hasNext()) {
			QuerySolution soln = resultsrw.next();
			//System.out.println("HERE IS THE SAMPLES*********" + soln.toString());
			sampleResult.add(soln.get("s").toString());
			//System.out.println("THIS IS SUBROW*********" + sampleResult);
		}
		return sampleResult;
	}

	@Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
	public Result index(String study_uri, String subject_uri) {
		Map<String, List<String>> indicatorValues = findSubjectIndicators(study_uri, subject_uri);
		Map<String, List<String>> subjectResult = findBasic(subject_uri);
		Map<String, List<String>> sampleResult = findSampleMap(subject_uri);

		Map<String, String> indicatorUris = findSubjectIndicatorsUri(study_uri);

		Map<String, String> showValues = new HashMap<String, String>();
		showValues.put("subject", subject_uri);
		showValues.put("user", UserManagement.getCurrentUserUri());
		showValues.put("study", study_uri);	

		return ok(viewSubject.render(subjectResult, sampleResult, indicatorValues, indicatorUris, showValues));    
	}

	@Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
	public Result postIndex(String study_uri, String subject_uri) {
		return index(study_uri, subject_uri);
	}
}