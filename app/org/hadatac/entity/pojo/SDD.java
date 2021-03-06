package org.hadatac.entity.pojo;

import java.io.File;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.jena.query.ResultSetRewindable;
import org.apache.jena.sparql.engine.http.QueryExceptionHTTP;
import org.hadatac.console.controllers.annotator.AnnotationLog;
import org.hadatac.console.http.SPARQLUtils;
import org.hadatac.utils.Templates;
import org.hadatac.data.loader.Record;
import org.hadatac.data.loader.RecordFile;
import org.hadatac.metadata.loader.URIUtils;
import org.hadatac.utils.CollectionUtil;
import org.hadatac.utils.NameSpaces;

public class SDD {
    private Map<String, String> mapCatalog = new HashMap<String, String>();
    private Map<String, String> codeMappings = new HashMap<String, String>();
    private Map<String, String> mapAttrObj = new HashMap<String, String>();
    private Map<String, Map<String, String>> codebook = new HashMap<String, Map<String, String>>();
    private Map<String, Map<String, String>> timeline = new HashMap<String, Map<String, String>>();
    private RecordFile sddfile = null;

    public SDD(RecordFile file) {
        this.sddfile = file;
        readCatalog(file);
    }

    public String getName() {
        String sddName = mapCatalog.get("Study_ID");
        System.out.println("sddName: " + sddName);
        if (sddName == null) {
            return "";
        }
        return sddName;
    }

    public String getNameFromFileName() {
        return (sddfile.getFile().getName().split("\\.")[0]).replace("_", "-").replace("SDD-", "");
    }

    public String getFileName() {
        return sddfile.getFile().getName();
    }

    public Map<String, String> getCatalog() {
        return mapCatalog;
    }

    public Map<String, String> getCodeMapping() {
        return codeMappings;
    }

    public Map<String, String> getMapAttrObj() {
        return mapAttrObj;
    }

    public Map<String, Map<String, String>> getCodebook() {
        return codebook;
    }

    public Map<String, Map<String, String>> getTimeLine() {
        return timeline;
    }

    private void readCatalog(RecordFile file) {
        if (!file.isValid()) {
            return;
        }
        // This is on the infosheet
        for (Record record : file.getRecords()) {
            mapCatalog.put(record.getValueByColumnIndex(0), record.getValueByColumnIndex(1));
        }
    }

    public File downloadFile(String fileURL, String fileName) {
        if(fileURL == null || fileURL.length() == 0){
            return null;
        } else {
            try {
                URL url = new URL(fileURL);
                File file = new File(fileName);
                FileUtils.copyURLToFile(url, file);
                return file;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }
    }

    public boolean checkCellValue(String str) {
        if(str.contains(",")){
            return false;
        }
        if(str.contains(" ")){
            return false;
        }
        return true;
    }

    public boolean checkCellUriRegistered(String str) {
        String prefixString = NameSpaces.getInstance().printSparqlNameSpaceList();
        //        System.out.println(prefixString);
        if (str.contains(":")){
            String[] split = str.split(":");
            String prefixname = split[0];
            if (!prefixString.contains(prefixname)){
                return false;
            }
            return true;
        } else {
            return true;
        }
    }

    public boolean checkCellUriResolvable(String str) {

        if (str.contains(":")){
            if (URIUtils.isValidURI(str)){
                try {
                    URIUtils.convertToWholeURI(str);
                } catch (Exception e) {
                    return false;
                }
            } else {
                return false;
            }
        }
        return true;
    }

    public boolean checkStudyIndicatorPath(String str) {
    	List<String> list = new ArrayList<String>();
    	list.add("sio:TimeStamp");
    	list.add("sio:TimeInstant");
    	list.add("hasco:namedTime");
    	list.add("hasco:originalID");
    	list.add("hasco:uriId");
    	list.add("hasco:hasMetaEntity");
    	list.add("hasco:hasMetaEntityURI");
    	list.add("hasco:hasMetaAttribute");
    	list.add("hasco:hasMetaAttributeURI");
    	list.add("hasco:hasMetaUnit");
    	list.add("hasco:hasMetaUnitURI");
    	list.add("sio:InRelationTo");
    	list.add("hasco:hasLOD");
    	list.add("hasco:hasCalibration");
    	list.add("hasco:hasElevation");
    	list.add("hasco:hasLocation");
    	list.add("chear:AnalysisMode");
    	list.add("chear:LabHubAccession");
    	list.add("chear:LevelOfDetection");
    	list.add("chear:ReplicateNumber");

        if (list.contains(str)) {
            return true;
        }

        String indvIndicatorQuery = "";
        indvIndicatorQuery += NameSpaces.getInstance().printSparqlNameSpaceList();
        indvIndicatorQuery += " SELECT * WHERE { "
                +  str + "  (<>|!<>)* hasco:StudyIndicator ."
                +	" } ";

        try {
            ResultSetRewindable resultsrwIndvInd = SPARQLUtils.select(
                    CollectionUtil.getCollectionPath(CollectionUtil.Collection.METADATA_SPARQL), indvIndicatorQuery);

            List<String> answer = new ArrayList<String>();
            while (resultsrwIndvInd.hasNext()) {
            	if (resultsrwIndvInd.size() > 0) {
            		return true;
            	} else {
            		return false;
            	}
            }
        	System.out.println("The " + str + " get: " + answer);
            return false;
        } catch (QueryExceptionHTTP e) {
            e.printStackTrace();
            AnnotationLog.printException("The 'Attribute' column " + str + " formed a bad query to the KG.", sddfile.getFile().getName());
        }
        return true;
    }

    public void printErrList(List<String> list, int num) {
        if (list.size()>0){
            String listString = "";
            for (String s : list) {
                listString += s + "; ";
            }
            if (num == 1){
                AnnotationLog.printException("The Dictionary Mapping has unresolvable uris in cells: " + listString + " .", sddfile.getFile().getName());
            } else if (num == 2){
                AnnotationLog.printException("The Dictionary Mapping has unregistered namespace in cells: " + listString + " .", sddfile.getFile().getName());
            } else if (num == 3){
                AnnotationLog.printException("The Attributes: " + listString + " NOT hasco:StudyIndicator .", sddfile.getFile().getName());
            } else if (num == 4){
                AnnotationLog.printException("The Dictionary Mapping has incorrect content in :" + listString + "in \"attributeOf\" column.", sddfile.getFile().getName());
            }
        }
    }
    
    public Map<String, List<String>> readDDforEAmerge(RecordFile file) {
    	
    	Map<String, List<String>> mapEAmerge = new HashMap<String, List<String>>();
        List<String> l_dasa = new ArrayList<String>();
    
        if (!file.isValid()) {
            return mapEAmerge;
        }
        
        for (Record record : file.getRecords()) {
        	if (checkCellValue(record.getValueByColumnIndex(0))){		
                if (record.getValueByColumnName("Attribute").length()>0) {
                	if (!l_dasa.contains(record.getValueByColumnIndex(0))) {
                		l_dasa.add(record.getValueByColumnIndex(0));
                	}
                }
        	}
        }
        
        for (Record record : file.getRecords()) {
        	
            if (checkCellValue(record.getValueByColumnIndex(0))){
            	
            	String columnCell = record.getValueByColumnIndex(0);
                String labelCell = record.getValueByColumnName("Label");
                String attrCell = record.getValueByColumnName("Attribute");
                String unitCell = record.getValueByColumnName("Unit");
                String timeCell = record.getValueByColumnName("Time");
                String entityCell = record.getValueByColumnName("Entity");
                String inRelationToCell = record.getValueByColumnName("inRelationTo");
                String attributeOfCell = record.getValueByColumnName("attributeOf");
                String dfCell = record.getValueByColumnName("wasDerivedFrom");	
            	
            	if (l_dasa.contains(columnCell)) {
            		
            		System.out.println("listEAmergeTrigger: " + columnCell + " " + attributeOfCell);
            		
                    List<String> listEAmerge = new ArrayList<String>();
                    
            		if (l_dasa.contains(attributeOfCell)) {
            			
            			if (columnCell != null) {
            				listEAmerge.add(columnCell);
            			} else {
            				listEAmerge.add("");
            			}
            			
            			if (labelCell != null) {
            				listEAmerge.add(labelCell);
            			} else {
            				listEAmerge.add("");
            			}
            			
            			if (unitCell != null) {
            				listEAmerge.add(unitCell);
            			} else {
            				listEAmerge.add("");
            			}
            			
            			if (timeCell != null) {
            				listEAmerge.add(timeCell);
            			} else {
            				listEAmerge.add("");
            			}
            			
            			if (attrCell != null) {
            				listEAmerge.add(attrCell);
            			} else {
            				listEAmerge.add("");
            			}
            			
            			mapEAmerge.put(attributeOfCell, listEAmerge);
            		}
                    System.out.println("listEAmerge :" + listEAmerge);
            	}
            }
        }
//        System.out.println("l_dasa :" + l_dasa);
//        System.out.println("mapEAmerge :" + mapEAmerge.keySet());
        return mapEAmerge;
    }
    

    public boolean readDataDictionary(RecordFile file) {

        if (!file.isValid()) {
            return false;
        }

        if (file.getHeaders().size() > 0){
            AnnotationLog.println("The Dictionary Mapping has " + file.getHeaders().size() + " columns.", sddfile.getFile().getName());	
        } else {
            AnnotationLog.printException("The Dictionary Mapping has " + file.getHeaders().size() + " columns.", sddfile.getFile().getName());
        }

        Boolean uriResolvable = true;
        Boolean namespaceRegisterd = true;
        Boolean isStudyIndicator = true;

        List<String> checkUriRegister = new ArrayList<String>();
        List<String> checkCellVal = new ArrayList<String>();
        List<String> checkUriResolve = new ArrayList<String>();
        List<String> checkStudyIndicatePath = new ArrayList<String>();
        List<String> dasaList = new ArrayList<String>();		
        List<String> dasoList = new ArrayList<String>();
        Map<String, String> sa2so = new HashMap<String, String>();
        Map<String, String> so2so2 = new HashMap<String, String>();
        Map<String, String> so2type = new HashMap<String, String>();
        Map<String, String> so2role = new HashMap<String, String>();
        Map<String, String> so2df = new HashMap<String, String>();

        for (Record record : file.getRecords()) {
            if (checkCellValue(record.getValueByColumnIndex(0))){
                String attributeCell = record.getValueByColumnName("Attribute");
                String entityCell = record.getValueByColumnName("Entity");
                String roleCell = record.getValueByColumnName("Role");
                String relationCell = record.getValueByColumnName("Relation");
                String inRelationToCell = record.getValueByColumnName("inRelationTo");
                String attributeOfCell = record.getValueByColumnName("attributeOf");
                String dfCell = record.getValueByColumnName("wasDerivedFrom");				

                if (checkCellUriResolvable(attributeCell)){
                    if (checkCellUriResolvable(entityCell)){
                        if (checkCellUriResolvable(roleCell)){
                            if (checkCellUriResolvable(relationCell)){

                            } else {
                                uriResolvable = false;
                                checkUriResolve.add(relationCell);
                            }
                        } else {
                            uriResolvable = false;
                            checkUriResolve.add(roleCell);
                        }
                    } else {
                        uriResolvable = false;
                        checkUriResolve.add(entityCell);
                    }
                } else {
                    uriResolvable = false;
                    checkUriResolve.add(attributeCell);
                }

                if (checkCellUriRegistered(attributeCell)){
                    if (checkCellUriRegistered(entityCell)){
                        if (checkCellUriRegistered(roleCell)){
                            if (checkCellUriRegistered(relationCell)){

                            } else {
                                namespaceRegisterd = false;
                                checkUriRegister.add(relationCell);
                            }
                        } else {
                            namespaceRegisterd = false;
                            checkUriRegister.add(roleCell);
                        }
                    } else {
                        namespaceRegisterd = false;
                        checkUriRegister.add(entityCell);
                    }
                } else {
                    namespaceRegisterd = false;
                    checkUriRegister.add(attributeCell);
                }

                if (URIUtils.isValidURI(attributeCell)){
                    isStudyIndicator = checkStudyIndicatorPath(attributeCell);
                    if (!isStudyIndicator) {
                        checkStudyIndicatePath.add(attributeCell);
                    }
                } else {
                    if (entityCell.length() == 0){
                        isStudyIndicator = false;
                        checkStudyIndicatePath.add(attributeCell);
                    }
                }

                if (attributeCell.length()>0) {
                    dasaList.add(record.getValueByColumnIndex(0));
                    if (attributeOfCell.length()>0) {
                        sa2so.put(record.getValueByColumnIndex(0), attributeOfCell);
                    } else {
                        AnnotationLog.printException("Attribute " + record.getValueByColumnIndex(0) + "is not attributeOf any object. Please fix the content.", sddfile.getFile().getName());
                    }
                }

                if (entityCell.length()>0) {
                	if (URIUtils.isValidURI(entityCell)) {
	                    dasoList.add(record.getValueByColumnIndex(0));
	                    if (inRelationToCell.length()>0) {
	                        so2so2.put(record.getValueByColumnIndex(0), inRelationToCell);
	                    } else {
	
	                    }
	
	                    so2type.put(record.getValueByColumnIndex(0), entityCell);
	
	                    if (roleCell.length()>0) {
	                        so2role.put(record.getValueByColumnIndex(0), roleCell);
	                    } else {
	
	                    }
	
	                    if (dfCell.length()>0) {
	                        so2df.put(record.getValueByColumnIndex(0), dfCell);
	                    } else {
	
	                    }
                	} else if (codeMappings.containsKey(entityCell)) {
                    	if (URIUtils.isValidURI(codeMappings.get(entityCell))) {
    	                    dasoList.add(record.getValueByColumnIndex(0));
    	                    if (inRelationToCell.length()>0) {
    	                        so2so2.put(record.getValueByColumnIndex(0), inRelationToCell);
    	                    } else {
    	
    	                    }
    	
    	                    so2type.put(record.getValueByColumnIndex(0), codeMappings.get(entityCell));
    	
    	                    if (roleCell.length()>0) {
    	                        so2role.put(record.getValueByColumnIndex(0), roleCell);
    	                    } else {
    	
    	                    }
    	
    	                    if (dfCell.length()>0) {
    	                        so2df.put(record.getValueByColumnIndex(0), dfCell);
    	                    } else {
    	
    	                    }
                    	}
                	} else {
                        AnnotationLog.printException("The Entity Cell \"" + entityCell + "\" in the \"Entity\" column is either not valid uri or it can not be resolved by replaceNameSpaceEx().", sddfile.getFile().getName());
                        return false;
                	}
                }
                
                if (checkCellValue(record.getValueByColumnName("attributeOf"))){
                    mapAttrObj.put(record.getValueByColumnIndex(0), record.getValueByColumnName("attributeOf"));
                } else {
                    checkCellVal.add(record.getValueByColumnName("attributeOf"));
                	AnnotationLog.printException("\"" + record.getValueByColumnName("attributeOf") + "\" in the attribute of column contains illegal content.", sddfile.getFile().getName());
                    return false;
                }
                
            } else {
                AnnotationLog.printException("The Dictionary Mapping conatins illegal content in \"" + record.getValueByColumnName("Column") + "\" in the \"Column\" column. Check if it contains characters such as \",\" and blank space.", sddfile.getFile().getName());
                return false;
            }

            mapAttrObj.put(record.getValueByColumnName(Templates.LABEL), 
                    record.getValueByColumnName(Templates.ATTTRIBUTEOF));
        }

        printErrList(checkUriResolve, 1);
        printErrList(checkUriRegister, 2);
        printErrList(checkStudyIndicatePath, 3);
        printErrList(checkCellVal, 4);

        if (uriResolvable == true){
            AnnotationLog.println("The Dictionary Mapping has resolvable uris.", sddfile.getFile().getName());	
        }
        if (namespaceRegisterd == true){
            AnnotationLog.println("The Dictionary Mapping has namespaces all registered.", sddfile.getFile().getName());	
        }
        if (isStudyIndicator == true){
            AnnotationLog.println("The Dictionary Mapping has all attributes being subclasses of hasco:StudyIndicator.", sddfile.getFile().getName());	
        }

        AnnotationLog.println("The Dictionary Mapping has correct content under \"Column\" and \"attributeOf\" columns.", sddfile.getFile().getName());
        System.out.println("[SDD] mapAttrObj: " + mapAttrObj);

        return true;
    }

    public boolean readCodeMapping(RecordFile file) {
        if (!file.isValid()) {
            return false;
        }

        for (Record record : file.getRecords()) {
            String code = record.getValueByColumnName("code");
            String uri = record.getValueByColumnName("uri");
            if (uri.startsWith("obo:UO_")) {
                uri = uri.replace("obo:UO_", "uo:");
            }
            codeMappings.put(code, uri);
        }

        if (codeMappings.isEmpty()){
            return false;
        } else {
            return true;
        }
    }

    public boolean readCodebook(RecordFile file) {
        if (!file.isValid()) {
            return false;
        }

        for (Record record : file.getRecords()) {
            if (!record.getValueByColumnName(Templates.LABEL).isEmpty()) {
                String colName = record.getValueByColumnName(Templates.LABEL);
                Map<String, String> mapCodeClass = null;
                if (!codebook.containsKey(colName)) {
                    mapCodeClass = new HashMap<String, String>();
                    codebook.put(colName, mapCodeClass);
                } else {
                    mapCodeClass = codebook.get(colName);
                }
                String classUri = "";
                if (!record.getValueByColumnName(Templates.CLASS).isEmpty()) {
                    // Class column
                    //System.out.println("[SDD] CLASS " + record.getValueByColumnName(Templates.CLASS));
                    classUri = URIUtils.replacePrefixEx(record.getValueByColumnName(Templates.CLASS));
                } 
                //					else {
                //						// Resource column
                //						classUri = URIUtils.replacePrefixEx(record.get(4));
                //					}
                //System.out.println("[SDD] CODE " + record.getValueByColumnName(Templates.CODE));
                mapCodeClass.put(record.getValueByColumnName(Templates.CODE), classUri);
            }
        }

        if (codebook.isEmpty()){
            return false;
        } else {
            return true;
        }
    }

    public boolean readTimeline(RecordFile file) {
        if (!file.isValid()) {
            return false;
        }

        for (Record record : file.getRecords()) {
            if (!record.getValueByColumnName("Name").isEmpty()) {
                String primaryKey = record.getValueByColumnName("Name");

                Map<String, String> timelineRow = new HashMap<String, String>();
                List<String> colNames = new ArrayList<String>();
                colNames.add("Label");
                colNames.add("Type");
                colNames.add("Start");
                colNames.add("End");
                colNames.add("Unit");
                colNames.add("inRelationTo");

                for (String col : colNames) {
                    if (!record.getValueByColumnName(col).isEmpty()) {
                        timelineRow.put(col, record.getValueByColumnName(col));
                    } else {
                        timelineRow.put(col, "null");
                    }
                }
                timeline.put(primaryKey, timelineRow);
            }
        }

        if(timeline.isEmpty()){
            return false;
        } else {
            return true;
        }
    }
}
