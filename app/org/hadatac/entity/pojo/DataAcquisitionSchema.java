package org.hadatac.entity.pojo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QueryParseException;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSetRewindable;
import org.apache.jena.update.UpdateExecutionFactory;
import org.apache.jena.update.UpdateFactory;
import org.apache.jena.update.UpdateProcessor;
import org.apache.jena.update.UpdateRequest;
import org.hadatac.utils.CollectionUtil;
import org.hadatac.utils.NameSpaces;
import org.hadatac.utils.FirstLabel;
import org.hadatac.metadata.loader.URIUtils;
import org.hadatac.console.http.SPARQLUtils;
import org.hadatac.entity.pojo.DataAcquisitionSchemaAttribute;
import org.hadatac.entity.pojo.DataAcquisitionSchemaObject;
import org.labkey.remoteapi.CommandException;

import org.hadatac.metadata.loader.LabkeyDataHandler;

public class DataAcquisitionSchema extends HADatAcThing {
    public static String INDENT1 = "     ";
    public static String INSERT_LINE1 = "INSERT DATA {  ";
    public static String DELETE_LINE1 = "DELETE WHERE {  ";
    public static String LINE3 = INDENT1 + "a         hasco:DASchema;  ";
    public static String DELETE_LINE3 = INDENT1 + " ?p ?o . ";
    public static String LINE_LAST = "}  ";
    public static String PREFIX = "DAS-";
    public static List<String> METADASA = Arrays.asList(
            "sio:TimeStamp", 
            "sio:TimeInstant", 
            "hasco:namedTime", 
            "hasco:originalID", 
            "hasco:uriId", 
            "hasco:hasMetaEntity", 
            "hasco:hasMetaEntityURI", 
            "hasco:hasMetaAttribute", 
            "hasco:hasMetaAttributeURI", 
            "hasco:hasMetaUnit", 
            "hasco:hasMetaUnitURI", 
            "sio:InRelationTo",
            "hasco:hasLOD",
            "hasco:hasCalibration",
            "hasco:hasElevation",
            "hasco:hasLocation");

    private String uri = "";
    private String label = "";
    private String timestampLabel = "";
    private String timeInstantLabel = "";
    private String namedTimeLabel = "";
    private String idLabel = "";
    private String originalIdLabel = "";
    private String elevationLabel = "";
    private String entityLabel = "";
    private String unitLabel = "";
    private String inRelationToLabel = "";
    private String lodLabel = "";
    private List<DataAcquisitionSchemaAttribute> attributes = new ArrayList<DataAcquisitionSchemaAttribute>();
    private List<DataAcquisitionSchemaObject> objects = new ArrayList<DataAcquisitionSchemaObject>();
    private List<DataAcquisitionSchemaEvent> events = new ArrayList<DataAcquisitionSchemaEvent>();

    public DataAcquisitionSchema() {
    }

    public DataAcquisitionSchema(String uri, String label) {
        this.uri = uri;
        this.label = label;
    }

    public String getUri() {
        return uri;
    }

    public String getUriNamespace() {
        return URIUtils.replaceNameSpaceEx(uri);
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getTimestampLabel() {
        return timestampLabel;
    }

    public void setTimestampLabel(String timestampLabel) {
        this.timestampLabel = timestampLabel;
    }

    public String getTimeInstantLabel() {
        return timeInstantLabel;
    }

    public void setTimeInstantLabel(String timeInstantLabel) {
        this.timeInstantLabel = timeInstantLabel;
    }

    public String getNamedTimeLabel() {
        return namedTimeLabel;
    }

    public void setNamedTimeLabel(String namedTimeLabel) {
        this.namedTimeLabel = namedTimeLabel;
    }

    public String getIdLabel() {
        return idLabel;
    }

    public void setIdLabel(String idLabel) {
        this.idLabel = idLabel;
    }

    public String getOriginalIdLabel() {
        return originalIdLabel;
    }

    public void setOriginalIdLabel(String originalIdLabel) {
        this.originalIdLabel = originalIdLabel;
    }
    
    public String getLODLabel() {
        return lodLabel;
    }

    public void setLODLabel(String lodLabel) {
        this.lodLabel = lodLabel;
    }

    public String getElevationLabel() {
        return elevationLabel;
    }

    public void setElevationLabel(String elevationLabel) {
        this.elevationLabel = elevationLabel;
    }

    public String getEntityLabel() {
        return entityLabel;
    }

    public void setEntityLabel(String entityLabel) {
        this.entityLabel = entityLabel;
    }

    public String getUnitLabel() {
        return unitLabel;
    }

    public void setUnitLabel(String unitLabel) {
        this.unitLabel = unitLabel;
    }

    public String getInRelationToLabel() {
        return inRelationToLabel;
    }

    public void setInRelationToLabel(String inRelationToLabel) {
        this.inRelationToLabel = inRelationToLabel;
    }

    public int getTotalDASA() {
        if (attributes == null) {
            return -1;
        }
        return attributes.size();
    }

    public int getTotalDASE() {
        if (events == null) {
            return -1;
        }
        return events.size();
    }

    public int getTotalDASO() {
        if (objects == null) {
            return -1;
        }
        return objects.size();
    }

    public List<DataAcquisitionSchemaAttribute> getAttributes() {
        return attributes;
    }

    public void setAttributes(List<DataAcquisitionSchemaAttribute> attributes) {
        if (attributes == null) {
            System.out.println("[ERROR] No DataAcquisitionSchemaAttribute for " + uri + " is defined in the knowledge base. ");
        } else {
            this.attributes = attributes;
            for (DataAcquisitionSchemaAttribute dasa : attributes) {
                dasa.setDataAcquisitionSchema(this);

                if (dasa.getAttributes().contains(URIUtils.replacePrefixEx("sio:TimeStamp"))) {
                    setTimestampLabel(dasa.getLabel());
                    System.out.println("[OK] DataAcquisitionSchema TimeStampLabel: " + dasa.getLabel());
                }
                if (dasa.getAttributes().contains(URIUtils.replacePrefixEx("sio:TimeInstant"))) {
                    setTimeInstantLabel(dasa.getLabel());
                    System.out.println("[OK] DataAcquisitionSchema TimeInstantLabel: " + dasa.getLabel());
                }
                if (dasa.getAttributes().contains(URIUtils.replacePrefixEx("hasco:namedTime"))) {
                    setNamedTimeLabel(dasa.getLabel());
                    System.out.println("[OK] DataAcquisitionSchema NamedTimeLabel: " + dasa.getLabel());
                }
                if (dasa.getAttributes().contains(URIUtils.replacePrefixEx("hasco:uriId"))) {
                    setIdLabel(dasa.getLabel());
                    System.out.println("[OK] DataAcquisitionSchema IdLabel: " + dasa.getLabel());
                }
                if (dasa.getAttributes().contains(URIUtils.replacePrefixEx("chear:LevelOfDetection"))) {
                    setLODLabel(dasa.getLabel());
                    System.out.println("[OK] DataAcquisitionSchema LODLabel: " + dasa.getLabel());
                }
                if (dasa.getAttributes().contains(URIUtils.replacePrefixEx("hasco:originalID")) 
                        || dasa.getAttributes().equals(URIUtils.replacePrefixEx("sio:Identifier")) 
                        || Entity.getSubclasses(URIUtils.replacePrefixEx("hasco:originalID")).contains(dasa.getAttributes())) { 
                    setOriginalIdLabel(dasa.getLabel());
                    System.out.println("[OK] DataAcquisitionSchema IdLabel: " + dasa.getLabel());
                }
                if (dasa.getAttributes().contains(URIUtils.replacePrefixEx("hasco:hasEntity"))) {
                    setEntityLabel(dasa.getLabel());
                    System.out.println("[OK] DataAcquisitionSchema EntityLabel: " + dasa.getLabel());
                }
                if (!dasa.getInRelationToUri(URIUtils.replacePrefixEx("sio:hasUnit")).isEmpty()) {
                    String uri = dasa.getInRelationToUri(URIUtils.replacePrefixEx("sio:hasUnit"));
                    System.out.println("uri: " + uri);
                    DataAcquisitionSchemaObject dasoUnit = DataAcquisitionSchemaObject.find(uri);
                    if (dasoUnit != null) {
                        setUnitLabel(dasoUnit.getLabel());
                    } else {
                        DataAcquisitionSchemaAttribute dasaUnit = DataAcquisitionSchemaAttribute.find(uri);
                        if (dasaUnit != null) {
                            setUnitLabel(dasaUnit.getLabel());
                        }
                    }
                    System.out.println("[OK] DataAcquisitionSchema UnitLabel: " + getUnitLabel());
                }
                if (dasa.getAttributes().contains(URIUtils.replacePrefixEx("sio:InRelationTo"))) {
                    setInRelationToLabel(dasa.getLabel());
                    System.out.println("[OK] DataAcquisitionSchema InRelationToLabel: " + dasa.getLabel());
                }
                System.out.println("[OK] DataAcquisitionSchemaAttribute <" + dasa.getUri() + "> is defined in the knowledge base. " + 
                        "Entity: \""    + dasa.getEntityLabel()     + "\"; " + 
                        "Attribute: \"" + dasa.getAttributeLabel() + "\"; " + 
                        "Unit: \""      + dasa.getUnitLabel()       + "\"");
            }
        }
    }

    public List<DataAcquisitionSchemaObject> getObjects() {
        return objects;
    }

    public void setObjects(List<DataAcquisitionSchemaObject> objects) {
        if (objects == null) {
            System.out.println("[WARNING] No DataAcquisitionSchemaObject for " + uri + " is defined in the knowledge base. ");
        } else {
            this.objects = objects;
            for (DataAcquisitionSchemaObject daso : objects) {
                System.out.println("[OK] DataAcquisitionSchemaObject <" + daso.getUri() + "> is defined in the knowledge base. " + 
                        "Role: \""  + daso.getRole() + " InRelationTo: \""  + daso.getInRelationToLabel() + "\"");
            }
        }
    }

    public DataAcquisitionSchemaObject getObject(String dasoUri) {
        for (DataAcquisitionSchemaObject daso : objects) {
            if (daso.getUri().equals(dasoUri)) {
                return daso;
            }
        }
        return null;
    }

    public List<DataAcquisitionSchemaEvent> getEvents() {
        return events;
    }

    public void setEvents(List<DataAcquisitionSchemaEvent> events) {
        if (events == null) {
            System.out.println("[WARNING] No DataAcquisitionSchemaEvent for " + uri + " is defined in the knowledge base. ");
        } else {
            this.events = events;
            for (DataAcquisitionSchemaEvent dase : events) {
                System.out.println("[OK] DataAcquisitionSchemaEvent <" + dase.getUri() + "> is defined in the knowledge base. " + 
                        "Label: \""  + dase.getLabel() + "\"");
            }
        }
    }

    public DataAcquisitionSchemaEvent getEvent(String daseUri) {
        for (DataAcquisitionSchemaEvent dase : events) {
            if (dase.getUri().equals(daseUri)) {
                return dase;
            }
        }
        return null;
    }

    public List<String> defineTemporaryPositions(List<String> csvHeaders) {
        List<String> unknownHeaders = new ArrayList<String>(csvHeaders);

        // Assign DASA positions by label matching
        List<DataAcquisitionSchemaAttribute> listDasa = getAttributes();
        if (listDasa != null && listDasa.size() > 0) {
            // reset temporary positions
            for (DataAcquisitionSchemaAttribute dasa : listDasa) {
                dasa.setTempPositionInt(-1);
            }

            for (int i = 0; i < csvHeaders.size(); i++) {
                for (DataAcquisitionSchemaAttribute dasa : listDasa) {
                    if (csvHeaders.get(i).equalsIgnoreCase(dasa.getLabel())) {
                        dasa.setTempPositionInt(i);
                        unknownHeaders.remove(csvHeaders.get(i));
                    }
                }
            }
        }

        // Assign DASO positions by label matching
        List<DataAcquisitionSchemaObject> listDaso = getObjects();
        if (listDaso != null && listDaso.size() > 0) {
            // reset temporary positions
            for (DataAcquisitionSchemaObject daso : listDaso) {
                daso.setTempPositionInt(-1);
            }

            for (int i = 0; i < csvHeaders.size(); i++) {
                for (DataAcquisitionSchemaObject daso : listDaso) {
                    if (csvHeaders.get(i).equalsIgnoreCase(daso.getLabel())) {
                        daso.setTempPositionInt(i);
                        unknownHeaders.remove(csvHeaders.get(i));
                    }
                }
            }
        }

        return unknownHeaders;
    }

    public int tempPositionOfLabel(String label) {
        if (label == null || label.equals("")) {
            return -1;
        }

        int position = -1;
        for (DataAcquisitionSchemaAttribute dasa : getAttributes()) {
            if (dasa.getLabel().equalsIgnoreCase(label)) {
                position = dasa.getTempPositionInt();
                break;
            }
        }

        if (position != -1) {
            return position;
        }

        for (DataAcquisitionSchemaObject daso : getObjects()) {
            if (daso.getLabel().equalsIgnoreCase(label)) {
                position = daso.getTempPositionInt();
                break;
            }
        }

        return position;
    }

    public static DataAcquisitionSchema find(String schemaUri) {
        System.out.println("Looking for data acquisition schema " + schemaUri);

        if (schemaUri == null || schemaUri.equals("")) {
            System.out.println("[ERROR] DataAcquisitionSchema URI blank or null.");
            return null;
        }

        String queryString = NameSpaces.getInstance().printSparqlNameSpaceList() + 
                " ASK { <" + schemaUri + "> a hasco:DASchema . } ";
        Query query = QueryFactory.create(queryString);

        QueryExecution qexec = QueryExecutionFactory.sparqlService(
                CollectionUtil.getCollectionPath(CollectionUtil.Collection.METADATA_SPARQL), query);
        boolean uriExist = qexec.execAsk();
        qexec.close();

        if (!uriExist) {
            System.out.println("[WARNING] DataAcquisitionSchema. Could not find schema for uri: <" + schemaUri + ">");
            return null;
        }

        DataAcquisitionSchema schema = new DataAcquisitionSchema();
        schema.setUri(schemaUri);
        schema.setLabel(FirstLabel.getLabel(schemaUri));
        schema.setAttributes(DataAcquisitionSchemaAttribute.findBySchema(schemaUri));
        schema.setObjects(DataAcquisitionSchemaObject.findBySchema(schemaUri));
        schema.setEvents(DataAcquisitionSchemaEvent.findBySchema(schemaUri));
        System.out.println("[OK] DataAcquisitionSchema <" + schemaUri + "> exists. " + 
                "It has " + schema.getAttributes().size() + " attributes, " + 
                schema.getObjects().size() + " objects, and " + 
                schema.getEvents().size() + " events.");

        return schema;
    }

    public static List<DataAcquisitionSchema> findAll() {
        List<DataAcquisitionSchema> schemas = new ArrayList<DataAcquisitionSchema>();

        String queryString = NameSpaces.getInstance().printSparqlNameSpaceList() + 
                "SELECT ?uri WHERE { " + 
                "   ?uri a hasco:DASchema . } ";
        
        ResultSetRewindable resultsrw = SPARQLUtils.select(
                CollectionUtil.getCollectionPath(CollectionUtil.Collection.METADATA_SPARQL), queryString);
        
        while (resultsrw.hasNext()) {
            QuerySolution soln = resultsrw.next();
            if (soln != null && soln.getResource("uri").getURI() != null) { 
                DataAcquisitionSchema schema = DataAcquisitionSchema.find(soln.getResource("uri").getURI());
                schemas.add(schema);
            }
        }
        return schemas;
    }

    public static Map<String, Map<String, String>> findPossibleValues(String schemaUri) {
        Map<String, Map<String, String>> mapPossibleValues = new HashMap<String, Map<String, String>>();
        String queryString = NameSpaces.getInstance().printSparqlNameSpaceList()
                + " SELECT ?daso_or_dasa ?codeClass ?code ?codeLabel ?resource WHERE { \n"
                + " ?possibleValue a hasco:PossibleValue . \n"
                + " ?possibleValue hasco:isPossibleValueOf ?daso_or_dasa . \n"
                + " ?possibleValue hasco:hasCode ?code . \n"
                + " ?daso_or_dasa hasco:partOfSchema <" + schemaUri + "> . \n" 
                + " OPTIONAL { ?possibleValue hasco:hasClass ?codeClass } . \n"
                + " OPTIONAL { ?possibleValue hasco:hasResource ?resource } . \n"
                + " OPTIONAL { ?possibleValue hasco:hasCodeLabel ?codeLabel } . \n"
                + " }";

        // System.out.println("findPossibleValues query: " + queryString);

        ResultSetRewindable resultsrw = SPARQLUtils.select(
                CollectionUtil.getCollectionPath(CollectionUtil.Collection.METADATA_SPARQL), queryString);

        try {
            while (resultsrw.hasNext()) {
                String classUri = "";
                QuerySolution soln = resultsrw.next();
                if (soln.get("codeClass") != null && !soln.get("codeClass").toString().isEmpty()) {
                    classUri = soln.get("codeClass").toString();
                } else if (soln.get("resource") != null && !soln.get("resource").toString().isEmpty()) {
                    classUri = soln.get("resource").toString();
                } else if (soln.get("codeLabel") != null && !soln.get("codeLabel").toString().isEmpty()) {
                    // No code class is given, use code label instead
                    classUri = soln.get("codeLabel").toString();
                }

                String daso_or_dasa = soln.getResource("daso_or_dasa").toString();
                String code = soln.getLiteral("code").toString();
                if (mapPossibleValues.containsKey(daso_or_dasa)) {
                    mapPossibleValues.get(daso_or_dasa).put(code.toLowerCase(), classUri);
                } else {
                    Map<String, String> indvMapPossibleValues = new HashMap<String, String>();
                    indvMapPossibleValues.put(code.toLowerCase(), classUri);
                    mapPossibleValues.put(daso_or_dasa, indvMapPossibleValues);
                }
            }
        } catch (Exception e) {
            System.out.println("DataAcquisitionSchema.findPossibleValues() Error: " + e.getMessage());
            e.printStackTrace();
        }

        return mapPossibleValues;
    }

    public static String findByLabel(String schemaUri, String label) {
        System.out.println("findByPosIndex is called!");

        String queryString = NameSpaces.getInstance().printSparqlNameSpaceList()
                + " SELECT ?daso_or_dasa WHERE { "
                + " ?daso_or_dasa rdfs:label \"" + label + "\" . "
                + " ?daso_or_dasa hasco:partOfSchema <" + schemaUri + "> . "
                + " }";

        ResultSetRewindable resultsrw = SPARQLUtils.select(
                CollectionUtil.getCollectionPath(CollectionUtil.Collection.METADATA_SPARQL), queryString);

        if (resultsrw.hasNext()) {
            QuerySolution soln = resultsrw.next();
            return soln.get("daso_or_dasa").toString();
        }

        return "";
    }

    public static Map<String, List<String>> findIdUriMappings(String studyUri) {
        System.out.println("findIdUriMappings is called!");

        Map<String, List<String>> mapIdUriMappings = new HashMap<String, List<String>>();
        String queryString = NameSpaces.getInstance().printSparqlNameSpaceList()
                + " SELECT ?subj_or_sample ?id ?obj ?subj_id WHERE { \n"
                + " { \n"
                + " 	?subj_or_sample hasco:originalID ?id . \n"
                + "     ?subj_or_sample hasco:isMemberOf ?soc . \n"
                + "     ?soc a hasco:SubjectGroup . \n"
                + "     ?soc hasco:isMemberOf* <" + studyUri + "> . \n"
                + " } UNION { \n"
                + " 	?subj_or_sample hasco:originalID ?id . \n"
                + "     ?subj_or_sample hasco:isMemberOf ?soc . \n"
                + "     ?soc a hasco:SampleCollection . \n"
                + " 	?soc hasco:isMemberOf* <" + studyUri + "> . \n"
                + " 	?subj_or_sample hasco:hasObjectScope ?obj . \n"
                + " 	?obj hasco:originalID ?subj_id . \n"
                + " } \n"
                + " } \n";
        
        //System.out.println("findIdUriMappings() queryString: " + queryString);

        ResultSetRewindable resultsrw = SPARQLUtils.select(
                CollectionUtil.getCollectionPath(CollectionUtil.Collection.METADATA_SPARQL), queryString);

        try {
            while (resultsrw.hasNext()) {			
                QuerySolution soln = resultsrw.next();
                List<String> details = new ArrayList<String>();
                if (soln.get("subj_or_sample") != null) {
                    details.add(soln.get("subj_or_sample").toString());
                } else {
                    details.add("");
                }
                if (soln.get("subj_id") != null) {
                    details.add(soln.get("subj_id").toString());
                } else {
                    details.add("");
                }
                if (soln.get("obj") != null) {
                    details.add(soln.get("obj").toString());
                } else {
                    details.add("");
                }
                mapIdUriMappings.put(soln.get("id").toString(), details);
            }
        } catch (Exception e) {
            System.out.println("Error in findIdUriMappings(): " + e.getMessage());
        }

        System.out.println("mapIdUriMappings: " + mapIdUriMappings.keySet().size());
        return mapIdUriMappings;
    }

    public static DataAcquisitionSchema create(String uri) {
        DataAcquisitionSchema das = new DataAcquisitionSchema();
        das.setUri(uri);
        return das;
    }

    @Override
    public int saveToLabKey(String user_name, String password) {
        // SAVING DAS's DASAs
        for (DataAcquisitionSchemaAttribute dasa : attributes) {
            dasa.saveToLabKey(user_name, password);
        }

        // SAVING DAS ITSELF
        LabkeyDataHandler loader = LabkeyDataHandler.createDefault(user_name, password);
        List< Map<String, Object> > rows = new ArrayList< Map<String, Object> >();
        Map<String, Object> row = new HashMap<String, Object>();
        row.put("hasURI", URIUtils.replaceNameSpaceEx(getUri()));
        row.put("a", "hasco:DataAcquisitionSchema");
        row.put("rdfs:label", getLabel());
        rows.add(row);

        try {
            return loader.insertRows("DASchema", rows);
        } catch (CommandException e) {
            System.out.println("[ERROR] Failed to insert DA Schemas to LabKey!");
            e.printStackTrace();
            return 0;
        }
    }

    @Override
    public int deleteFromLabKey(String user_name, String password) {
        // DELETING DAS's DASAs
        for (DataAcquisitionSchemaAttribute dasa : attributes) {
            dasa.deleteFromLabKey(user_name, password);
        }

        // DELETING DAS ITSELF
        LabkeyDataHandler loader = LabkeyDataHandler.createDefault(user_name, password);
        List< Map<String, Object> > rows = new ArrayList< Map<String, Object> >();
        Map<String, Object> row = new HashMap<String, Object>();
        row.put("hasURI", URIUtils.replaceNameSpaceEx(getUri()));
        rows.add(row);

        try {
            return loader.deleteRows("DASchema", rows);
        } catch (CommandException e) {
            System.out.println("[ERROR] Failed to delete DA Schemas from LabKey!");
            e.printStackTrace();
            return 0;
        }
    }

    @Override
    public boolean saveToTripleStore() {
        // SAVING DAS's DASAs
        for (DataAcquisitionSchemaAttribute dasa : attributes) {
            dasa.saveToTripleStore();
        }

        // SAVING DAS ITSELF
        String insert = "";
        insert += NameSpaces.getInstance().printSparqlNameSpaceList();
        insert += INSERT_LINE1;
        
        if (!getNamedGraph().isEmpty()) {
            insert += " GRAPH <" + getNamedGraph() + "> { ";
        }
        
        insert += this.getUri() + " a hasco:DASchema . ";
        insert += this.getUri() + " rdfs:label  \"" + this.getLabel() + "\" . ";
        
        if (!getNamedGraph().isEmpty()) {
            insert += " } ";
        }
        
        if (!getNamedGraph().isEmpty()) {
            insert += " } ";
        }
        
        if (!getNamedGraph().isEmpty()) {
            insert += " } ";
        }
        
        insert += LINE_LAST;

        try {
            UpdateRequest request = UpdateFactory.create(insert);
            UpdateProcessor processor = UpdateExecutionFactory.createRemote(
                    request, CollectionUtil.getCollectionPath(CollectionUtil.Collection.METADATA_UPDATE));
            processor.execute();
        } catch (QueryParseException e) {
            System.out.println("QueryParseException due to update query: " + insert);
            throw e;
        }

        return true;
    }

    @Override
    public void deleteFromTripleStore() {
        String query = "";
        query += NameSpaces.getInstance().printSparqlNameSpaceList();
        query += DELETE_LINE1;
        query += "<" + this.getUri() + ">  ";
        query += DELETE_LINE3;
        query += LINE_LAST;
        UpdateRequest request = UpdateFactory.create(query);
        UpdateProcessor processor = UpdateExecutionFactory.createRemote(
                request, CollectionUtil.getCollectionPath(CollectionUtil.Collection.METADATA_UPDATE));
        processor.execute();
    }

    @Override
    public boolean saveToSolr() {
        return false;
    }

    @Override
    public int deleteFromSolr() {
        return 0;
    }
}
