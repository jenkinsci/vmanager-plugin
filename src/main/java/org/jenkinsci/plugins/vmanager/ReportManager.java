/*
 * The MIT License
 *
 * Copyright 2019 Cadence.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.jenkinsci.plugins.vmanager;

import hudson.FilePath;
import hudson.Launcher;
import hudson.model.Executor;
import hudson.model.Job;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.plugins.vmanager.BuildStatusMap;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;

import org.apache.http.HttpEntity;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.util.EntityUtils;
import org.jenkinsci.plugins.tokenmacro.TokenMacro;
import org.json.simple.JSONArray;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 *
 * @author tyanai
 */
public class ReportManager {

    private final static String runsFilter = "{\"filter\":{\"@c\":\".RelationFilter\",\"relationName\":\"session\",\"filter\":{\"@c\":\".ChainedFilter\",\"condition\":\"OR\",\"chain\":[" + "######" + "]}}}";

    private Run<?, ?> build;
    private SummaryReportParams summaryReportParams;
    private VAPIConnectionParam vAPIConnectionParam;
    private VMGRRun vmgrRun;
    private TaskListener listener;
    private boolean testMode = false;
    private Utils utils;
   

    public ReportManager(Run<?, ?> build, SummaryReportParams summaryReportParams, VAPIConnectionParam vAPIConnectionParam, TaskListener listener,FilePath filePath) {
        this.build = build;
        this.summaryReportParams = summaryReportParams;
        this.vAPIConnectionParam = vAPIConnectionParam;
        this.listener = listener;
              
        if (vAPIConnectionParam != null){
            try {
                this.vAPIConnectionParam.vAPIUrl = TokenMacro.expandAll(build,filePath, listener, vAPIConnectionParam.vAPIUrl);
            } catch (Exception e) {
                e.printStackTrace();
                listener.getLogger().println("Failed to extract out macro from the input of vAPIUrl: " + vAPIConnectionParam.vAPIUrl);
            }
        }

        Job job = build.getParent();
        String workingDir = job.getBuildDir() + File.separator + build.getNumber();
        vmgrRun = new VMGRRun(build, workingDir, job.getBuildDir().getAbsolutePath());
        this.utils = new Utils(build,listener,filePath);
        

    }

    public ReportManager(SummaryReportParams summaryReportParams, VAPIConnectionParam vAPIConnectionParam, boolean testMode) {
        this.testMode = testMode;
        this.summaryReportParams = summaryReportParams;
        this.vAPIConnectionParam = vAPIConnectionParam;
    }

    private String buildPostDataSessionFilter() {

        String sessionIdFromBuild;
        String[] listOfSessions;
        if (this.testMode) {
            listOfSessions = new String[1];
            listOfSessions[0] = "1";
        } else {
            sessionIdFromBuild = BuildStatusMap.getValue(vmgrRun.getRun().getId(), vmgrRun.getRun().getNumber(), vmgrRun.getJobWorkingDir() + "", "id", true);
            listOfSessions = sessionIdFromBuild.split("\\s*,\\s*");
        }

        String result = "";
        int commaCounter = listOfSessions.length - 1;
        for (String listOfSession : listOfSessions) {
            result = result + "{\"attName\":\"id\",\"operand\":\"EQUALS\",\"@c\":\".AttValueFilter\",\"attValue\":\"" + listOfSession.trim() + "\"}";
            if (commaCounter > 0) {
                result = result + ",";
            }
            commaCounter--;
        }
        return result;
    }

    private String getReportEmailAddresses() {

        String[] emails;
        StringBuilder output = null;
        String result = null;

        if ("static".equals(summaryReportParams.emailType)) {
            String[] values = summaryReportParams.emailList.split(",");
            output = new StringBuilder("");
            for (String email : values) {
                output.append("\"").append(email.trim()).append("\",");
            }
            //Remove the last comma
            if (output.length() > 2) {
                result = output.toString().substring(0, output.toString().length() - 1);
            }
        } else {
            try {
                emails = utils.loadDataFromInputFiles(vmgrRun.getRun().getId(), vmgrRun.getRun().getNumber(), "" + vmgrRun.getJobWorkingDir(), summaryReportParams.emailInputFile, listener, summaryReportParams.deleteEmailInputFile, "emails", "emails.input");
                output =  new StringBuilder("");
                for (String email : emails) {
                    output.append("\"").append(email.trim()).append("\",");
                }

                //Remove the last comma
                if (output.length() > 2) {
                    result = output.toString().substring(0, output.toString().length() - 1);
                }

            } catch (Exception e) {
                e.printStackTrace();
                listener.getLogger().println("Failed to find the email input file " + summaryReportParams.emailInputFile + " or any email file within the workspace for this build.\n " + e.getMessage());
            }
        }

        return result;

    }

    public String buildPostParamForSummaryReport(boolean email) throws Exception {

        if (email) {
            if (!this.testMode) {
                listener.getLogger().println("Starting to build the POST API part for sending the report email.");
            }
        } else {
            if (!this.testMode) {
                listener.getLogger().println("Starting to build the POST API part for creating summary report.");
            }
        }

        JSONParser jsonParser = new JSONParser();
        String postData = "";

        if (summaryReportParams.summaryType.equals("wizard")) {
            if (!this.testMode) {
                listener.getLogger().println("ReportManager - Using wizrd based json to bring the report...");
            }

            JSONObject metricsData;
            JSONObject vplanData;
            JSONObject ctxData = null;

            //Go over static params
            String staticParams = SummaryReportParams.staticReportParams;

            if (summaryReportParams.runReport) {
                summaryReportParams.includeTests = true;
                String testsDepth = ",\"testsDepth\":" + summaryReportParams.testsDepth;
                String testsViewName = ",\"testsViewName\":\"" + summaryReportParams.testsViewName.trim() + "\"";

                staticParams = staticParams.replace("$test_depth", testsDepth);
                staticParams = staticParams.replace("$test_view_name", testsViewName);
            } else {
                summaryReportParams.includeTests = false;
                staticParams = staticParams.replace("$test_depth", "");
                staticParams = staticParams.replace("$test_view_name", "");
            }

            if (summaryReportParams.metricsReport) {
                String metricsViewName = ",\"metricsViewName\":\"" + summaryReportParams.metricsViewName.trim() + "\"";
                staticParams = staticParams.replace("$metrics_view_name", metricsViewName);
            } else {
                staticParams = staticParams.replace("$metrics_view_name", "");
            }

            if (summaryReportParams.vPlanReport) {
                String vplanViewName = ",\"vplanViewName\":\"" + summaryReportParams.vplanViewName.trim() + "\"";
                staticParams = staticParams.replace("$vplan_view_name", vplanViewName);
            } else {
                staticParams = staticParams.replace("$vplan_view_name", "");
            }

            //If this is an email send operation, set the emails and remove the url link
            if (email) {
                staticParams = staticParams.replace("$link_output", "false");
                staticParams = staticParams.replace("$jenkins_mode", "");
                staticParams = "\"emails\":[" + getReportEmailAddresses() + "]," + staticParams;
            } else {
                staticParams = staticParams.replace("$link_output", "true");
            }

            //set if stream mode of not based on vManager version
            if ("stream".equals(summaryReportParams.vManagerVersion)) {
                staticParams = staticParams.replace("$jenkins_mode", "\"jenkins\":true,");
            } else {
                staticParams = staticParams.replace("$jenkins_mode", "");
            }

            postData = postData + "{" + staticParams + ",\"includeTests\":" + summaryReportParams.includeTests;

            if (summaryReportParams.metricsReport) {
                String tmpDataHolder = null;
                try {
                    if (summaryReportParams.metricsInputType.equals("basic")) {
                        tmpDataHolder = SummaryReportParams.metricsData;
                        metricsData = (JSONObject) jsonParser.parse(tmpDataHolder);
                        metricsData.replace("depth", summaryReportParams.metricsDepth);
                    } else {
                        try {
                            Executor exceutor = build.getExecutor();
                            FilePath filePath = null;
                            if (exceutor != null){
                                filePath = exceutor.getCurrentWorkspace();
                            } else {
                                throw new Exception("Failed to find Executor");
                            }
                            tmpDataHolder =TokenMacro.expandAll(build, filePath, listener, summaryReportParams.metricsAdvanceInput.trim());
                        } catch (Exception e) {
                            e.printStackTrace();
                            listener.getLogger().println("Failed to extract out macro from the input of report metricsAdvanceInput: " + summaryReportParams.metricsAdvanceInput);
                            tmpDataHolder = summaryReportParams.metricsAdvanceInput.trim();
                        }
                        
                        metricsData = (JSONObject) jsonParser.parse(tmpDataHolder);
                    }
                } catch (Exception e) {
                    listener.getLogger().println("ReportManager - fail to parse metricsData json input: " + tmpDataHolder);
                    throw e;
                }
                postData = postData + ",\"metricsData\":[" + metricsData.toJSONString() + "]";
            }
            
            String parsedVPlanFileName = null;
            if (summaryReportParams.vPlanReport) {
                String tmpDataHolder = null;
                
                try {
                    if (summaryReportParams.vPlanInputType.equals("basic")) {
                        tmpDataHolder = SummaryReportParams.vPlanData;
                        vplanData = (JSONObject) jsonParser.parse(tmpDataHolder);
                        vplanData.replace("depth", summaryReportParams.vPlanDepth);
                    } else {
                        try {
                            Executor exceutor = build.getExecutor();
                            FilePath filePath = null;
                            if (exceutor != null){
                                filePath = exceutor.getCurrentWorkspace();
                            } else {
                                throw new Exception("Failed to find Executor");
                            }
                            tmpDataHolder =TokenMacro.expandAll(build, filePath, listener, summaryReportParams.vPlanAdvanceInput.trim());
                        } catch (Exception e) {
                            e.printStackTrace();
                            listener.getLogger().println("Failed to extract out macro from the input of report vPlanAdvanceInput: " + summaryReportParams.vPlanAdvanceInput);
                            tmpDataHolder = summaryReportParams.vPlanAdvanceInput.trim();
                        }
                        
                        vplanData = (JSONObject) jsonParser.parse(tmpDataHolder);
                    }
                } catch (Exception e) {
                    listener.getLogger().println("ReportManager - fail to parse vplanData json input: " + tmpDataHolder);
                    throw e;
                }

                try {
                    ctxData = (JSONObject) jsonParser.parse(SummaryReportParams.ctxData);
                    
                    //Check if this is a vPlan in DB
                    if (summaryReportParams.vPlanxFileName.trim().indexOf("(DB)") > -1){
                        ctxData.put("db-vplan", true);
                        summaryReportParams.vPlanxFileName = summaryReportParams.vPlanxFileName.substring(0,summaryReportParams.vPlanxFileName.trim().indexOf("(DB)")).trim();
                    }
                    
                    try {
                        Executor exceutor = build.getExecutor();
                        FilePath filePath = null;
                        if (exceutor != null){
                            filePath = exceutor.getCurrentWorkspace();
                        } else {
                            throw new Exception("Failed to find Executor");
                        }
                        parsedVPlanFileName =TokenMacro.expandAll(build, filePath, listener, summaryReportParams.vPlanxFileName.trim());
                    } catch (Exception e) {
                        e.printStackTrace();
                        listener.getLogger().println("Failed to extract out macro from the input of report vPlanxFileName: " + summaryReportParams.vPlanxFileName);
                        parsedVPlanFileName = summaryReportParams.vPlanxFileName.trim();
                    }
                    
                    ctxData.put("vplanFile", parsedVPlanFileName);
                } catch (Exception e) {
                    listener.getLogger().println("ReportManager - fail to parse ctxData json input for vPlan name: " + summaryReportParams.vPlanxFileName);
                    throw e;
                }

                postData = postData + ",\"vplanData\":[" + vplanData.toJSONString() + "]";
            }

            //See if there's anything additional that comes from ctxData optional input:
            if (summaryReportParams.ctxInput) {
                try {
                    String ctxDataStringForEvaluating = null;
                    try {
                        Executor exceutor = build.getExecutor();
                        FilePath filePath = null;
                        if (exceutor != null){
                            filePath = exceutor.getCurrentWorkspace();
                        } else {
                            throw new Exception("Failed to find Executor");
                        }
                        ctxDataStringForEvaluating = TokenMacro.expandAll(build, filePath, listener, summaryReportParams.ctxAdvanceInput);
                    } catch (Exception e) {
                        e.printStackTrace();
                        listener.getLogger().println("Failed to extract out macro from the input of report ctxData: " + summaryReportParams.ctxAdvanceInput);
                        ctxDataStringForEvaluating = summaryReportParams.ctxAdvanceInput;
                    }
                  
                    ctxData = (JSONObject) jsonParser.parse(ctxDataStringForEvaluating);
                    
                    
                    if (summaryReportParams.vPlanReport) {
                        if (!summaryReportParams.vPlanxFileName.trim().equals("")) {
                            //There is a vPlan, check if there's also in the ctxData
                            if (!ctxData.containsKey("vplanFile")) {
                                ctxData.put("vplanFile", parsedVPlanFileName);
                            }
                        }
                    }
                    
                    

                } catch (Exception e) {
                    listener.getLogger().println("ReportManager - fail to parse ctxData json input for vPlan name: " + summaryReportParams.vPlanxFileName);
                    throw e;
                }

                postData = postData + ",\"ctxData\":" + ctxData.toJSONString();
            } else {
                if (summaryReportParams.vPlanReport) {
                    postData = postData + ",\"ctxData\":" + ctxData.toJSONString();
                }
            }

            postData = postData + ",\"rs\":" + runsFilter.replace("######", buildPostDataSessionFilter()) + "}";

        } else {
            //User choose to place his own full vAPI request.  All we need to do is to add the RS part and send it over.
            if (!this.testMode) {
                listener.getLogger().println("ReportManager - Using user freestyle json to bring the report...");
            }
            //Load json from file

            String freeVAPISyntax;
            if (this.testMode) {
                freeVAPISyntax = utils.loadUserSyntaxForSummaryReport("20", 20, "" + "c://temp", summaryReportParams.freeVAPISyntax, null, summaryReportParams.deleteReportSyntaxInputFile);
            } else {
                freeVAPISyntax = utils.loadUserSyntaxForSummaryReport(vmgrRun.getRun().getId(), vmgrRun.getRun().getNumber(), "" + vmgrRun.getJobWorkingDir(), summaryReportParams.freeVAPISyntax, listener, summaryReportParams.deleteReportSyntaxInputFile);
            }
            if (!this.testMode) {
                listener.getLogger().println("ReportManager - User freestyle syntax is:\n" + freeVAPISyntax + "\n");
            }

            JSONObject userSyntaxData;
            try {
                userSyntaxData = (JSONObject) jsonParser.parse(freeVAPISyntax);
            } catch (Exception e) {
                listener.getLogger().println("ReportManager - fail to parse user free syntax json input for summary report: " + summaryReportParams.vPlanxFileName);
                throw e;
            }

            //Add the RS part
            JSONObject rsData;
            try {
                rsData = (JSONObject) jsonParser.parse(runsFilter.replace("######", buildPostDataSessionFilter()));
            } catch (Exception e) {
                listener.getLogger().println("ReportManager - fail to parse rsData for sessions list: " + buildPostDataSessionFilter());
                throw e;
            }
            userSyntaxData.put("rs", rsData);

            //If this is 19.09 server adds the jenkins:true key, otherwise remove the key
            if (userSyntaxData.containsKey("jenkins")) {
                userSyntaxData.remove("jenkins");
            }
            if ("stream".equals(summaryReportParams.vManagerVersion)) {
                userSyntaxData.put("jenkins", true);
            }

            //Add the email part
            //If this is an email send operation, set the emails and remove the url link
            if (email) {
                //If user set his own email, continue and skip
                if (!userSyntaxData.containsKey("emails")) {
                    JSONArray jsonArray = (JSONArray) jsonParser.parse("[" + getReportEmailAddresses() + "]");
                    userSyntaxData.put("emails", jsonArray);
                }
                //Also no need for jenkins:true
                if (userSyntaxData.containsKey("jenkins")) {
                    userSyntaxData.remove("jenkins");
                }
                //Also no need for link_output:true
                if (userSyntaxData.containsKey("linkOutput")) {
                    userSyntaxData.remove("linkOutput");
                }
            }

            postData = userSyntaxData.toJSONString();

        }

        return postData;

    }

    public void fetchFromRemoteURL(String reportUrl) throws Exception {

        JSONParser jsonParser = new JSONParser();
        JSONObject urlObject;
        try {
            urlObject = (JSONObject) jsonParser.parse(reportUrl);
        } catch (Exception e) {
            listener.getLogger().println("ReportManager - fail to parse url from /reports/generate-summary-report: " + reportUrl);
            throw e;
        }

        int buildNumber = 20;
        String buildId = "20";
        String jobWorkingDir = "c://temp";
        

        if (!this.testMode) {
            buildNumber = vmgrRun.getRun().getNumber();
            buildId = vmgrRun.getRun().getId();
            jobWorkingDir = vmgrRun.getJobWorkingDir();
        }

        //Fix for SECURITY-1615 - Use Apache dedicated instead
        CloseableHttpClient httpClient = null;
        if (summaryReportParams.ignoreSSLError) {
            httpClient = fixUntrustCertificate();
        } else {
            httpClient = HttpClients.createDefault();
        }

        String username = vAPIConnectionParam.vAPIUser;
        String thePath = vAPIConnectionParam.vAPIUrl + "/rest/reports" + urlObject.get("path");

        HttpGet httpGet = new HttpGet(thePath);

        if (vAPIConnectionParam.authRequired) {
            // ----------------------------------------------------------------------------------------
            // Authentication
            // ----------------------------------------------------------------------------------------
            if (vAPIConnectionParam.dynamicUserId) {
                BufferedReader reader = null;
                try {

                    reader = utils.loadFileFromWorkSpace(buildId, buildNumber, jobWorkingDir, null, listener, false, "user.input");
                    String line = null;
                    while ((line = reader.readLine()) != null) {
                        username = line;
                        break;
                    }
                } catch (Exception e) {

                    if (!this.testMode) {
                        listener.getLogger().print("Failed to read input file for the dynamic users. \n");
                    } else {

                        System.out.println("Failed to read input file for the dynamic users. \n");
                    }
                    throw e;
                } finally {
                    if (reader != null){
                        reader.close();
                    }
                    
                }
            }

            String userpass = username + ":" + vAPIConnectionParam.vAPIPassword;
            String basicAuth = "Basic " + java.util.Base64.getUrlEncoder().encodeToString(userpass.getBytes(Charset.forName("UTF-8")));
            httpGet.setHeader("Authorization", basicAuth);
        }

        CloseableHttpResponse httpResponse = httpClient.execute(httpGet);
        StatusLine statusLine = httpResponse.getStatusLine();
        if (statusLine.getStatusCode() == HttpURLConnection.HTTP_OK) {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            try {
                HttpEntity entity = httpResponse.getEntity();
                entity.writeTo(outputStream);
                EntityUtils.consume(entity);

                String output = outputStream.toString(Charset.forName("UTF-8"));
                int start = output.indexOf("<head>");
                int end = output.indexOf("</head>") + 7;
                output = output.substring(0, start) + output.substring(end, output.length());

                start = output.indexOf("<style>");
                end = output.indexOf("</style>") + 8;
                output = output.substring(0, start) + output.substring(end, output.length());

                start = output.indexOf("<script>");
                end = output.indexOf("</script>") + 9;
                output = output.substring(0, start) + output.substring(end, output.length());

                start = output.indexOf("<script>");
                end = output.indexOf("</script>") + 9;
                output = output.substring(0, start) + output.substring(end, output.length());

                output = output.replace("<html>", "");
                output = output.replace("</html>", "");
                output = output.replace("<body>", "");
                output = output.replace("</body>", "");

                String fileOutput = /*jobWorkingDir + File.separator +*/ buildNumber + "." + buildId + ".summary.report";
                if (utils.getFilePath() == null){
                    //Pipeline always run on master
                    fileOutput = jobWorkingDir + File.separator + fileOutput;            
                }
                StringBuffer writer = new StringBuffer();
                writer.append(output);
               

                utils.saveFileOnDisk(fileOutput, writer.toString());
                utils.moveFromNodeToMaster(buildNumber + "." + buildId + ".summary.report");
               

                if (!this.testMode) {
                    listener.getLogger().println("Report Summary was created succesfully.");
                }
            } finally {
                httpResponse.close();
            }
        }

    }

    private void convertBYORToJenkinsType(int buildNumber, String buildId, String jobWorkingDir)  throws Exception {

        try{
            
            String output = utils.getFilePath().child(buildNumber + "." + buildId + ".user.summary.report").readToString();
            //String output = Files.readString(Path.of(utils.getFilePath().child(buildNumber + "." + buildId + ".user.summary.report").getRemote()), StandardCharsets.UTF_8);

            int start = output.indexOf("<head>");
            int end = output.indexOf("</head>") + 7;
            output = output.substring(0, start) + output.substring(end, output.length());

            start = output.indexOf("<style>");
            end = output.indexOf("</style>") + 8;
            output = output.substring(0, start) + output.substring(end, output.length());

            start = output.indexOf("<script>");
            end = output.indexOf("</script>") + 9;
            output = output.substring(0, start) + output.substring(end, output.length());

            start = output.indexOf("<script>");
            end = output.indexOf("</script>") + 9;
            output = output.substring(0, start) + output.substring(end, output.length());

            output = output.replace("<html>", "");
            output = output.replace("</html>", "");
            output = output.replace("<body>", "");
            output = output.replace("</body>", "");

            FilePath outFile = utils.getFilePath().child(buildNumber + "." + buildId + ".summary.report");
            outFile.write(output, "UTF-8"); 


        } catch (IOException e){
            listener.getLogger().println("Failed to find the user provided summary report file: " + utils.getFilePath().child(buildNumber + "." + buildId + ".user.summary.report").getRemote());
            throw e;
        }
        

    }

    public void retrievReportFromServer(boolean isStreamingOn,Launcher launcher) throws Exception {

        int buildNumber = 20;
        String buildId = "20";
        String jobWorkingDir = "c://temp";
        String jobRootDir = "c://temp";

        if (!this.testMode) {
            buildNumber = vmgrRun.getRun().getNumber();
            buildId = vmgrRun.getRun().getId();
            jobWorkingDir = vmgrRun.getJobWorkingDir();
            jobRootDir = build.getRootDir().getAbsolutePath();
        }


        //In case user choose to bring the report manualy skip and return
        if (summaryReportParams.summaryMode.equals("selfmade")) {
            listener.getLogger().println("User choose to bring the report manually.  Skipping automatic retrieval...");

            listener.getLogger().println("Converting Users Report Summary into Jenkins format....");
            convertBYORToJenkinsType( buildNumber,  buildId, jobWorkingDir);
            listener.getLogger().println("Copying over user report summary into Jenkins job location at master....");
            utils.moveFromNodeToMaster(buildNumber + "." + buildId + ".summary.report");
            listener.getLogger().println("Report Summary was copied succesfully.");
            return;
        }

        HttpURLConnection conn = null;

        String apiURL = vAPIConnectionParam.vAPIUrl + "/rest/reports/generate-summary-report";
        if (isStreamingOn) {
            apiURL = vAPIConnectionParam.vAPIUrl + "/rest/reports/stream-summary-report";
        }

        
        BufferedReader br = null;
        try {
            conn = utils.getVAPIConnection(apiURL, vAPIConnectionParam.authRequired, vAPIConnectionParam.vAPIUser, vAPIConnectionParam.vAPIPassword, "POST", vAPIConnectionParam.dynamicUserId, buildId, buildNumber, jobRootDir, listener, vAPIConnectionParam.connTimeout, vAPIConnectionParam.readTimeout, vAPIConnectionParam.advConfig);
            OutputStream os = conn.getOutputStream();
            String postData = buildPostParamForSummaryReport(false);
            if (!this.testMode) {
                listener.getLogger().println("ReportManager is using the following POST data for getting the summary report:\n" + postData);
            } else {
                System.out.println(postData);
            }
            os.write(postData.getBytes(Charset.forName("UTF-8")));
            os.flush();
            
                  
            
            if (checkResponseCode(conn)) {
                if (isStreamingOn) {
                    InputStream inputStream = conn.getInputStream();
                    String fileOutput = buildNumber + "." + buildId + ".summary.report";
                    if (utils.getFilePath() == null){
                        //Pipeline always run on master
                        fileOutput = jobWorkingDir + File.separator + fileOutput;            
                    } else {
                        hudson.FilePath newFile = utils.getFilePath().child(fileOutput);    
                        fileOutput = newFile.getRemote();
                    }
                    listener.getLogger().println("Full Path of temporary remote report is: " + fileOutput);
                    
                    
                    try (OutputStream outputStream = utils.getRemoteDiskOutoutStream(fileOutput)) {
                        byte[] buffer = new byte[8192]; // Buffer size set to 8192 bytes
                        int bytesRead;

                        // Read bytes from the InputStream and write them to the FileOutputStream
                        while ((bytesRead = inputStream.read(buffer)) != -1) {
                            outputStream.write(buffer, 0, bytesRead);
                        }
                        outputStream.flush();
                        utils.moveFromNodeToMaster(buildNumber + "." + buildId + ".summary.report");
                        if (!this.testMode) {
                            listener.getLogger().println("Report Summary was created succesfully.");
                        }

                    } catch (IOException e) {
                        listener.getLogger().println("Failed to stream the report from the server.");
                        e.printStackTrace();
                    } finally {
                        // Close the InputStream if it was opened
                        if (inputStream != null) {
                            try {
                                inputStream.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                } else {
                    br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
                    StringBuffer sb = new StringBuffer();
                    String output;
                    while ((output = br.readLine()) != null) {
                        sb.append(output);
                    }
                    fetchFromRemoteURL(sb.toString());                   
                }
                
                
              

            } else {
                br = new BufferedReader(new InputStreamReader(conn.getErrorStream(), "UTF-8"));
                String output;
                String fileOutput = buildNumber + "." + buildId + ".summary.report";
                if (utils.getFilePath() == null){
                    //Pipeline always run on master
                    fileOutput = jobWorkingDir + File.separator + fileOutput;            
                }
                StringBuffer writer = new StringBuffer();
                writer.append("<div class=\"microAgentWaiting\"><div class=\"spinnerMicroAgentMessage\"><p><img src=\"/plugin/vmanager-plugin/img/support-icon.png\"></img></p><p>");
                writer.append("Failure to retrieve the report from the Verisium Manager server for this build.  Check your parameters.<br>Below you can find the exception that was thrown during the retrieval process:<br><br><strong>");
                while ((output = br.readLine()) != null) {
                    writer.append(output + "<br>");
                }

                if (conn.getResponseCode() == 500) {
                    //This is an error with the existance of the acctual API call.  Probably because the version is too old
                    if ("stream".equals(summaryReportParams.vManagerVersion)) {
                        writer.append("<br><br>");
                        writer.append("Hint: This error usually indicates that you choosed the wrong Verisium Manager version at the plugin configuration section for Verisium Manager Version.<br>");
                        writer.append("If that's the case, and if your Verisium Manager server version is below 19.09 - set Verisium Manager Version as \"html\" (if pipeline dsl is used), or \"Lower than 19.09\" for regular post configuration mode.<br>");
                    }
                }
                writer.append("</strong></p></div></div>");
                utils.saveFileOnDisk(fileOutput, writer.toString());
                utils.moveFromNodeToMaster(buildNumber + "." + buildId + ".summary.report");

            }
        } catch (Exception e) {
            if (this.testMode) {
                e.printStackTrace();
            } else {
                
                e.printStackTrace();
                if (!this.testMode) {
                    listener.getLogger().println("Failed to retrieve report from the Verisium Manager server.");

                    String fileOutput = buildNumber + "." + buildId + ".summary.report";
                    if (utils.getFilePath() == null){
                        //Pipeline always run on master
                        fileOutput = jobWorkingDir + File.separator + fileOutput;            
                    }
                    StringBuffer writer = new StringBuffer();
                    writer.append("<div class=\"microAgentWaiting\"><div class=\"spinnerMicroAgentMessage\"><p><img src=\"/plugin/vmanager-plugin/img/support-icon.png\"></img></p><p>");
                    writer.append("Failure to retrieve the report from the Verisium Manager server for this build.  Check your parameters.<br>Below you can find the exception that was thrown during the retrieval process:<br><br><strong>");
                    writer.append(e.getMessage());
                    writer.append("</strong></p></div></div>");
                    utils.saveFileOnDisk(fileOutput, writer.toString());
                    utils.moveFromNodeToMaster(buildNumber + "." + buildId + ".summary.report");
                }
            }
            throw e;

        } finally {
            if (conn != null){
                conn.disconnect();
            }
            
            if (br != null){
                br.close();
            }

        }
        
        
        

    }
    
    

    public void emailSummaryReport() throws Exception {

        //In case user choose to bring the report manualy skip and return
        if (!summaryReportParams.sendEmail) {
            return;
        }

        if (summaryReportParams.summaryMode.equals("selfmade")) {
            return;
        }

        HttpURLConnection conn = null;

        String apiURL = vAPIConnectionParam.vAPIUrl + "/rest/reports/generate-summary-report";

        int buildNumber = 20;
        String buildId = "20";
        String jobRootDir = "c://temp";

        if (!this.testMode) {
            buildNumber = vmgrRun.getRun().getNumber();
            buildId = vmgrRun.getRun().getId();  
            jobRootDir = build.getRootDir().getAbsolutePath();
        }
        BufferedReader br = null;
        try {
            conn = utils.getVAPIConnection(apiURL, vAPIConnectionParam.authRequired, vAPIConnectionParam.vAPIUser, vAPIConnectionParam.vAPIPassword, "POST", vAPIConnectionParam.dynamicUserId, buildId, buildNumber, jobRootDir, listener, vAPIConnectionParam.connTimeout, vAPIConnectionParam.readTimeout, vAPIConnectionParam.advConfig);

            OutputStream os = conn.getOutputStream();
            String postData = buildPostParamForSummaryReport(true);
            if (!this.testMode) {
                listener.getLogger().println("ReportManager is using the following POST data for sending the summary report email:\n" + postData);
            } else {
                System.out.println(postData);
            }

            os.write(postData.getBytes(Charset.forName("UTF-8")));
            os.flush();
            
            
            if (!checkResponseCode(conn)) {

                br = new BufferedReader(new InputStreamReader(conn.getErrorStream(), "UTF-8"));

                String output;
                StringBuffer sb = new StringBuffer();

                while ((output = br.readLine()) != null) {
                    sb.append(output);
                }
                if (!this.testMode) {
                    listener.getLogger().println("Failed to send report using the Verisium Manager server.  Exception is:\n" + sb.toString());
                }
            } else {
                if (!this.testMode) {
                    listener.getLogger().println("Report Summary email was sent succesfully.");
                }
            }
        } catch (Exception e) {
            if (this.testMode) {
                e.printStackTrace();
            } else {
                if (!this.testMode) {
                    listener.getLogger().println("Failed to send report using the Verisium Manager server.");
                }
            }
            throw e;

        } finally {
            if (conn != null){
                conn.disconnect();
            }
            if (br != null){
                br.close();
            }

        }

    }

    public String getReportFromWorkspace() {

        String fileInput = vmgrRun.getJobWorkingDir() + File.separator + vmgrRun.getRun().getNumber() + "." + vmgrRun.getRun().getId() + ".summary.report";
        String output = "<div class=\"microAgentWaiting\"><div class=\"spinnerMicroAgentMessage\"><p><img src=\"/plugin/vmanager-plugin/img/weblinks.png\"></img></p><p>Failed to find a report file for this build.<br>Please check that the following file exist:<br>" + fileInput + "</p></div></div>";
        try {
                                                                                             
            output = new String(Files.readAllBytes(Paths.get(fileInput)),Charset.forName("UTF-8"));
            

        } catch (IOException ex) {
            System.out.println("Verisium Manager Action - Can't find file for loading report: " + fileInput);
            return output;
        }

        return output;

    }

    private boolean checkResponseCode(HttpURLConnection conn) {
        try {
            if (conn.getResponseCode() != HttpURLConnection.HTTP_OK && conn.getResponseCode() != HttpURLConnection.HTTP_NO_CONTENT && conn.getResponseCode() != HttpURLConnection.HTTP_ACCEPTED
                    && conn.getResponseCode() != HttpURLConnection.HTTP_CREATED && conn.getResponseCode() != HttpURLConnection.HTTP_PARTIAL && conn.getResponseCode() != HttpURLConnection.HTTP_RESET
                    && conn.getResponseCode() != 406) {
                //System.out.println("Error - Got wrong response from /reports/stream-summary-report - " + conn.getResponseCode()  );
                if ("html".equals(summaryReportParams.vManagerVersion)) {
                    if (!this.testMode) {
                        listener.getLogger().println("Error - Got wrong response from /reports/generate-summary-report - " + conn.getResponseCode());
                    }
                } else {
                    if (!this.testMode) {
                        listener.getLogger().println("Error - Got wrong response from /reports/stream-summary-report - " + conn.getResponseCode());
                    }
                }
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

    public CloseableHttpClient fixUntrustCertificate() throws IOException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
        javax.net.ssl.SSLContext sslContext = SSLContexts.custom().loadTrustMaterial(new TrustSelfSignedStrategy()).build();
        SSLConnectionSocketFactory socketFactory = new SSLConnectionSocketFactory(sslContext, NoopHostnameVerifier.INSTANCE);
        Registry<ConnectionSocketFactory> reg = RegistryBuilder.<ConnectionSocketFactory>create().register("https", socketFactory).build();
        HttpClientConnectionManager cm = new PoolingHttpClientConnectionManager(reg);
        CloseableHttpClient httpClient = HttpClients.custom().setConnectionManager(cm).build();
        return httpClient;
    }

}



