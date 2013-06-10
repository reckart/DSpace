package cz.cuni.mff.ufal;

import java.io.IOException;
import java.sql.SQLException;

import javax.servlet.ServletException;

import org.apache.log4j.Logger;
import org.dspace.app.util.SubmissionInfo;
import org.dspace.core.ConfigurationManager;
import org.dspace.core.Context;
import org.dspace.handle.HandleManager;

import cz.cuni.mff.ufal.dspace.PIDService;

/*
 * This class is an extension of the DSpace source code. Initial version is 1.6.2
 */
public class DSpaceApi {

	private static final Logger log = Logger.getLogger(DSpaceApi.class);


//    private static IFunctionalities manager = null;
//
//    public static IFunctionalities getFunctionalityManager() {
//        if (manager == null) {
//            loadFunctionalityManager();
//            log.info("IFunctionalities initilized.");
//        }
//        return manager;
//    }
//
//    private static void loadFunctionalityManager() {
//        String configurationDirectory = ConfigurationManager
//                .getProperty("utilities.dir"); // /installations/dspace/conf/utilities
//        String className = ConfigurationManager
//                .getProperty("utilities.functionalitymanager.class"); // cz.cuni.mff.ufal.lindat.utilities.FunctionalityManager
//
//        try {
//
//            log.debug("Loading class : " + className);
//
//            @SuppressWarnings("unchecked")
//            Class<IFunctionalities> functionalities = (Class<IFunctionalities>) Class
//                    .forName(className);
//            Constructor<IFunctionalities> constructor = functionalities
//                    .getConstructor(String.class);
//            manager = constructor.newInstance(configurationDirectory);
//
//            log.debug("Class " + className
//                    + " loaded successfully with configuration "
//                    + configurationDirectory);
//
//        } catch (Exception e) {
//            log.error(
//                    "Failed to initialize the IFunctionalities with configuration "
//                            + configurationDirectory, e);
//        }
//    }
//

    /*
     * Function originally taken from Petr Pajas' modifications.
     */
    public static void submit_step_CompleteStep(Logger log, Context context,
            SubmissionInfo subInfo) throws ServletException {
        // added for UFAL purposes: finally, the item URL should be working and
        // we are able
        // to re-register the PID (handle) to point to the actual item URL in
        // dspace
        try {
            log.debug("doProcessing.CompleteStep.java: Contex commited, now re-registering the PID, now finding handle with context="
                    + context.toString()
                    + ", and subInfo="
                    + subInfo.getSubmissionItem().getItem().toString());
            context.turnOffAuthorisationSystem();
            String handle = HandleManager.findHandle(context, subInfo
                    .getSubmissionItem().getItem());
            log.info("registering final URL for handle " + handle);
            // HandleManager.registerFinalHandleURL(handle);
            cz.cuni.mff.ufal.DSpaceApi
                    .handle_HandleManager_registerFinalHandleURL(log, handle);
            context.restoreAuthSystemState();
        } catch (Exception error) {
            throw new ServletException(error);
        } // end of try - catch block

    }



	/**
	 * Create a new handle PID. This is modified implementation for UFAL, using
	 * the PID service pidconsortium.eu as wrapped in the PIDService class.
	 *
	 * Note: this function creates a handle to a provisional existing URL and
	 * the handle must be updated to point to the final URL once DSpace is able
	 * to report the URL exists (otherwise the pidservice will refuse to set the
	 * URL)
	 *
	 * @return A new handle PID
	 * @exception Exception
	 *                If error occurrs
	 */
	public static String handle_HandleManager_createId(Logger log, int id)
			throws IOException {

		/* Modified by PP for use pidconsortium.eu at UFAL/CLARIN */
		// String handlePrefix =
		// ConfigurationManager.getProperty("handle.prefix");

		String base_url = ConfigurationManager.getProperty("dspace.url") + "?dummy=" + id;

		log.debug("Asking for a new PID using a dummy URL " + base_url);

		/* request a new PID, initially pointing to dspace base_uri+id */
		String pid = PIDService.createPID(base_url);

		log.debug("got PID " + pid);
		return pid;
	}

	/**
	 * Modify an existing PID to point to the corresponding DSpace handle
	 *
	 * @exception SQLException
	 *                If a database error occurs
	 */
	public static void handle_HandleManager_registerFinalHandleURL(Logger log,
			String pid) throws IOException {
		if (pid == null) {
			log.info("Modification failed invalid/null PID.");
			return;
		}

		String url = ConfigurationManager.getProperty("dspace.url");
		url = url + (url.endsWith("/") ? "" : "/") + "handle/" + pid;

		/*
		 * request modification of the PID to point to the correct URL, which
		 * itself should contain the PID as a substring
		 */
		log.debug("Asking for changing the PID '" + pid + "' to " + url);
		String modifiedUrl = PIDService.modifyPID(pid, url);
		System.out.println(modifiedUrl);
		if (!url.equals(modifiedUrl)) {
			throw new IOException("Failed to map PID " + pid + " to " + url);
		}
	}

}