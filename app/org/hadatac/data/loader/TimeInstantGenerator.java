package org.hadatac.data.loader;

import java.lang.String;
import java.util.Map;

import org.hadatac.utils.ConfigProp;
import org.hadatac.entity.pojo.StudyObject;
import org.hadatac.metadata.loader.URIUtils;
import org.hadatac.entity.pojo.HADatAcThing;


public class TimeInstantGenerator extends BasicGenerator {

    String study_id;
    String file_name;
    final String kbPrefix = ConfigProp.getKbPrefix();
    public TimeInstantGenerator(RecordFile file) {
        super(file);
        file_name = file.getFile().getName();
        study_id = file.getFile().getName().replaceAll("SSD-", "").replaceAll(".xlsx", "");
        
        setStudyUri(URIUtils.replacePrefixEx(kbPrefix + "STD-" + study_id));
    }

    @Override
    public void initMapping() {
        mapCol.clear();
        mapCol.put("originalID", "originalID");
        mapCol.put("Type", "rdf:type");
    }

    private String getUri(Record rec) {
        return kbPrefix + "TIME-" + getOriginalID(rec) + "-" + study_id;
    }

    private String getType(Record rec) {
        return rec.getValueByColumnName(mapCol.get("Type")).replaceAll("(?<=^\\d+)\\.0*$", "");
    }

    private String getOriginalID(Record rec) {
        return rec.getValueByColumnName(mapCol.get("originalID")).replaceAll("(?<=^\\d+)\\.0*$", "");
    }
    
    private String getCollectionUri(Record rec) {
        return kbPrefix + "SOC-" + study_id + "-VISITS";
    }

    public StudyObject createStudyObject(Record record) throws Exception {
        StudyObject obj = new StudyObject(getUri(record), getType(record), 
                getOriginalID(record), "TIME-" + getOriginalID(record) + "-" + study_id, 
                getCollectionUri(record), "");
        
        return obj;
    }
    
    @Override
    HADatAcThing createObject(Record rec, int row_number) throws Exception {
        return createStudyObject(rec);
    }
	
    @Override
    Map<String, Object> createRow(Record rec, int row_number) throws Exception {
        return null;
    }

    @Override
    public void preprocess() throws Exception {
    }

    @Override
    public String getTableName() {
        return "SOC-VISITS";
    }

    @Override
    public String getErrorMsg(Exception e) {
        return "Error in TimeInstantGenerator: " + e.getMessage();
    }
}
