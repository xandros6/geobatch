/** 
 * Java Imports ...
 **/
import it.geosolutions.geobatch.egeos.deployers.services.EGEOSRegistryDeployerConfiguration
import it.geosolutions.geobatch.egeos.deployers.actions.EGEOSDeployerBaseAction

import java.util.logging.Level
import java.util.logging.Logger

import java.util.HashMap;
import java.util.Map;

import it.geosolutions.geobatch.flow.event.IProgressListener
import it.geosolutions.geobatch.flow.event.ProgressListener
import it.geosolutions.geobatch.flow.event.ProgressListenerForwarder

import it.geosolutions.geobatch.flow.event.action.ActionException

import it.geosolutions.geobatch.action.egeos.emsa.PackageType
import it.geosolutions.geobatch.action.egeos.emsa.EMSAIOUtils

import it.geosolutions.geobatch.action.egeos.emsa.features.*;
import it.geosolutions.geobatch.action.egeos.emsa.raster.*;
import it.geosolutions.geobatch.action.scripting.ScriptingConfiguration;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import org.apache.commons.io.FileUtils
import org.apache.commons.io.FilenameUtils
import org.apache.commons.io.filefilter.WildcardFileFilter;

import java.net.MalformedURLException;
import java.net.URL;

import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.postgis.PostgisNGDataStoreFactory;
import org.geotools.jdbc.JDBCDataStoreFactory;

/** 
 * Script Main "execute" function
 **/
/* ----------------------------------------------------------------- */
public List execute(ScriptingConfiguration configuration, String inputFileName, ProgressListenerForwarder listenerForwarder) throws Exception {
		/* ----------------------------------------------------------------- */
		// Main Input Variables: must be configured 

		/** DataStore properties **/
		def database 	= "egeos"
		def dbtype 		= "postgis"
		def host 			= "10.20.2.4"
		def port 			= 5432
		def user 			= "postgres"
		def passwd 		= "postgres_matera"
		
		def SARNetCDFdestDir=new File("/emsa/out/nfs/sarDerived/");
		def ImageIODir=new File("/emsa/out/nfs/sarImages/");

    try {
        listenerForwarder.started();
        // ////
        // Instatiate EMSA Utilities
        // ////
        utils = new EMSAUtils();
				
        // ////////////////////////////////////////////////////////////////////
        //
        // Initializing input variables from Flow configuration
        //
        // ////////////////////////////////////////////////////////////////////
        /* Map props = configuration.getProperties();

        String example0 = props.get("key0");
        listenerForwarder.progressing(50, example0);
        String example1 = props.get("key1");
        listenerForwarder.progressing(90, example1); */
        
        final String filePrefix = FilenameUtils.getBaseName(inputFileName);
        
        final String fileSuffix = FilenameUtils.getExtension(inputFileName);
        
//println("Input file name is: "+inputFileName);
        // ////
		// getting package directory
		// ////
		File pkgDir=null;
        if (fileSuffix.equalsIgnoreCase("xml")) {
			 pkgDir= new File(inputFileName).getParentFile();   
        }
        else
			pkgDir= new File(inputFileName);

        // ////
        // forwarding some logging information to Flow Logger Listener
        // ////
        listenerForwarder.setTask("::EGEOSGeoServerDeployer : Processing event " + inputFileName)

        /** The outcome events variable **/
        List results = new ArrayList();


        // ////
        // getting package type
        // ////
        //PackageType type = utils.getPackageTypeFromName(FilenameUtils.getBaseName(pkgDir.getName()));

//println("File name is: "+pkgDir.getName());

        PackageType type = utils.getPackageTypeFromName(pkgDir.getName());
        
//println("Type is: "+type);

        // ////
        // Update GeoServer DataStore...
        // ////
        // connect to the store
        DataStore store = connect(database, dbtype, host, port, user, passwd);
        listenerForwarder.setTask("::EGEOSGeoServerDeployer : online store ")
        if (type != null) {
            if (type == PackageType.DER) {
                // deploy Ship Detections...
                File[] shipFiles = pkgDir.listFiles(new ShipDetectionNameFilter());

                ShipParser shipParser = new ShipParser();
                for (File shipFile : shipFiles) {
                    shipParser.parseShip(store, ShipParser.xpath, shipFile);
                }

                store.dispose();

                // Forwarding Wind and Wave to GeoBatch METOC Actions...
                File[] ncDerivedFiles = pkgDir.listFiles(new NetCDFNameFilter());

                for (File ncFile : ncDerivedFiles) {
					File destNcFile = new File(SARNetCDFdestDir, ncFile.getName());
					if (destNcFile.exists()) {
						if (destNcFile.delete()) {
							FileUtils.copyFileToDirectory(ncFile, SARNetCDFdestDir, true)
							results.add(destNcFile.getAbsolutePath());
						}
					}
                }
            } else if (type == PackageType.OSW || type == PackageType.OSN) {
                // deploy Oil Spills...
                File[] spillFiles = pkgDir.listFiles(new OilSpillNameFilter());

                SpillParser spillParser = new SpillParser();
                for (File spillFile : spillFiles) {
                    spillParser.parseOilSpill(store, SpillParser.xpath, spillFile);
                }

                store.dispose();
            } else if (type == PackageType.PRO) {
				
                File[] proFiles = pkgDir.listFiles((FilenameFilter)new WildcardFileFilter("*.xml"));
				if (proFiles!=null){
					for (File proXmlFile : proFiles) {
//File test=
						ProParser.copyTif(ProParser.parse(proXmlFile),ImageIODir,120);
//println("RESULTING_FILE: "+test.getAbsolutePath());
					}
				}
//else{
//println("PROFILES list is null ");
//}
                
				results.add(ImageIODir.getAbsolutePath());
            }
        }

        // ////
        // forwarding event to the next action
        // ////
        // fake event to avoid Failed Status!
        results.add("DONE");
        return results;
    } catch (Exception cause) {
        sendError(listenerForwarder, cause.getLocalizedMessage(), cause);
    }
}

// ///////////////////////////////////////////////////////////////////////////// //
//                                                                               //
//                       E-GEOS - U T I L I T I E S                              //
//                                                                               //
// ///////////////////////////////////////////////////////////////////////////// //

/** ****************************************************************************
 Script Utility Methods...
 **************************************************************************** **/

/**
 * Error forwarder...
 **/
void sendError(final ProgressListenerForwarder listenerForwarder, final String message) throws Exception {
    sendError(listenerForwarder, message, null);
}
void sendError(final ProgressListenerForwarder listenerForwarder, final String message, final Throwable cause) throws Exception {
    /**
     * Default LOGGER
     **/
    final Logger LOGGER = Logger.getLogger(EGEOSDeployerBaseAction.class.toString());

    LOGGER.log(Level.SEVERE, message);
    Exception theCause = (cause != null ? cause : new Exception(message));
    listenerForwarder.failed(theCause);
    throw theCause;
}

/**
 * DS connect:
 * - database
 * - dbtype
 * - host
 * - port
 * - user
 * - passwd
 */
private DataStore connect(
            String database,
            String dbtype,
            String host,
            int port,
            String user,
            String passwd)
    throws IOException 
    {
    Map params = new HashMap();
    params.put(JDBCDataStoreFactory.DATABASE.key, database);
    params.put(JDBCDataStoreFactory.DBTYPE.key,   dbtype);
    params.put(JDBCDataStoreFactory.HOST.key,     host);
    params.put(JDBCDataStoreFactory.PORT.key,     port);
    params.put(JDBCDataStoreFactory.USER.key,     user);
    params.put(JDBCDataStoreFactory.PASSWD.key,   passwd);
    // important as there are some chars that need escaping
    params.put(PostgisNGDataStoreFactory.PREPARED_STATEMENTS.key, Boolean.TRUE);

    return DataStoreFinder.getDataStore(params);
}