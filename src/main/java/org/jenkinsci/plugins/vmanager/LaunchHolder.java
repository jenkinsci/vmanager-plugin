package org.jenkinsci.plugins.vmanager;

import hudson.FilePath;
import hudson.Launcher;
import hudson.model.TaskListener;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.lang.exception.ExceptionUtils;

public class LaunchHolder {

    private StepHolder stepHolder = null;
    private List<String> listOfSessions = null;
    private List<String> listOfSessionsForCountDown = null;
    private static long TIME_TO_SLEEP = 60000;
    private static final String postData1 = "{\"filter\":{\"attName\":\"id\",\"operand\":\"EQUALS\",\"@c\":\".AttValueFilter\",\"attValue\":\"";
    private static final String postData2 = "\"},\"projection\": {\"type\":\"SELECTION_ONLY\",\"selection\":[\"session_status\",\"name\",\"running\",\"waiting\"]}}";
    private static final String runsList = "{\"filter\":{\"condition\":\"AND\",\"@c\":\".ChainedFilter\",\"chain\":[{\"@c\":\".RelationFilter\",\"relationName\":\"session\",\"filter\":{\"condition\":\"AND\",\"@c\":\".ChainedFilter\",\"chain\":[{\"@c\":\".InFilter\",\"attName\":\"id\",\"operand\":\"IN\",\"values\":[\"######\"]}]}}]},\"pageLength\":100000,\"settings\":{\"write-hidden\":true,\"stream-mode\":true},\"projection\": {\"type\": \"SELECTION_ONLY\",\"selection\":[\"test_name\",\"status\",\"duration\",\"test_group\",\"computed_seed\",\"id\",\"first_failure_name\",\"first_failure_description\"###ATTR###]}}";
    private FilePath filePath = null;
    private Utils utils = null;
    Map<String, String> extraAttrLabels = new HashMap<String, String>();
    Map<String, String> sessionFinalState = new HashMap<String, String>();

    public LaunchHolder(StepHolder stepHolder, List<String> listOfSessions, Utils utilsInstance) {
        super();
        this.stepHolder = stepHolder;
        this.listOfSessions = listOfSessions;
        this.utils = utilsInstance;

        this.listOfSessionsForCountDown = new ArrayList<String>();
        Iterator<String> iter = listOfSessions.iterator();
        while (iter.hasNext()) {
            this.listOfSessionsForCountDown.add(iter.next());
        }

    }

    public StepHolder getStepHolder() {
        return stepHolder;
    }

    public void setStepHolder(StepHolder stepHolder) {
        this.stepHolder = stepHolder;
    }

    public List<String> getListOfSessions() {
        return listOfSessions;
    }

    public void setListOfSessions(List<String> listOfSessions) {
        this.listOfSessions = listOfSessions;
    }

    public void performWaiting(String url, boolean requireAuth, String user, String password, TaskListener listener, boolean dynamicUserId, String buildID, int buildNumber, String workPlacePath,
                               int connConnTimeOut, int connReadTimeout, boolean advConfig, boolean notInTestMode, String workingJobDir, Launcher launcher) throws Exception {

        String requestMethod = "POST";
        String apiURL = url + "/rest/sessions/list";
        boolean keepWaiting = true;

        HttpURLConnection conn = null;
        long startTime = new Date().getTime();
        long startTimeForDebugInfo = new Date().getTime();
        long timeToWaitOverall = stepHolder.getStepSessionTimeout() * 60L * 1000L;
        long timeBetweenPrintStatus = 30L * 60L * 1000L;
        boolean debugPrint = true;
        String buildResult = null;

        if (notInTestMode) {
            listener.getLogger().print("Waiting until all sessions will end...\n");
            listener.getLogger().print("Checking for state change every " + (TIME_TO_SLEEP / 60000) + " minutes.\n");
            listener.getLogger().print("Printing out session state every " + (timeBetweenPrintStatus / 60000) + " minutes.\n");
        } else {
            System.out.println("Waiting until all sessions will end...\n");
            System.out.println("Checking for state change every " + (TIME_TO_SLEEP / 60000) + " minutes.");
            System.out.println("Printing out session state every " + (timeBetweenPrintStatus / 60000) + " minutes.");
        }

        // Init the SessionStatusHolder - it will be saving the aggregated
        // sessions info every check in the file system
        SessionStatusHolder sessionStatusHolder = new SessionStatusHolder(url, requireAuth, user, password, listener, dynamicUserId, buildNumber, workPlacePath, buildID, connConnTimeOut,
                connReadTimeout, advConfig, notInTestMode, listOfSessions, stepHolder.isMarkBuildAsFailedIfAllRunFailed(), stepHolder.isFailJobIfAllRunFailed(), workingJobDir, stepHolder.isMarkBuildAsPassedIfAllRunPassed(), stepHolder.isFailJobUnlessAllRunPassed());

        //While we iterate over session status, we can use it to grab the real session name for later usages
        Map<String, String> sessionIdName = new HashMap<String, String>();

        //Since session can finish its execution and start an automatic rerun right after, we need to make sure we wait.
        //We check that by checkig that two times in a row, there are no runs in waiting or running state
        Map<String, String> sessionCompletedLastState = new HashMap<String, String>();

        while (keepWaiting) {

            buildResult = "";
            if (stepHolder.getStepSessionTimeout() != 0) {
                if (new Date().getTime() - startTime > timeToWaitOverall) {
                    // MARK_BUILD_FAIL
                    buildResult = "(" + new Date().toString() + ") - Timeout.  Waiting for more than " + stepHolder.getStepSessionTimeout() + " minutes. Marking build as failed.\n";
                    if (notInTestMode) {
                        listener.getLogger().print(buildResult);
                    } else {
                        System.out.println(buildResult);
                    }
                    break;
                }
            }

            try {
                Thread.sleep(TIME_TO_SLEEP);
            } catch (InterruptedException e1) {
                if (stepHolder.isPauseSessionOnBuildInterruption()) {
                    listener.getLogger().print("Build " + buildID + " was interrupted. checking if there are sessions running in Verisium Manager to be also aborted...");
                    try {
                        java.util.logging.Logger logger = java.util.logging.Logger.getLogger(LaunchHolder.class.getName());
                        abortVManagerSessions(logger, url, requireAuth, user, password, listener, dynamicUserId, buildNumber, workPlacePath, buildID, connConnTimeOut, connReadTimeout, advConfig, notInTestMode, listOfSessions, workingJobDir);
                    } catch (Exception ex) {
                        listener.getLogger().print("Failed to delete session during build removal." + ex.getMessage());
                        listener.getLogger().println(ExceptionUtils.getFullStackTrace(ex));
                    }
                }

                e1.printStackTrace();
                // MARK_BUILD_FAIL
                break;
            }

            try {

                // Check if to print information
                if (new Date().getTime() - startTimeForDebugInfo > timeBetweenPrintStatus) {
                    startTimeForDebugInfo = new Date().getTime();
                    debugPrint = true;
                }

                // Make the vAPI call to get sessions status
                Iterator<String> sessionIter = this.listOfSessions.iterator();
                String tmpSessionId = null;
                String tmpPostData = null;
                String sessionState = null;
                int numOfRunningRuns = 0;
                int numOfWaitingRuns = 0;

                while (sessionIter.hasNext()) {
                    tmpSessionId = sessionIter.next();
                    tmpPostData = postData1 + tmpSessionId + postData2;

                    try {
                        conn = utils.getVAPIConnection(apiURL, requireAuth, user, password, requestMethod, dynamicUserId, buildID, buildNumber, workPlacePath, listener, connConnTimeOut,
                                connReadTimeout, advConfig);

                        OutputStream os = conn.getOutputStream();
                        os.write(tmpPostData.getBytes(StandardCharsets.UTF_8));
                        os.flush();

                        if (checkResponseCode(conn)) {
                            BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream()), StandardCharsets.UTF_8));
                            StringBuilder result = new StringBuilder();
                            String output;
                            while ((output = br.readLine()) != null) {
                                result.append(output);
                            }
                            br.close();

                            JSONArray tmpArray = JSONArray.fromObject(result.toString());

                            // Check if session exist:
                            if (tmpArray.size() == 0) {
                                // MARK_THE_BUILD_FAIL
                                buildResult = "(" + new Date().toString() + ") -  Session id (" + tmpSessionId + ") has been deleted on the Verisium Manager system.  Failing the build.\n";
                                if (notInTestMode) {
                                    listener.getLogger().print(buildResult);
                                } else {
                                    System.out.println(buildResult);
                                }
                                keepWaiting = false;
                                break;
                            }

                            JSONObject tmp = tmpArray.getJSONObject(0);
                            sessionState = tmp.getString("session_status");
                            numOfRunningRuns = Integer.valueOf(tmp.getString("running"));
                            numOfWaitingRuns = Integer.valueOf(tmp.getString("waiting"));

                            //Treat real session state when ALL runs completed
                            if (numOfRunningRuns == 0 && numOfWaitingRuns == 0) {
                                String lastKnownSessionCompletedState = sessionCompletedLastState.get(tmpSessionId);
                                if ("true".equals(lastKnownSessionCompletedState)) {
                                    //This is already the second time. that means no auto re-run and the session don't have any run in running or waiting state - mark as done.
                                    sessionFinalState.put(tmpSessionId, "true");
                                    //Special Treat in case the session state is "Failed" but runs kept running and session completes succesfully
                                    if (("failed").equals(sessionState) && checkIfAllSessionsEnded(tmpSessionId, listener)) {
                                        buildResult = "(" + new Date().toString() + ") - All sessions got into a state in which the build step can continue.\n";
                                        if (notInTestMode) {
                                            listener.getLogger().print(buildResult);
                                        } else {
                                            System.out.println(buildResult);
                                        }
                                        buildResult = "success";
                                        keepWaiting = false;
                                        break;
                                    }
                                } else {
                                    //Mark for first try.  If re-run is started, the second try will invalidate it.
                                    sessionCompletedLastState.put(tmpSessionId, "true");
                                    sessionFinalState.put(tmpSessionId, "false");

                                }
                            } else {
                                sessionCompletedLastState.put(tmpSessionId, "false");
                                sessionFinalState.put(tmpSessionId, "false");
                            }

                            sessionIdName.put(tmpSessionId, tmp.getString("name"));
                            if (notInTestMode) {
                                if (debugPrint) {
                                    listener.getLogger().print(
                                            "(" + new Date().toString() + ") - State of Session '" + tmp.getString("name") + "' (" + tmpSessionId + ") = " + tmp.getString("session_status") + "\n");
                                }
                            } else {
                                if (debugPrint) {
                                    System.out
                                            .println("(" + new Date().toString() + ") - State of Session '" + tmp.getString("name") + "' (" + tmpSessionId + ") = " + tmp.getString("session_status"));
                                }
                            }

                            if (toContinue(sessionState, tmpSessionId, listener)) {
                                // MARK THAT ALL SESSION ENDED
                                buildResult = "(" + new Date().toString() + ") - All sessions got into a state in which the build step can continue.\n";
                                if (notInTestMode) {
                                    listener.getLogger().print(buildResult);
                                } else {
                                    System.out.println(buildResult);
                                }
                                buildResult = "success";
                                keepWaiting = false;
                                break;
                            }


                            if (toFail(sessionState, tmpSessionId, listener)) {
                                // MARK_BUILD_FAIL
                                buildResult = "(" + new Date().toString() + ") - State of Session '" + tmp.getString("name") + "' (" + tmpSessionId + ") = " + tmp.getString("session_status")
                                        + " - Marking build failed.\n";
                                if (notInTestMode) {
                                    listener.getLogger().print(buildResult);
                                } else {
                                    System.out.println(buildResult);
                                }
                                keepWaiting = false;
                                break;
                            }

                            if (toIgnore(sessionState, tmpSessionId, listener)) {
                                // Don't do anything, just continue.
                            }
                        }

                    } catch (java.net.ConnectException e) {
                        if (notInTestMode) {
                            if (debugPrint) {
                                listener.getLogger().print("(" + new Date().toString() + ") - Verisium Manager Server is not responding or is down. Build will keep try to connect.\n");
                            }
                        } else {
                            System.out.println("(" + new Date().toString() + ") - Verisium Manager Server is not responding or is down. Build will keep try to connect.'");
                        }
                        break;
                    } catch (Exception e) {
                        if (notInTestMode) {
                            listener.getLogger().print(e.getMessage());
                        }
                        e.printStackTrace();
                    } finally {
                        if (conn != null) {
                            conn.disconnect();
                        }
                    }

                }

            } catch (Exception e) {
                if (notInTestMode) {
                    listener.getLogger().print(e.getMessage());
                }
                e.printStackTrace();
            } finally {
                if (listOfSessions.size() > 1) {
                    if (notInTestMode) {
                        if (debugPrint) {
                            listener.getLogger().print("\n");
                        }
                    } else {
                        if (debugPrint) {
                            System.out.println("\n");
                        }
                    }
                }
                debugPrint = false;

                // Write the session state information - can be future use by
                // the dashboard
                sessionStatusHolder.dumpSessionStatus(false, sessionIdName, utils, launcher);

            }

        }

        sessionStatusHolder.dumpSessionStatus(true, sessionIdName, utils, launcher);

        // Check if to write the Unit Test XML
        if (stepHolder.getjUnitRequestHolder() != null) {
            //listener.getLogger().print("(" + new Date().toString() + ") Starting to dump JUnit XML ");
            if (stepHolder.getjUnitRequestHolder().isGenerateJUnitXML()) {

                // Fill in the Extra runs attribute map
                apiURL = url + "/rest/$schema/response?action=list&component=runs&extended=true";
                conn = utils.getVAPIConnection(apiURL, requireAuth, user, password, "GET", dynamicUserId, buildID, buildNumber, workPlacePath, listener, connConnTimeOut, connReadTimeout, advConfig);
                BufferedReader brExtra = new BufferedReader(new InputStreamReader((conn.getInputStream()), StandardCharsets.UTF_8));

                StringBuilder resultExtra = new StringBuilder();

                String outputExtra;

                while ((outputExtra = brExtra.readLine()) != null) {
                    resultExtra.append(outputExtra);
                }
                brExtra.close();

                conn.disconnect();

                JSONObject tmp = JSONObject.fromObject(resultExtra.toString());

                JSONObject responseItems = JSONObject.fromObject(tmp.getString("items"));
                JSONObject properties = JSONObject.fromObject(responseItems.getString("properties"));

                List<String> extraItems = new ArrayList<String>();
                if (stepHolder.getjUnitRequestHolder().getStaticAttributeList() != null) {
                    extraItems = Arrays.asList(stepHolder.getjUnitRequestHolder().getStaticAttributeList().split("\\s*,\\s*"));
                }
                Iterator<String> iterExtra = extraItems.iterator();

                String attr = null;
                JSONObject attrObject = null;
                String extraAttributesForRuns = "";

                while (iterExtra.hasNext()) {
                    attr = iterExtra.next();
                    if (properties.has(attr)) {
                        attrObject = JSONObject.fromObject(properties.getString(attr));
                        String attrTitle = attrObject.getString("title");
                        extraAttrLabels.put(attr, attrTitle);

                        if (attr.indexOf(" ") > 0 || attr.equals("first_failure_name") || attr.equals("first_failure_description") || attr.equals("computed_seed") || attr.equals("test_group")
                                || attr.equals("test_name")) {
                            continue;
                        } else {
                            extraAttributesForRuns = extraAttributesForRuns + ",\"" + attr + "\"";
                        }
                    }
                }

                // Get all the runs data from the server
                String runsRestURL = url + "/rest/runs/list";
                Iterator<String> sessionIter = this.listOfSessions.iterator();

                String runsJSONData = null;
                String tmpSessionId = null;
                List<JSONObject> entireSessionsRuns = new ArrayList<JSONObject>();
                while (sessionIter.hasNext()) {
                    runsJSONData = new String(runsList);
                    tmpSessionId = sessionIter.next();
                    runsJSONData = runsJSONData.replaceAll("######", tmpSessionId);
                    runsJSONData = runsJSONData.replaceAll("###ATTR###", extraAttributesForRuns);
                    try {
                        conn = utils.getVAPIConnection(runsRestURL, requireAuth, user, password, requestMethod, dynamicUserId, buildID, buildNumber, workPlacePath, listener, connConnTimeOut,
                                connReadTimeout, advConfig);

                        OutputStream os = conn.getOutputStream();
                        os.write(runsJSONData.getBytes(StandardCharsets.UTF_8));
                        os.flush();

                        if (checkResponseCode(conn)) {
                            BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream()), StandardCharsets.UTF_8));
                            StringBuilder result = new StringBuilder();
                            String output;
                            while ((output = br.readLine()) != null) {
                                result.append(output);
                            }
                            br.close();

                            JSONArray tmpRunsArray = JSONArray.fromObject(result.toString());

                            Iterator<JSONObject> runsIter = tmpRunsArray.iterator();
                            while (runsIter.hasNext()) {
                                entireSessionsRuns.add(runsIter.next());
                            }

                        }
                    } catch (Exception e) {
                        if (notInTestMode) {
                            listener.getLogger().print(e.getMessage());
                        }
                        e.printStackTrace();
                    } finally {
                        if (conn != null) {
                            conn.disconnect();
                        }
                    }
                }
                if (entireSessionsRuns.size() > 0) {
                    UnitTestFormatter unitTestFormatter = new UnitTestFormatter(entireSessionsRuns, tmpSessionId, stepHolder.getjUnitRequestHolder(), extraAttrLabels);
                    unitTestFormatter.dumpXMLFile(workPlacePath, buildNumber, buildID, utils);
                }
            }
        }

        if (!"success".equals(buildResult)) {
            throw new Exception(buildResult);
        }

    }

    private boolean toContinue(String state, String sessionId, TaskListener listener) {
        return checkWhatNext(state, sessionId, "continue", listener);
    }

    private boolean toFail(String state, String sessionId, TaskListener listener) {
        return checkWhatNext(state, sessionId, "fail", listener);
    }

    private boolean toIgnore(String state, String sessionId, TaskListener listener) {
        return checkWhatNext(state, sessionId, "ignore", listener);
    }

    private boolean checkWhatNext(String state, String sessionId, String checkFor, TaskListener listener) {

        if (("inaccessible").equals(state)) {

            if (stepHolder.getInaccessibleResolver().equals(checkFor)) {
                return stepResolver(checkFor, sessionId, listener);
            }

        } else if (("stopped").equals(state)) {

            if (stepHolder.getStoppedResolver().equals(checkFor)) {
                return stepResolver(checkFor, sessionId, listener);
            }

        } else if (("failed").equals(state)) {

            if (stepHolder.getFailedResolver().equals(checkFor)) {
                return stepResolver(checkFor, sessionId, listener);
            }

        } else if (("done").equals(state)) {

            if (stepHolder.getDoneResolver().equals(checkFor)) {
                return stepResolver(checkFor, sessionId, listener);
            }

        } else if (("suspended").equals(state)) {

            if (stepHolder.getSuspendedResolver().equals(checkFor)) {
                return stepResolver(checkFor, sessionId, listener);
            }

        } else if (("completed").equals(state)) {

            if (checkFor.equals("continue")) {
                return checkIfAllSessionsEnded(sessionId, listener);
            }
        }

        return false;

    }

    private boolean checkIfAllSessionsEnded(String sessionId, TaskListener listener) {

        //Only if there's no rerun planned
        //listener.getLogger().print("Checking Session State for real completion - session id ("+ sessionId +"). Checking for completion (2nd check in a row): " + sessionFinalState.get(sessionId) + " \n");
        if ("true".equals(sessionFinalState.get(sessionId))) {
            listOfSessionsForCountDown.remove(sessionId);
        }

        if (listOfSessionsForCountDown.size() == 0) {
            return true;
        } else {
            return false;
        }


    }

    private boolean stepResolver(String checkFor, String sessionId, TaskListener listener) {
        if (checkFor.equals("continue")) {
            return checkIfAllSessionsEnded(sessionId, listener);
        } else if (checkFor.equals("fail")) {
            return true;
        } else if (checkFor.equals("ignore")) {
            //return true;
            return checkIfAllSessionsEnded(sessionId, listener);
        }

        return false;
    }

    /*
    private boolean checkIfAllRunsFinished(String sessionId, TaskListener listener){
        //The ignore feature signals the build to keep waiting.  So far there was no stop condition.  The below also check to
        //see that all runs from that build are not in running or waiting state, and once that stage is being accomplished, we will
        //exit this waiting state.
        if ("true".equals(sessionFinalState.get(sessionId))){
            return checkIfAllSessionsEnded(sessionId,  listener);
        } else {
            return false;
        }
        //listener.getLogger().print("Ignroing Session State for session id ("+ sessionId +"). Waiting until all session's runs will end...\n");

    }
*/

    private boolean checkResponseCode(HttpURLConnection conn) {
        try {
            if (conn.getResponseCode() != HttpURLConnection.HTTP_OK && conn.getResponseCode() != HttpURLConnection.HTTP_NO_CONTENT && conn.getResponseCode() != HttpURLConnection.HTTP_ACCEPTED
                    && conn.getResponseCode() != HttpURLConnection.HTTP_CREATED && conn.getResponseCode() != HttpURLConnection.HTTP_PARTIAL && conn.getResponseCode() != HttpURLConnection.HTTP_RESET
                    && conn.getResponseCode() != 406) {
                return false;
            } else {
                return true;
            }
        } catch (IOException e) {
            // MARK_BUILD_FAIL
            e.printStackTrace();
            return false;
        }
    }

    public void abortVManagerSessions(Logger logger, String url, boolean requireAuth, String user, String password, TaskListener listener, boolean dynamicUserId, int buildNumber, String workPlacePath, String buildId,
                                      int connConnTimeOut, int connReadTimeout, boolean advConfig, boolean notInTestMode, List<String> listOfSessions, String workingJobDir) throws Exception {

        String postData = "{\"filter\":{\"@c\":\".InFilter\",\"attName\":\"id\",\"operand\":\"IN\",\"values\":[" + String.join(",", listOfSessions) + "]}}";
        String apiURL = url + "/rest/sessions/suspend";

        HttpURLConnection conn = utils.getVAPIConnection(apiURL, requireAuth, user, password, "POST", dynamicUserId, buildId, buildNumber, workPlacePath, listener, connConnTimeOut, connReadTimeout, advConfig);

        OutputStream os = conn.getOutputStream();
        os.write(postData.getBytes(StandardCharsets.UTF_8));
        os.flush();

        if (conn.getResponseCode() != HttpURLConnection.HTTP_OK && conn.getResponseCode() != HttpURLConnection.HTTP_NO_CONTENT && conn.getResponseCode() != HttpURLConnection.HTTP_ACCEPTED && conn.getResponseCode() != HttpURLConnection.HTTP_CREATED && conn.getResponseCode() != HttpURLConnection.HTTP_PARTIAL && conn.getResponseCode() != HttpURLConnection.HTTP_RESET) {
            String reason = "";
            if (conn.getResponseCode() == 503) {
                reason = "Failed to suspend sessions.  vAPI process failed to connect to remote Verisium Manager server.";
            }
            if (conn.getResponseCode() == 401) {
                reason = "Failed to suspend sessions.  Authentication Error";
            }
            if (conn.getResponseCode() == 408) {
                reason = "Failed to suspend sessions.  No more licences are available for vAPI";
            }
            logger.log(Level.SEVERE, reason);
            processErrorFromRespone(conn, logger);

        } else {
            logger.log(Level.INFO, "Sessions were suspended in Verisium Manager as a result of build interuption.");
        }

        conn.disconnect();

    }

    public void processErrorFromRespone(HttpURLConnection conn, Logger logger) {
        String errorMessage = "";
        StringBuilder resultFromError = null;
        int responseCode = 0;
        try {
            resultFromError = new StringBuilder(conn.getResponseMessage());
            responseCode = conn.getResponseCode();
            BufferedReader br = new BufferedReader(new InputStreamReader((conn.getErrorStream()), StandardCharsets.UTF_8));

            String output;
            while ((output = br.readLine()) != null) {
                resultFromError.append(output);
            }
            br.close();
        } catch (Exception e) {

        } finally {
            errorMessage = "Failed : HTTP error code : " + responseCode + " (" + resultFromError + ")\n";

            logger.log(Level.SEVERE, errorMessage);

        }
    }

}
