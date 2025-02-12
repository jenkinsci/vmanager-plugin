package org.jenkinsci.plugins.vmanager;

import hudson.FilePath;
import hudson.Launcher;
import hudson.Launcher.ProcStarter;
import hudson.Proc;
import hudson.model.Run;
import hudson.model.TaskListener;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.*;
import javax.net.ssl.*;
import net.sf.json.JSONObject;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;

public class Utils {

    private String UserUsedForLogin = null;
    private String PasswordUsedForLogin = null;
    private FilePath filePath = null;
    private TaskListener jobListener = null;
    private Run build = null;

    public Utils(Run run, TaskListener listener) {
        if (run.getExecutor() != null) {
            filePath = run.getExecutor().getCurrentWorkspace();
        }

        if (filePath == null) {
            throw new IllegalArgumentException("Workspace is null. Cannot proceed.");
        }

        jobListener = listener;
        this.build = run;
    }

    public Utils(Run run, TaskListener listener, FilePath filePath) {
        this.filePath = filePath;
        jobListener = listener;
        this.build = run;
    }

    public Utils() {}

    public FilePath getFilePath() {
        return filePath;
    }

    public BufferedReader loadFileFromWorkSpace(
            String buildID,
            int buildNumber,
            String workPlacePath,
            String inputFile,
            TaskListener listener,
            boolean deleteInputFile,
            String fileTypeEndingName)
            throws Exception {

        BufferedReader reader = null;
        String fileName = null;
        boolean notInTestMode = true;
        if (listener == null) {
            notInTestMode = false;
        }

        try {

            if ("".equals(inputFile) || inputFile == null) {
                fileName = /*workPlacePath + File.separator + */ buildNumber + "." + buildID + "." + fileTypeEndingName;
                if (notInTestMode) {
                    listener.getLogger().print("Loading input file '" + fileName + "\n");
                }
                reader = readFileOnDisk(fileName);
            } else {
                // fileName = workPlacePath + File.separator + inputFile;
                fileName = inputFile;
                if (notInTestMode) {
                    listener.getLogger().print("Loading input file '" + fileName + "\n");
                }
                reader = readFileOnDisk(fileName);
            }
        } catch (Exception e) {

            if (notInTestMode) {
                listener.getLogger().print("Failed to open input file.  Failed to load file '" + fileName + "'\n");

            } else {

                System.out.println("Failed to open the input file .  Failed to load file '" + fileName + "'");
            }

            throw e;
        }
        return reader;
    }

    public String[] loadDataFromInputFiles(
            String buildID,
            int buildNumber,
            String workPlacePath,
            String inputFile,
            TaskListener listener,
            boolean deleteInputFile,
            String type,
            String fileEnding)
            throws Exception {
        String[] output = null;
        List<String> listOfNames = new LinkedList<String>();
        BufferedReader reader = null;
        String fileName = null;
        boolean notInTestMode = true;
        if (listener == null) {
            notInTestMode = false;
        }

        if (notInTestMode) {
            listener.getLogger().print("Looking for " + type + " input:\n");
        }

        // Set the right File name.
        if ("".equals(inputFile) || inputFile == null) {
            fileName = /*workPlacePath + File.separator + */ buildNumber + "." + buildID + "." + fileEnding;
        } else {
            fileName = inputFile;
        }

        try {

            reader = this.loadFileFromWorkSpace(
                    buildID, buildNumber, workPlacePath, inputFile, listener, deleteInputFile, fileEnding);
            String line = null;
            while ((line = reader.readLine()) != null) {
                listOfNames.add(line);
            }

        } catch (Exception e) {

            if (notInTestMode) {
                listener.getLogger()
                        .print("Failed to read input file for the " + type + " targets.  Failed to load file '"
                                + fileName + "'\n");
            } else {

                System.out.println("Failed to open the read file for the " + type + " targets.  Failed to load file '"
                        + fileName + "'");
            }

            throw e;
        } finally {
            if (reader != null) {
                reader.close();
            }
        }

        Iterator<String> iter = listOfNames.iterator();
        output = new String[listOfNames.size()];
        int i = 0;
        if (notInTestMode) {
            listener.getLogger().print("Found the following " + type + " files for Verisium Manager plugin:\n");
        }
        String theFileName = null;
        while (iter.hasNext()) {
            theFileName = new String(iter.next());
            output[i++] = theFileName;
            if (notInTestMode) {
                listener.getLogger().print(i + " '" + theFileName + "'\n");
            } else {

                System.out.println(i + " '" + theFileName + "'");
            }
        }

        if (deleteInputFile) {
            if (notInTestMode) {
                listener.getLogger().print("Job set to delete the input file.  Deleting " + fileName + "\n");
            }
            try {
                File fileToDelete = new File(fileName);
                if (!fileToDelete.renameTo(new File(fileToDelete + ".delete"))) {
                    throw new IOException("Failed to rename file: " + fileName);
                }
            } catch (Exception e) {
                if (notInTestMode) {
                    listener.getLogger()
                            .print("Failed to delete input file from workspace.  Failed to delete file '" + fileName
                                    + "'\n");

                } else {

                    System.out.println("Failed to delete the input file from the workspace.  Failed to delete file '"
                            + fileName + "'");
                }
                throw e;
            }
        }

        return output;
    }

    public String loadUserSyntaxForSummaryReport(
            String buildID,
            int buildNumber,
            String workPlacePath,
            String inputFile,
            TaskListener listener,
            boolean deleteInputFile)
            throws Exception {
        String output = null;
        StringBuffer jsonInput = new StringBuffer();
        BufferedReader reader = null;
        String fileName = null;
        boolean notInTestMode = true;
        if (listener == null) {
            notInTestMode = false;
        }

        // Set the right File name.
        if ("".equals(inputFile) || inputFile == null) {
            fileName = /*workPlacePath + File.separator +*/ buildNumber + "." + buildID + "." + "summary_report.input";
        } else {
            fileName = inputFile;
        }

        try {

            reader = this.loadFileFromWorkSpace(
                    buildID, buildNumber, workPlacePath, inputFile, listener, deleteInputFile, "summary_report.input");
            String line = null;
            while ((line = reader.readLine()) != null) {
                jsonInput.append(line);
            }

        } catch (Exception e) {

            if (notInTestMode) {
                listener.getLogger()
                        .print("Failed to read input file for the summary report.  Failed to load file '" + fileName
                                + "'\n");
            } else {

                System.out.println(
                        "Failed to read input file for the summary report.  Failed to load file '" + fileName + "'");
            }

            throw e;
        } finally {
            if (reader != null) {
                reader.close();
            }
        }

        if (deleteInputFile) {
            if (notInTestMode) {
                listener.getLogger().print("Job set to delete the input file.  Deleting " + fileName + "\n");
            }
            try {
                File fileToDelete = new File(fileName);
                if (!fileToDelete.renameTo(new File(fileToDelete + ".delete"))) {
                    throw new IOException("Failed to rename file: " + fileName);
                }
            } catch (Exception e) {
                if (notInTestMode) {
                    listener.getLogger()
                            .print("Failed to delete input file from workspace.  Failed to delete file '" + fileName
                                    + "'\n");

                } else {

                    System.out.println("Failed to delete the input file from the workspace.  Failed to delete file '"
                            + fileName + "'");
                }
                throw e;
            }
        }
        output = jsonInput.toString();
        return output;
    }

    public String[] loadFileCredentials(
            String buildID,
            int buildNumber,
            String workPlacePath,
            String credentialInputFile,
            TaskListener listener,
            boolean deleteInputFile)
            throws Exception {
        String[] output = null;
        List<String> listOfNames = new LinkedList<String>();
        BufferedReader reader = null;
        String fileName = null;
        boolean notInTestMode = true;
        if (listener == null) {
            notInTestMode = false;
        }

        // Set the right File name.
        if ("".equals(credentialInputFile) || credentialInputFile == null) {
            fileName = /*workPlacePath + File.separator + */ buildNumber + "." + buildID + "." + "credential.input";
        } else {
            fileName = credentialInputFile;
        }

        try {

            reader = this.loadFileFromWorkSpace(
                    buildID,
                    buildNumber,
                    workPlacePath,
                    credentialInputFile,
                    listener,
                    deleteInputFile,
                    "credential.input");
            String line = null;
            while ((line = reader.readLine()) != null) {
                listOfNames.add(line);
            }

        } catch (Exception e) {

            if (notInTestMode) {
                listener.getLogger()
                        .print("Failed to read input file for the credentials.  Failed to load file '" + fileName
                                + "'\n");
            } else {

                System.out.println(
                        "Failed to open the read file for the credentials.  Failed to load file '" + fileName + "'");
            }

            throw e;
        } finally {
            if (reader != null) {
                reader.close();
            }
        }

        Iterator<String> iter = listOfNames.iterator();
        output = new String[listOfNames.size()];
        int i = 0;

        String stringValue = null;
        while (iter.hasNext()) {
            stringValue = new String(iter.next());
            output[i++] = stringValue;
        }

        if (deleteInputFile) {
            if (notInTestMode) {
                listener.getLogger().print("Job set to delete the credential file.  Deleting " + fileName + "\n");
            }
            try {
                File fileToDelete = new File(fileName);
                if (!fileToDelete.delete()) {
                    throw new IOException("Failed to delete file: " + fileName);
                }
            } catch (Exception e) {
                if (notInTestMode) {
                    listener.getLogger()
                            .print("Failed to delete input file from workspace.  Failed to delete file '" + fileName
                                    + "'\n");

                } else {

                    System.out.println("Failed to delete the input file from the workspace.  Failed to delete file '"
                            + fileName + "'");
                }
                throw e;
            }
        }

        return output;
    }

    public String loadJSONEnvInput(
            String buildID, int buildNumber, String workPlacePath, String envInputFile, TaskListener listener)
            throws Exception {
        String output = null;
        StringBuffer listOfEnvs = new StringBuffer();
        BufferedReader reader = null;
        String fileName = null;
        boolean notInTestMode = true;
        if (listener == null) {
            notInTestMode = false;
        }

        // Set the right File name.
        if ("".equals(envInputFile) || envInputFile == null) {
            fileName = /*workPlacePath + File.separator +*/ buildNumber + "." + buildID + "." + "environment.input";
        } else {
            fileName = envInputFile;
        }

        try {

            reader = this.loadFileFromWorkSpace(
                    buildID, buildNumber, workPlacePath, fileName, listener, false, "environment.input");
            String line = null;
            while ((line = reader.readLine()) != null) {
                listOfEnvs.append(line);
            }

            output = listOfEnvs.toString();

            output = "\"environment\":{  " + output + "}";

        } catch (Exception e) {

            if (notInTestMode) {
                listener.getLogger()
                        .print("Failed to read input file for the environment varibles.  Failed to load file '"
                                + fileName + "'\n");
            } else {

                System.out.println("Failed to open the read file for the environment varibles.  Failed to load file '"
                        + fileName + "'");
            }

            throw e;
        } finally {
            if (reader != null) {
                reader.close();
            }
        }

        return output;
    }

    public static String getRegressionURLFromVAPIURL(String vAPIUrl) {
        String output;
        try {
            String strWorker = vAPIUrl.substring(vAPIUrl.indexOf("https://") + 8, vAPIUrl.length());
            // Look for host & port
            String hostAndPort = strWorker.substring(0, strWorker.indexOf("/"));

            // Look for project name
            strWorker = strWorker.substring(strWorker.indexOf("/") + 1, strWorker.length());
            String projectCode = strWorker.substring(0, strWorker.indexOf("/"));

            // Check if this is a multi project setup, and if is remove the site part
            if (projectCode.indexOf(",") > 0) {
                projectCode = projectCode.substring(0, projectCode.indexOf(','));
            }

            output = "https://" + hostAndPort + "/web/" + projectCode + "/regression/index.html";
        } catch (Exception e) {
            return "#";
        }

        return output;
    }

    public String loadJSONAttrValuesInput(
            String buildID, int buildNumber, String workPlacePath, String attrValuesFile, TaskListener listener)
            throws Exception {
        String output = null;
        StringBuffer listOfAttrValues = new StringBuffer();
        BufferedReader reader = null;
        String fileName = null;
        boolean notInTestMode = true;
        if (listener == null) {
            notInTestMode = false;
        }

        // Set the right File name.
        if ("".equals(attrValuesFile) || attrValuesFile == null) {
            fileName = /* workPlacePath + File.separator +*/ buildNumber + "." + buildID + "." + "attr.values.input";
        } else {
            fileName = attrValuesFile;
        }

        try {

            reader = this.loadFileFromWorkSpace(
                    buildID, buildNumber, workPlacePath, fileName, listener, false, "attr.values.input");
            String line = null;
            boolean foundOneAttr = false;
            while ((line = reader.readLine()) != null) {
                String tmpLineResult = "";
                StringTokenizer tokenizer = new StringTokenizer(line, ",");
                tmpLineResult =
                        tmpLineResult + "{\"name\":\"" + tokenizer.nextToken().trim() + "\",";
                tmpLineResult =
                        tmpLineResult + "\"value\":\"" + tokenizer.nextToken().trim() + "\",";
                tmpLineResult =
                        tmpLineResult + "\"type\":\"" + tokenizer.nextToken().trim() + "\"},";
                listOfAttrValues.append(tmpLineResult);
                foundOneAttr = true;
            }

            output = listOfAttrValues.toString();
            if (foundOneAttr) {
                // Remove the last comma
                output = output.substring(0, output.length() - 1);
            }

            output = "\"attributes\":[" + output + "]";

        } catch (Exception e) {

            if (notInTestMode) {
                listener.getLogger()
                        .print("Failed to read input file for the attribute values.  Failed to load file '" + fileName
                                + "'\n " + e.getMessage());
            } else {

                System.out.println("Failed to open the read file for the attribute values.  Failed to load file '"
                        + fileName + "'");
            }

            throw e;
        } finally {
            if (reader != null) {
                reader.close();
            }
        }

        return output;
    }

    public String loadJSONDefineInput(
            String buildID, int buildNumber, String workPlacePath, String envInputFile, TaskListener listener)
            throws Exception {
        String output = null;
        StringBuffer listOfDefineValues = new StringBuffer();
        BufferedReader reader = null;
        String fileName = null;
        boolean notInTestMode = true;
        if (listener == null) {
            notInTestMode = false;
        }

        // Set the right File name.
        if ("".equals(envInputFile) || envInputFile == null) {
            fileName = buildNumber + "." + buildID + "." + "define.input";
        } else {
            fileName = envInputFile;
        }

        try {

            reader = this.loadFileFromWorkSpace(
                    buildID, buildNumber, workPlacePath, fileName, listener, false, "define.input");
            String line = null;
            boolean foundOneAttr = false;
            while ((line = reader.readLine()) != null) {
                String tmpLineResult = "";
                StringTokenizer tokenizer = new StringTokenizer(line, ",");
                tmpLineResult =
                        tmpLineResult + "{\"name\":\"" + tokenizer.nextToken().trim() + "\",";
                tmpLineResult =
                        tmpLineResult + "\"value\":\"=" + tokenizer.nextToken().trim() + "\"},";
                listOfDefineValues.append(tmpLineResult);
                foundOneAttr = true;
            }

            output = listOfDefineValues.toString();
            if (foundOneAttr) {
                // Remove the last comma
                output = output.substring(0, output.length() - 1);
            }

            output = "\"params\":[" + output + "]";

        } catch (Exception e) {

            if (notInTestMode) {
                listener.getLogger()
                        .print("Failed to read input file for the define values.  Failed to load file '" + fileName
                                + "'\n " + e.getMessage());
            } else {

                System.out.println(
                        "Failed to open the read file for the define values.  Failed to load file '" + fileName + "'");
            }

            throw e;
        } finally {
            if (reader != null) {
                reader.close();
            }
        }

        return output;
    }

    public String loadJSONAttrValuesFromTextArea(
            String buildID, int buildNumber, String workPlacePath, TaskListener listener, String textarea)
            throws Exception {
        String output = null;
        StringBuffer listOfAttrValues = new StringBuffer();

        boolean notInTestMode = true;
        if (listener == null) {
            notInTestMode = false;
        }

        try {

            String line = null;
            boolean foundOneAttr = false;
            String[] lines = StringUtils.split(textarea, System.lineSeparator());
            for (int i = 0; i < lines.length; i++) {

                String tmpLineResult = "";
                StringTokenizer tokenizer = new StringTokenizer(lines[i], ",");
                tmpLineResult =
                        tmpLineResult + "{\"name\":\"" + tokenizer.nextToken().trim() + "\",";
                tmpLineResult =
                        tmpLineResult + "\"value\":\"" + tokenizer.nextToken().trim() + "\",";
                tmpLineResult =
                        tmpLineResult + "\"type\":\"" + tokenizer.nextToken().trim() + "\"},";
                listOfAttrValues.append(tmpLineResult);
                foundOneAttr = true;
            }

            output = listOfAttrValues.toString();
            if (foundOneAttr) {
                // Remove the last comma
                output = output.substring(0, output.length() - 1);
            }

            output = "\"attributes\":[" + output + "]";

        } catch (Exception e) {

            if (notInTestMode) {
                listener.getLogger().print("Failed to parse attributes for vsif.\n " + e.getMessage());
            } else {

                System.out.println("Failed to parse attributes for vsif.\n ");
            }

            throw e;
        }

        return output;
    }

    public String loadJSONDefineValuesFromTextArea(
            String buildID, int buildNumber, String workPlacePath, TaskListener listener, String textarea)
            throws Exception {
        String output = null;
        StringBuffer listOfAttrValues = new StringBuffer();

        boolean notInTestMode = true;
        if (listener == null) {
            notInTestMode = false;
        }

        try {

            String line = null;
            boolean foundOneAttr = false;
            String[] lines = StringUtils.split(textarea, System.lineSeparator());
            for (int i = 0; i < lines.length; i++) {

                String tmpLineResult = "";
                StringTokenizer tokenizer = new StringTokenizer(lines[i], ",");
                tmpLineResult =
                        tmpLineResult + "{\"name\":\"" + tokenizer.nextToken().trim() + "\",";
                tmpLineResult =
                        tmpLineResult + "\"value\":\"=" + tokenizer.nextToken().trim() + "\"},";
                listOfAttrValues.append(tmpLineResult);
                foundOneAttr = true;
            }

            output = listOfAttrValues.toString();
            if (foundOneAttr) {
                // Remove the last comma
                output = output.substring(0, output.length() - 1);
            }

            output = "\"params\":[" + output + "]";

        } catch (Exception e) {

            if (notInTestMode) {
                listener.getLogger().print("Failed to parse define for vsif.\n " + e.getMessage());
            } else {

                System.out.println("Failed to parse define for vsif.\n ");
            }

            throw e;
        }

        return output;
    }

    public String loadJSONFromFile(
            String buildID,
            int buildNumber,
            String workPlacePath,
            String vInputFile,
            TaskListener listener,
            boolean deleteInputFile)
            throws Exception {
        String output = null;
        StringBuffer listOfNames = new StringBuffer();
        BufferedReader reader = null;
        String fileName = null;
        boolean notInTestMode = true;
        if (listener == null) {
            notInTestMode = false;
        }

        // Set the right File name.
        if ("".equals(vInputFile) || vInputFile == null) {
            fileName = /* workPlacePath + File.separator + */ buildNumber + "." + buildID + "." + "vapi.input";
        } else {
            fileName = vInputFile;
        }

        try {

            reader = this.loadFileFromWorkSpace(
                    buildID, buildNumber, workPlacePath, vInputFile, listener, deleteInputFile, "vapi.input");
            String line = null;
            while ((line = reader.readLine()) != null) {
                listOfNames.append(line);
            }

        } catch (Exception e) {

            if (notInTestMode) {
                listener.getLogger()
                        .print("Failed to read json input file for the vAPI input.  Failed to load file '" + fileName
                                + "'\n");
            } else {

                System.out.println(
                        "Failed to open the read file for the vAPI input.  Failed to load file '" + fileName + "'");
            }

            throw e;
        } finally {
            if (reader != null) {
                reader.close();
            }
        }

        output = listOfNames.toString();

        if (notInTestMode) {
            listener.getLogger().print("Input jSON for vAPI is:\n");
            listener.getLogger().print(output + "\n");
        }

        if (deleteInputFile) {
            if (notInTestMode) {
                listener.getLogger().print("Job set to delete the input file.  Deleting " + fileName + "\n");
            }
            try {
                File fileToDelete = new File(fileName);
                if (!fileToDelete.renameTo(new File(fileToDelete + ".delete"))) {
                    throw new IOException("Failed to rename file: " + fileName);
                }
            } catch (Exception e) {
                if (notInTestMode) {
                    listener.getLogger()
                            .print("Failed to delete input file from workspace.  Failed to delete file '" + fileName
                                    + "'\n");

                } else {

                    System.out.println("Failed to delete the input file from the workspace.  Failed to delete file '"
                            + fileName + "'");
                }
                throw e;
            }
        }

        return output;
    }

    public String checkVAPIConnection(String url, boolean requireAuth, String user, String password) throws Exception {
        String textOut = null;
        OutputStream os = null;
        BufferedReader br = null;
        try {
            System.out.println("Trying to connect with Verisium Manager vAPI " + url);
            String input = "{}";
            String apiURL = url + "/rest/sessions/count";
            HttpURLConnection conn = getVAPIConnection(
                    apiURL, requireAuth, user, password, "POST", false, "", 0, null, null, 0, 0, false);
            try {
                os = conn.getOutputStream();
                os.write(input.getBytes(StandardCharsets.UTF_8));
                os.flush();
            } catch (java.net.UnknownHostException e) {
                throw new Exception("Failed to connect to host " + e.getMessage() + ". Host is unknown.");
            }

            if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
                String reason = "";
                if (conn.getResponseCode() == 503) {
                    reason = "vAPI process failed to connect to remote Verisium Manager server.";
                }
                if (conn.getResponseCode() == 401) {
                    reason = "Authentication Error.";
                }
                if (conn.getResponseCode() == 412) {
                    reason = "vAPI requires Verisium Manager 'Integration Server' license.";
                }
                String errorMessage = processErrorFromRespone(conn, null, false);
                return errorMessage;
            }

            br = new BufferedReader(new InputStreamReader((conn.getInputStream()), StandardCharsets.UTF_8));
            StringBuilder result = new StringBuilder();
            String output;
            while ((output = br.readLine()) != null) {
                result.append(output);
            }
            conn.disconnect();
            JSONObject tmp = JSONObject.fromObject(result.toString());
            textOut = "The current number of sessions held on this Verisium Manager server are: "
                    + tmp.getString("count");
        } catch (Exception e) {
            String errorMessage = "Failed: HTTP error: " + e.getMessage();
            if (e.getMessage().indexOf("Unexpected end of file from server") > -1) {
                errorMessage = errorMessage
                        + " (from Incisive 14.2 onward the connection is secured. Verify your url is https://)";
            }
            System.out.println(errorMessage);
            textOut = errorMessage;
        } finally {
            if (os != null) {
                os.close();
            }
            if (br != null) {
                br.close();
            }
        }
        return textOut;
    }

    public String checkExtraStaticAttr(String url, boolean requireAuth, String user, String password, String listOfAttr)
            throws Exception {
        String textOut = null;
        BufferedReader br = null;
        try {
            List<String> items = Arrays.asList(listOfAttr.split("\\s*,\\s*"));
            String apiURL = url + "/rest/$schema/response?action=list&component=runs&extended=true";
            HttpURLConnection conn = getVAPIConnection(
                    apiURL, requireAuth, user, password, "GET", false, "", 0, null, null, 0, 0, false);

            if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
                String reason = "";
                if (conn.getResponseCode() == 503) {
                    reason = "vAPI process failed to connect to remote Verisium Manager server.";
                }
                if (conn.getResponseCode() == 401) {
                    reason = "Authentication Error";
                }
                String errorMessage = processErrorFromRespone(conn, null, false);
                return errorMessage;
            }

            br = new BufferedReader(new InputStreamReader((conn.getInputStream()), StandardCharsets.UTF_8));
            StringBuilder result = new StringBuilder();
            String output;
            while ((output = br.readLine()) != null) {
                result.append(output);
            }

            conn.disconnect();
            JSONObject tmp = JSONObject.fromObject(result.toString());
            JSONObject responseItems = JSONObject.fromObject(tmp.getString("items"));
            JSONObject properties = JSONObject.fromObject(responseItems.getString("properties"));

            Iterator<String> iter = items.iterator();
            String attr = null;
            JSONObject attrObject = null;
            String attrOutputString = "";
            int i = 1;
            boolean isSuccess = true;
            while (iter.hasNext()) {
                attr = iter.next();
                if (properties.has(attr)) {
                    attrObject = JSONObject.fromObject(properties.getString(attr));
                    String attrTitle = attrObject.getString("title");
                    attrOutputString = attrOutputString + "(" + i + ") " + attr + " (" + attrTitle + ")\n";
                    i++;
                } else {
                    isSuccess = false;
                    textOut = "Failed : '" + attr
                            + "' doesn't exist for runs entities in the Verisium Manager server you are pointing at.\n";
                    break;
                }
            }

            if (isSuccess) {
                textOut = " All attributes were found on server:\n" + attrOutputString;
            }

        } catch (Exception e) {
            e.printStackTrace();
            String errorMessage = "Failed : HTTP error: " + e.getMessage();
            if (e.getMessage().indexOf("Unexpected end of file from server") > -1) {
                errorMessage = errorMessage
                        + " (from Incisive 14.2 onward the connection is secured. Verify your url is https://)";
            }
            System.out.println(errorMessage);
            textOut = errorMessage;
        } finally {
            if (br != null) {
                br.close();
            }
        }

        return textOut;
    }

    public HttpURLConnection getVAPIConnection(
            String apiUrl,
            boolean requireAuth,
            String user,
            String password,
            String requestMethod,
            boolean dynamicUserId,
            String buildID,
            int buildNumber,
            String workPlacePath,
            TaskListener listener,
            int connConnTimeOut,
            int connReadTimeout,
            boolean advConfig)
            throws Exception {

        boolean notInTestMode = true;
        if (listener == null) {
            notInTestMode = false;
        }

        // In case this is an SSL connections
        URL url = new URL(apiUrl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        if (apiUrl.indexOf("https://") > -1) {
            configureAllowAll((HttpsURLConnection) conn);
        }

        conn.setDoOutput(true);
        conn.setRequestMethod(requestMethod);
        if ("PUT".equals(requestMethod) || "POST".equals(requestMethod)) {
            conn.setRequestProperty("Content-Type", "application/json");
        }

        // set the connection timeouts to one minute and the read timeout to 30 minutes by default
        if (advConfig) {
            conn.setConnectTimeout(connConnTimeOut * 60 * 1000);
        } else {
            conn.setConnectTimeout(60000);
        }

        if (advConfig) {
            conn.setReadTimeout(connReadTimeout * 60 * 1000);
        } else {
            conn.setReadTimeout(1800000);
        }

        if (requireAuth) {
            // ----------------------------------------------------------------------------------------
            // Authentication
            // ----------------------------------------------------------------------------------------
            if (dynamicUserId) {
                BufferedReader reader = null;
                try {

                    reader = this.loadFileFromWorkSpace(
                            buildID, buildNumber, workPlacePath, null, listener, false, "user.input");
                    String line = null;
                    while ((line = reader.readLine()) != null) {
                        user = line;
                        break;
                    }

                } catch (Exception e) {

                    if (notInTestMode) {
                        listener.getLogger().print("Failed to read input file for the dynamic users. \n");
                    } else {

                        System.out.println("Failed to read input file for the dynamic users. \n");
                    }

                    throw e;
                } finally {
                    if (reader != null) {
                        reader.close();
                    }
                }
            }

            String authString = user + ":" + password;
            UserUsedForLogin = user;
            PasswordUsedForLogin = password;
            byte[] authEncBytes = Base64.encodeBase64(authString.getBytes(StandardCharsets.UTF_8));
            String authStringEnc = new String(authEncBytes, StandardCharsets.UTF_8);
            conn.setRequestProperty("Authorization", "Basic " + authStringEnc);
            // ----------------------------------------------------------------------------------------
        }

        return conn;
    }

    public String executeVSIFLaunch(
            String[] vsifs,
            String url,
            boolean requireAuth,
            String user,
            String password,
            TaskListener listener,
            boolean dynamicUserId,
            String buildID,
            int buildNumber,
            String workPlacePath,
            int connConnTimeOut,
            int connReadTimeout,
            boolean advConfig,
            String jsonEnvInput,
            boolean useUserOnFarm,
            String userFarmType,
            String[] farmUserPassword,
            StepHolder stepHolder,
            String envSourceInputFile,
            String workingJobDir,
            VMGRBuildArchiver vMGRBuildArchiver,
            boolean userPrivateSSHKey,
            String jsonAttrValuesInput,
            String executionType,
            String[] sessionNames,
            String envSourceInputFileType,
            Launcher launcher,
            String jsonDefineInput)
            throws Exception {

        boolean notInTestMode = true;
        if (listener == null) {
            notInTestMode = false;
        }

        String apiURL = url + "/rest/sessions/launch";

        List<String> listOfSessions = new ArrayList<>();

        if ("batch".equals(executionType)) {
            // Treat sessions that were launched by the user's batch
            SessionNameIdHolder sessionNameIdHolder = new SessionNameIdHolder();
            listOfSessions = sessionNameIdHolder.getSessionNames(
                    sessionNames,
                    url,
                    requireAuth,
                    user,
                    password,
                    listener,
                    dynamicUserId,
                    buildID,
                    buildNumber,
                    workPlacePath,
                    connConnTimeOut,
                    connReadTimeout,
                    advConfig,
                    this);
            if (listOfSessions.size() == 0) {
                listener.getLogger()
                        .print("Couldn't find any data for the given project and the supplied session names.\n");
                return "Please check the input file that lists the session names and make sure you have the full session names as listed in Verisium Manager.";
            }

        } else {

            for (String vsif : vsifs) {

                if (notInTestMode) {
                    listener.getLogger().print("Verisium Manager vAPI - Trying to launch vsif file: '" + vsif + "'\n");
                }
                String input = "{\"vsif\":\"" + vsif + "\"";
                if (jsonEnvInput != null) {
                    input = input + "," + jsonEnvInput;
                }
                if (jsonAttrValuesInput != null) {
                    input = input + "," + jsonAttrValuesInput;
                }
                if (jsonDefineInput != null) {
                    input = input + "," + jsonDefineInput;
                }
                if (useUserOnFarm) {
                    String userFarm = null;
                    String passwordFarm = null;
                    if ("static".equals(userFarmType)) {
                        userFarm = user;
                        passwordFarm = password;
                        if (dynamicUserId) {
                            BufferedReader reader = null;
                            try {

                                reader = this.loadFileFromWorkSpace(
                                        buildID, buildNumber, workPlacePath, null, listener, false, "user.input");
                                String line;
                                while ((line = reader.readLine()) != null) {
                                    userFarm = line;
                                    break;
                                }

                            } catch (Exception e) {

                                if (notInTestMode) {
                                    listener.getLogger().print("Failed to read input file for the dynamic users. \n");
                                } else {

                                    System.out.println("Failed to read input file for the dynamic users. \n");
                                }

                                throw e;
                            } finally {
                                if (reader != null) {
                                    reader.close();
                                }
                            }
                        }
                    } else if (!userPrivateSSHKey) {
                        userFarm = farmUserPassword[0];
                        passwordFarm = farmUserPassword[1];
                    }

                    if (!userPrivateSSHKey) {
                        input = input + ",\"credentials\":{\"username\":\"" + userFarm + "\",\"password\":\""
                                + passwordFarm + "\"}";
                    } else {
                        input = input + ",\"credentials\":{\"connectType\":\"PUBLIC_KEY\"}";
                    }

                    if ((envSourceInputFile != null) && (!"".equals(envSourceInputFile.trim()))) {
                        String scriptShell = envSourceInputFileType;
                        input = input + ",\"preliminaryStage\":{\"sourceFilePath\":\"" + envSourceInputFile
                                + "\",\"shell\":\"" + scriptShell + "\"}";
                    }
                }
                input = input + "}";

                HttpURLConnection conn = getVAPIConnection(
                        apiURL,
                        requireAuth,
                        user,
                        password,
                        "POST",
                        dynamicUserId,
                        buildID,
                        buildNumber,
                        workPlacePath,
                        listener,
                        connConnTimeOut,
                        connReadTimeout,
                        advConfig);
                OutputStream os = null;
                BufferedReader br = null;
                try {
                    os = conn.getOutputStream();
                    os.write(input.getBytes(StandardCharsets.UTF_8));
                    os.flush();

                    if (conn.getResponseCode() != HttpURLConnection.HTTP_OK
                            && conn.getResponseCode() != HttpURLConnection.HTTP_NO_CONTENT
                            && conn.getResponseCode() != HttpURLConnection.HTTP_ACCEPTED
                            && conn.getResponseCode() != HttpURLConnection.HTTP_CREATED
                            && conn.getResponseCode() != HttpURLConnection.HTTP_PARTIAL
                            && conn.getResponseCode() != HttpURLConnection.HTTP_RESET) {
                        String reason = "";
                        if (conn.getResponseCode() == 503) {
                            reason = "vAPI process failed to connect to remote Verisium Manager server.";
                        }
                        if (conn.getResponseCode() == 401) {
                            reason = "Authentication Error";
                        }
                        if (conn.getResponseCode() == 412) {
                            reason = "vAPI requires Verisium Manager 'Integration Server' license.";
                        }
                        if (conn.getResponseCode() == 406) {
                            reason = "VSIF file '" + vsif
                                    + "' was not found on file system, or is not accessed by the vAPI process.\n";
                        }

                        String errorMessage = processErrorFromRespone(conn, listener, notInTestMode);

                        return errorMessage;
                    }

                    br = new BufferedReader(new InputStreamReader((conn.getInputStream()), StandardCharsets.UTF_8));

                    StringBuilder result = new StringBuilder();
                    String output;
                    while ((output = br.readLine()) != null) {
                        result.append(output);
                    }

                    JSONObject tmp = JSONObject.fromObject(result.toString());

                    String textOut = "Session Launch Success: Session ID: " + tmp.getString("value") + "\n";
                    listOfSessions.add(tmp.getString("value"));

                    if (notInTestMode) {
                        listener.getLogger().print(textOut);
                    } else {

                        System.out.println(textOut);
                    }
                } finally {
                    if (os != null) {
                        os.close();
                    }
                    if (br != null) {
                        br.close();
                    }
                }
            }
        }

        if (vMGRBuildArchiver != null) {
            if (vMGRBuildArchiver.isVMGRBuildArchive()) {
                vMGRBuildArchiver.markBuildForArchive(
                        listOfSessions,
                        apiURL,
                        requireAuth,
                        UserUsedForLogin,
                        PasswordUsedForLogin,
                        workingJobDir,
                        listener,
                        launcher,
                        this);
            }
        }

        // Write the sessions id into the workspace for further use
        // Flush the output into workspace
        String fileOutput = buildNumber + "." + buildID + ".session_launch.output";

        if (filePath == null) {
            // Pipeline always run on master
            fileOutput = workPlacePath + File.separator + fileOutput;
        }

        StringBuilder writer = new StringBuilder();
        for (String session : listOfSessions) {
            writer.append(session).append("\n");
        }

        this.saveFileOnDisk(fileOutput, writer.toString());

        if (stepHolder != null) {
            waitTillSessionEnds(
                    url,
                    requireAuth,
                    user,
                    password,
                    listener,
                    dynamicUserId,
                    buildID,
                    buildNumber,
                    workPlacePath,
                    connConnTimeOut,
                    connReadTimeout,
                    advConfig,
                    stepHolder,
                    listOfSessions,
                    notInTestMode,
                    workingJobDir,
                    launcher);
        }

        return "success";
    }

    public String executeAPI(
            String jSON,
            String apiUrl,
            String url,
            boolean requireAuth,
            String user,
            String password,
            String requestMethod,
            TaskListener listener,
            boolean dynamicUserId,
            String buildID,
            int buildNumber,
            String workPlacePath,
            int connConnTimeOut,
            int connReadTimeout,
            boolean advConfig)
            throws Exception {

        OutputStream os = null;
        BufferedReader br = null;
        try {
            boolean notInTestMode = true;
            if (listener == null) {
                notInTestMode = false;
            }

            String apiURL = url + "/rest" + apiUrl;

            if (notInTestMode) {
                listener.getLogger().print("Verisium Manager vAPI - Trying to call vAPI '" + "/rest" + apiUrl + "'\n");
            }
            String input = jSON;
            HttpURLConnection conn = getVAPIConnection(
                    apiURL,
                    requireAuth,
                    user,
                    password,
                    requestMethod,
                    dynamicUserId,
                    buildID,
                    buildNumber,
                    workPlacePath,
                    listener,
                    connConnTimeOut,
                    connReadTimeout,
                    advConfig);

            if ("PUT".equals(requestMethod) || "POST".equals(requestMethod)) {
                os = conn.getOutputStream();
                os.write(input.getBytes(StandardCharsets.UTF_8));
                os.flush();
            }

            if (conn.getResponseCode() != HttpURLConnection.HTTP_OK
                    && conn.getResponseCode() != HttpURLConnection.HTTP_NO_CONTENT
                    && conn.getResponseCode() != HttpURLConnection.HTTP_ACCEPTED
                    && conn.getResponseCode() != HttpURLConnection.HTTP_CREATED
                    && conn.getResponseCode() != HttpURLConnection.HTTP_PARTIAL
                    && conn.getResponseCode() != HttpURLConnection.HTTP_RESET) {
                String reason = "";
                if (conn.getResponseCode() == 503) {
                    reason = "vAPI process failed to connect to remote Verisium Manager server.";
                }
                if (conn.getResponseCode() == 401) {
                    reason = "Authentication Error";
                }
                if (conn.getResponseCode() == 415) {
                    reason =
                            "The server is refusing to service the request because the entity of the request is in a format not supported by the requested resource for the requested method.  Check if you selected the right request method (GET/POST/DELETE/PUT).";
                }
                if (conn.getResponseCode() == 405) {
                    reason =
                            "The method specified in the Request-Line is not allowed for the resource identified by the Request-URI. The response MUST include an Allow header containing a list of valid methods for the requested resource.  Check if you selected the right request method (GET/POST/DELETE/PUT).";
                }
                if (conn.getResponseCode() == 412) {
                    reason = "vAPI requires Verisium Manager 'Integration Server' license.";
                }
                String errorMessage = processErrorFromRespone(conn, listener, notInTestMode);
                if (notInTestMode) {
                    listener.getLogger().print(errorMessage);
                } else {
                    System.out.println(errorMessage);
                }

                return errorMessage;
            }

            br = new BufferedReader(new InputStreamReader((conn.getInputStream()), StandardCharsets.UTF_8));

            StringBuilder result = new StringBuilder();
            String output;
            while ((output = br.readLine()) != null) {
                result.append(output);
            }

            conn.disconnect();

            // Flush the output into workspace
            String fileOutput = buildNumber + "." + buildID + ".vapi.output";
            if (filePath == null) {
                // Pipeline always run on master
                fileOutput = workPlacePath + File.separator + fileOutput;
            }
            this.saveFileOnDisk(fileOutput, result.toString());

            String textOut = "API Call Success: Output was saved into: " + fileOutput + "\n";

            if (notInTestMode) {
                listener.getLogger().print(textOut);
            } else {
                System.out.println(textOut);
            }

            return "success";

        } catch (Exception e) {
            listener.getLogger().print("Filed: Error: " + e.getMessage());
            return e.getMessage();
        } finally {
            if (os != null) {
                os.close();
            }
            if (br != null) {
                br.close();
            }
        }
    }

    public void waitTillSessionEnds(
            String url,
            boolean requireAuth,
            String user,
            String password,
            TaskListener listener,
            boolean dynamicUserId,
            String buildID,
            int buildNumber,
            String workPlacePath,
            int connConnTimeOut,
            int connReadTimeout,
            boolean advConfig,
            StepHolder stepHolder,
            List listOfSessions,
            boolean notInTestMode,
            String workingJobDir,
            Launcher launcher)
            throws Exception {

        LaunchHolder launchHolder = new LaunchHolder(stepHolder, listOfSessions, this);
        launchHolder.performWaiting(
                url,
                requireAuth,
                user,
                password,
                listener,
                dynamicUserId,
                buildID,
                buildNumber,
                workPlacePath,
                connConnTimeOut,
                connReadTimeout,
                advConfig,
                notInTestMode,
                workingJobDir,
                launcher);
    }

    public static void configureAllowAll(HttpsURLConnection connection) {
        connection.setHostnameVerifier(new HostnameVerifier() {
            @Override
            public boolean verify(String s, SSLSession sslSession) {
                return true;
            }
        });

        try {
            connection.setSSLSocketFactory(getSocketFactory());
        } catch (Exception e) {
            throw new RuntimeException("XTrust Failed to set SSL Socket factory", e);
        }
    }

    private static SSLSocketFactory getSocketFactory() throws NoSuchAlgorithmException, KeyManagementException {
        SSLContext sslContext = SSLContext.getInstance("TLS");
        TrustManager tm = new X509TrustManager() {
            public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {}

            public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {}

            public X509Certificate[] getAcceptedIssuers() {
                return null;
            }
        };
        sslContext.init(null, new TrustManager[] {tm}, null);
        return sslContext.getSocketFactory();
    }

    @SuppressWarnings("finally")
    public String processErrorFromRespone(HttpURLConnection conn, TaskListener listener, boolean notInTestMode) {
        String errorMessage = "";
        StringBuilder resultFromError = null;
        int responseCode = 0;
        BufferedReader br = null;
        try {
            resultFromError = new StringBuilder(conn.getResponseMessage());
            responseCode = conn.getResponseCode();
            br = new BufferedReader(new InputStreamReader((conn.getErrorStream()), StandardCharsets.UTF_8));

            String output;
            while ((output = br.readLine()) != null) {
                resultFromError.append(output);
            }
        } catch (Exception e) {

        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            errorMessage = "Failed : HTTP error code : " + responseCode + " (" + resultFromError + ")\n";
            if (notInTestMode) {
                listener.getLogger().print(errorMessage);
            } else {
                System.out.println(errorMessage);
            }
            return errorMessage;
        }
    }

    public void saveFileOnDisk(String fileOnDiskPath, String output) throws IOException {
        if (filePath != null) {
            try {
                hudson.FilePath newFile = filePath.child(fileOnDiskPath);
                newFile.write(output, StandardCharsets.UTF_8.name());
                // dir.createTextTempFile("hudson", ".sh", command);
            } catch (Exception e) {
                e.printStackTrace();
                standardWriteToDisk(fileOnDiskPath, output);
            }
        } else {
            standardWriteToDisk(fileOnDiskPath, output);
        }
    }

    public BufferedReader readFileOnDisk(String fileOnDiskPath) throws FileNotFoundException {

        if (filePath != null) {
            try {
                hudson.FilePath newFile = filePath.child(fileOnDiskPath);
                return new BufferedReader(new InputStreamReader(newFile.read(), StandardCharsets.UTF_8));

            } catch (Exception e) {
                e.printStackTrace();
                return standardReadFromDisk(fileOnDiskPath);
            }
        } else {
            return standardReadFromDisk(fileOnDiskPath);
        }
    }

    public void standardWriteToDisk(String fileOnDiskPath, String output) throws IOException {
        try (FileWriter writer = new FileWriter(fileOnDiskPath, StandardCharsets.UTF_8)) {
            writer.append(output);
            writer.flush();
        }
    }

    public BufferedReader standardReadFromDisk(String fileOnDiskPath) throws FileNotFoundException {
        try {
            if (filePath != null) {
                hudson.FilePath newFile = filePath.child(fileOnDiskPath);
                return new BufferedReader(new InputStreamReader(newFile.read(), StandardCharsets.UTF_8));
            } else {
                return new BufferedReader(new FileReader(fileOnDiskPath, StandardCharsets.UTF_8));
            }
        } catch (IOException e) {
            e.printStackTrace();
            throw new FileNotFoundException("File not found: " + fileOnDiskPath);
        } catch (InterruptedException e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        }
    }

    public void moveFromNodeToMaster(String fileName, Launcher launcher, String content)
            throws IOException, InterruptedException {

        // Get master FilePath
        FilePath masterDirectory = new FilePath(build.getRootDir()).child(fileName);
        // this.jobListener.getLogger().print("About to copy " + fileName + " from Slave location to Master location:
        // \n");
        // this.jobListener.getLogger().print("From Slave location: " + this.filePath.getRemote() + "\n");
        // this.jobListener.getLogger().print("To Master location: " + buildDir + "\n\n");

        this.filePath.child(fileName).copyTo(masterDirectory);
    }

    public void batchExecManager(
            TaskListener listener,
            String executionScript,
            String executionShellLocation,
            String executionVsifFile,
            String buildId,
            int buildNumber,
            Launcher launcher)
            throws IOException {
        String sessionNameToMonitor = null;
        String[] command = {executionShellLocation, executionScript, executionVsifFile};
        jobListener.getLogger().print("\nTrying to launch a session using batch on this agent workspace:\n");
        jobListener.getLogger().print("Select script for this execution is " + executionScript + "\n");
        jobListener.getLogger().print("Select shell type for this execution is " + executionShellLocation + "\n");
        jobListener.getLogger().print("Select vsif for this execution is " + executionVsifFile + "\n");
        boolean foundGoodVSIF = false;

        Proc proc = null;
        BufferedReader in = null;
        BufferedReader inError = null;

        try {
            Launcher.ProcStarter ps = launcher.new ProcStarter();
            ps.cmds(command);
            ps.readStdout();
            ps.readStderr();
            proc = launcher.launch(ps);
            InputStream procStdout = proc.getStdout();
            InputStream procStderr = proc.getStderr();
            if (procStdout != null && procStderr != null) {
                in = new BufferedReader(new InputStreamReader(procStdout, StandardCharsets.UTF_8));
                inError = new BufferedReader(new InputStreamReader(procStderr, StandardCharsets.UTF_8));
            }
            String s;

            while ((s = in.readLine()) != null) {
                jobListener.getLogger().print(s + "\n");
                if (s.indexOf("*I,runner.sessionStarted: Session") > -1) {
                    foundGoodVSIF = true;
                    sessionNameToMonitor = s.substring(34, s.indexOf(" started."));

                    // Now creates the file of sessions.input
                    String fileOutput = buildNumber + "." + buildId + ".sessions.input";
                    StringBuffer writer = new StringBuffer();
                    writer.append(sessionNameToMonitor);
                    hudson.FilePath newFile = filePath.child(fileOutput);
                    if (newFile != null) {
                        newFile.write(writer.toString(), StandardCharsets.UTF_8.name());
                    } else {
                        throw new IOException("Failed to create file: " + fileOutput);
                    }
                }
            }

            String sError;
            while ((sError = inError.readLine()) != null) {
                jobListener.getLogger().print(sError + "\n");
            }

        } catch (IOException e) {
            jobListener.getLogger().println(e.getMessage());
            for (StackTraceElement ste : e.getStackTrace()) {
                jobListener.getLogger().println(" " + ste);
            }
            jobListener.getLogger().println(ExceptionUtils.getFullStackTrace(e));
        } catch (InterruptedException ex) {
            jobListener.getLogger().println(ex.getMessage());
            for (StackTraceElement ste : ex.getStackTrace()) {
                jobListener.getLogger().println(" " + ste);
            }
            jobListener.getLogger().println(ExceptionUtils.getFullStackTrace(ex));
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (inError != null) {
                try {
                    inError.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        if (foundGoodVSIF) {
            jobListener.getLogger().print("Found session name to monitor: " + sessionNameToMonitor + "\n");
        } else {
            throw new IOException("Failed to launch vsif using batch. Job stopped.");
        }
    }
}
