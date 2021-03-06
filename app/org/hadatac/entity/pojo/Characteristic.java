package org.hadatac.entity.pojo;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.hadatac.utils.CollectionUtil;

import com.typesafe.config.ConfigFactory;

public class Characteristic extends HADatAcClass implements Comparable<Characteristic> {
	
	static String className = "oboe-core:Characteristic";

	public Characteristic() {
		super(className);
	}
	
	public static Characteristic find(String uri) {
		Characteristic characteristic = null;
		Model model;
		Statement statement;
		RDFNode object;

		String queryString = "DESCRIBE <" + uri + ">";
		Query query = QueryFactory.create(queryString);
		QueryExecution qexec = QueryExecutionFactory.sparqlService(
		        CollectionUtil.getCollectionPath(CollectionUtil.Collection.METADATA_SPARQL), query);
		model = qexec.execDescribe();

		characteristic = new Characteristic();
		StmtIterator stmtIterator = model.listStatements();

		while (stmtIterator.hasNext()) {
			statement = stmtIterator.next();
			object = statement.getObject();
			if (statement.getPredicate().getURI().equals("http://www.w3.org/2000/01/rdf-schema#label")) {
				characteristic.setLabel(object.asLiteral().getString());
			} else if (statement.getPredicate().getURI().equals("http://www.w3.org/2000/01/rdf-schema#subClassOf")) {
				characteristic.setSuperUri(object.asResource().getURI());
			}
		}

		characteristic.setUri(uri);
		characteristic.setLocalName(uri.substring(uri.indexOf('#') + 1));
		if (characteristic.getLabel() == null || characteristic.getLabel().equals("")) {
			characteristic.setLabel(characteristic.getLocalName());
		}

		return characteristic;
	}

	@Override
	public int compareTo(Characteristic o) {
		// TODO Auto-generated method stub
		return 0;
	}
}
