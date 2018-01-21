package module;
import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.hadatac.data.loader.AnnotationWorker;

import play.libs.Akka;
import scala.concurrent.duration.FiniteDuration;

@Singleton
public class OnStart {
	
    @Inject
    public OnStart() {
    	initDirectoryStructure();

		// check if default user still have default password. If so, ask to change.
		// TODO: implement this code

		// check if there is at least one user is pre-registered. If not, ask to pre-register at least the main user
		// TODO: implement this code

		// (NOT SURE THIS FUNCTION SHOULD BE CALLED ON ONSTART) check if ontologies are loaded. If not ask to upload them  
		// TODO: implement this code

		// (NOT SURE THIS FUNCTION SHOULD BE CALLED ON ONSTART) check if instances are loaded. 
		//       If not, show how to upload some default instances  
		// TODO: implement this code
    }
    
    private void initDirectoryStructure(){
		List<String> listFolderPaths = new LinkedList<String>();
		listFolderPaths.add("tmp");
		listFolderPaths.add("logs");
		listFolderPaths.add("processed_csv");
		listFolderPaths.add("unprocessed_csv");
		listFolderPaths.add("tmp/ttl");
		listFolderPaths.add("tmp/cache");
		listFolderPaths.add("tmp/uploads");

		for(String path : listFolderPaths){
			File folder = new File(path);
			// if the directory does not exist, create it
			if (!folder.exists()) {
				System.out.println("creating directory: " + path);
				try{
					folder.mkdir();
				} 
				catch(SecurityException se){
					System.out.println("Failed to create directory.");
				}
				System.out.println("DIR created");
			}
		}
	}
}
