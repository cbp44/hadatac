package org.hadatac.console.controllers.schema;

import org.hadatac.utils.ConfigProp;
import org.hadatac.console.http.GetSparqlQuery;

import play.mvc.Controller;
import play.mvc.Result;
import play.data.*;
import javax.inject.Inject;

import org.hadatac.console.views.html.schema.*;
import org.hadatac.data.api.DataFactory;
import org.hadatac.entity.pojo.DataAcquisitionSchema;
import org.hadatac.entity.pojo.DataAcquisitionSchemaAttribute;
import org.labkey.remoteapi.CommandException;

import be.objectify.deadbolt.java.actions.Group;
import be.objectify.deadbolt.java.actions.Restrict;

import org.hadatac.console.models.DataAcquisitionSchemaForm;
import org.hadatac.console.models.SparqlQuery;
import org.hadatac.console.models.SparqlQueryResults;
import org.hadatac.console.controllers.AuthApplication;
import org.hadatac.console.controllers.schema.NewDAS;
import org.hadatac.console.controllers.annotator.FileProcessing;

public class NewDAS extends Controller {

    public static final String kbPrefix = ConfigProp.getKbPrefix();

    @Inject
    private FormFactory formFactory;

    public static SparqlQueryResults getQueryResults(String tabName) {
        SparqlQuery query = new SparqlQuery();
        GetSparqlQuery query_submit = new GetSparqlQuery(query);
        SparqlQueryResults thePlatforms = null;
        String query_json = null;
        try {
            query_json = query_submit.executeQuery(tabName);
            thePlatforms = new SparqlQueryResults(query_json, false);
        } catch (IllegalStateException | NullPointerException e1) {
            e1.printStackTrace();
        }
        return thePlatforms;
    }

    @Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public Result index() {
        if (ConfigProp.getLabKeyLoginRequired() && session().get("LabKeyUserName") == null && session().get("LabKeyPassword") == null) {
            return redirect(org.hadatac.console.controllers.triplestore.routes.LoadKB.logInLabkey(
                    org.hadatac.console.controllers.schema.routes.NewDAS.index().url()));
        }
        
        return ok(newDAS.render());
    }

    @Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public Result postIndex() {
        return index();
    }

    @Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public Result processForm() {
        Form<DataAcquisitionSchemaForm> form = formFactory.form(DataAcquisitionSchemaForm.class).bindFromRequest();
        if (form.hasErrors()) {
            return badRequest("The submitted form has errors!");
        }

        DataAcquisitionSchemaForm data = form.get();

        String label = data.getLabel();
        DataAcquisitionSchema das = DataFactory.createDataAcquisitionSchema(label);

        String user_name = session().get("LabKeyUserName");
        String password = session().get("LabKeyPassword");
        if (user_name != null && password != null) {
            int nRowsOfSchema = 0;
            
            if (ConfigProp.getLabKeyLoginRequired()) {
                nRowsOfSchema = das.saveToLabKey(session().get("LabKeyUserName"), session().get("LabKeyPassword"));
            }
            
            if (!ConfigProp.getLabKeyLoginRequired() || nRowsOfSchema > 0) {
                das.save();
                return ok(DASConfirm.render("New Data Acquisition Schema", 
                        String.format("%d row(s) have been inserted in Table \"DataAcquisitionSchema\" \n", 
                                nRowsOfSchema),data.getLabel()));
            } else {
                return badRequest("Failed to insert Deployment to LabKey!\n");
            }
        }

        return ok(DASConfirm.render("New Data Acquisition Schema", "no content added to labkey", data.getLabel()));
    }

    @Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public Result processFormFromFile(String attributes) {
        System.out.println("Inside processFormFromFile()");

        Form<DataAcquisitionSchemaForm> form = formFactory.form(DataAcquisitionSchemaForm.class).bindFromRequest();
        if (form.hasErrors()) {
            return badRequest("The submitted form has errors!");
        }

        DataAcquisitionSchemaForm data = form.get();
        String[] fields;

        String label = data.getLabel().replace("DA-","").replace(".csv","").replace(".","").replace("+","-");
        DataAcquisitionSchema das = DataFactory.createDataAcquisitionSchema(label);

        System.out.println("File Processing: [" + attributes + "]");
        if (attributes == null || attributes.equals("")) {
            return ok(editDAS.render(das));	
        }
        fields = FileProcessing.extractFields(attributes);
        System.out.println("# of fields: " + fields.length);
        int pos = 0;
        for (String attribute: fields) {
            //attribute = URLEncoder.encode(attribute);
            String finalAttribute = attribute.replace(".","").replace("+","-").replace(" ","-").replace("?","").replace("(","").replace(")","").replace("\"","");
            System.out.println("Label: " + finalAttribute + "  Pos: " + pos);
            DataAcquisitionSchemaAttribute dasa = new DataAcquisitionSchemaAttribute(kbPrefix + "DASA-" + label + "-" + finalAttribute, das.getUri());
            dasa.setLabel(finalAttribute);
            dasa.setPosition(Integer.toString(pos));
            das.getAttributes().add(dasa);
            pos++;
        }
        
        String user_name = session().get("LabKeyUserName");
        String password = session().get("LabKeyPassword");
        if (user_name != null && password != null) {
            System.out.println("Saving DAS in Labkey");
            if (!ConfigProp.getLabKeyLoginRequired() || das.saveToLabKey(session().get("LabKeyUserName"), session().get("LabKeyPassword")) > 0) {
                das.save();
            } else {
                return badRequest("Failed to insert DA Schema to LabKey!\n");
            }
        }

        return ok(editDAS.render(das));
    }

    @Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public Result processFormFromFileLabels(String attributes) {
        System.out.println("Inside processFormFromFileLabels()");
        Form<DataAcquisitionSchemaForm> form = formFactory.form(DataAcquisitionSchemaForm.class).bindFromRequest();
        if (form.hasErrors()) {
            return badRequest("The submitted form has errors!");
        }

        DataAcquisitionSchemaForm data = form.get();
        String[] fields;

        String label = data.getLabel().replace("DA-","").replace(".csv","").replace(".","").replace("+","-");
        DataAcquisitionSchema das = DataFactory.createDataAcquisitionSchema(label);

        System.out.println("File Processing: [" + attributes + "]");
        if (attributes == null || attributes.equals("")) {
            return ok(editDAS.render(das));	
        }
        fields = FileProcessing.extractFields(attributes);
        System.out.println("# of fields: " + fields.length);
        int pos = 0;
        for (String attribute: fields) {
            //attribute = URLEncoder.encode(attribute);
            String finalAttribute = attribute.replace(".","").replace("+","-").replace(" ","-").replace("?","").replace("(","").replace(")","").replace("\"","");
            System.out.println("Label: " + finalAttribute + "  Pos: " + pos);
            DataAcquisitionSchemaAttribute dasa = new DataAcquisitionSchemaAttribute(kbPrefix + "DASA-" + label + "-" + finalAttribute, das.getUri());
            dasa.setLabel(finalAttribute);
            dasa.setPosition(Integer.toString(-1));
            das.getAttributes().add(dasa);
            pos++;
        }

        String user_name = session().get("LabKeyUserName");
        String password = session().get("LabKeyPassword");
        if (user_name != null && password != null) {
            System.out.println("Saving DAS in Labkey");
            if (!ConfigProp.getLabKeyLoginRequired() || das.saveToLabKey(session().get("LabKeyUserName"), session().get("LabKeyPassword")) > 0) {
                das.save();
            } else {
                return badRequest("Failed to insert DA Schema to LabKey!\n");
            }
        }

        return ok(editDAS.render(das));
    }
}
