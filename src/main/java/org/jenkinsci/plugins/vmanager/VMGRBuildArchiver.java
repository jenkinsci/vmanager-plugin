/*
 * The MIT License
 *
 * Copyright 2017 Cadence.
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

import hudson.Launcher;
import hudson.model.Run;
import hudson.model.TaskListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.net.ssl.HttpsURLConnection;
import org.apache.commons.codec.binary.Base64;

/**
 *
 * @author tyanai
 */
public class VMGRBuildArchiver {

    private boolean vMGRBuildArchive = false;
    private String archiveUser;
    private String archivePassword;
    private boolean deleteAlsoSessionDirectory = false;
    private boolean genericCredentialForSessionDelete = false;
    private String famMode;
    private String famModeLocation;

    public VMGRBuildArchiver(boolean vMGRBuildArchive, boolean deleteAlsoSessionDirectory, boolean genericCredentialForSessionDelete, String archiveUser, String archivePassword, String famMode, String famModeLocation) {
        this.archiveUser = archiveUser;
        this.archivePassword = archivePassword;
        this.vMGRBuildArchive = vMGRBuildArchive;
        this.deleteAlsoSessionDirectory = deleteAlsoSessionDirectory;
        this.genericCredentialForSessionDelete = genericCredentialForSessionDelete;
        this.famMode = famMode;
        this.famModeLocation = famModeLocation;
    }

    public VMGRBuildArchiver() {
    }

    public boolean isVMGRBuildArchive() {
        return vMGRBuildArchive;
    }

    public void setVMGRBuildArchive(boolean isVMGRBuildArchive) {
        this.vMGRBuildArchive = isVMGRBuildArchive;
    }

    public String getArchiveUser() {
        return archiveUser;
    }

    public void setArchiveUser(String archiveUser) {
        this.archiveUser = archiveUser;
    }

    public String getArchivePassword() {
        return archivePassword;
    }

    public void setArchivePassword(String archivePassword) {
        this.archivePassword = archivePassword;
    }

    public boolean isDeleteAlsoSessionDirectory() {
        return deleteAlsoSessionDirectory;
    }

    public void setSeleteAlsoSessionDirectory(boolean deleteAlsoSessionDirectory) {
        this.deleteAlsoSessionDirectory = deleteAlsoSessionDirectory;
    }

    public boolean isGenericCredentialForSessionDelete() {
        return genericCredentialForSessionDelete;
    }

    public void setGenericCredentialForSessionDelete(boolean genericCredentialForSessionDelete) {
        this.genericCredentialForSessionDelete = genericCredentialForSessionDelete;
    }

    public void markBuildForArchive(List<String> listOfSessions, String apiURL, boolean requireAuth, String userUsedForLogin, String passwordUsedForLogin, String workingJobDir, TaskListener listener, Launcher launcher, Utils utils) throws InterruptedException {

        //Build a string from listOfSessions
        String sessions = null;
        Iterator<String> iter = listOfSessions.iterator();
        while (iter.hasNext()) {
            if (sessions == null) {
                sessions = iter.next();
            } else {
                sessions = sessions + "," + iter.next();
            }
        }

        //Save the data into sdi.properties
        String fileOutput = "sdi.properties";
        if (utils.getFilePath() == null){
             //Pipeline always run on master
             fileOutput = workingJobDir + File.separator + fileOutput;            
        }
        StringBuffer writer;
        try {
            writer = new StringBuffer();
            String url = apiURL.trim();
            url = url.substring(0, url.length() - 6);

            writer.append("url=" + url + "\n");
            writer.append("deleteAlsoSessionDirectory=" + deleteAlsoSessionDirectory + "\n");
            writer.append("genericCredentialForSessionDelete=" + genericCredentialForSessionDelete + "\n");
            writer.append("requireAuth=" + requireAuth + "\n");
            writer.append("famMode=" + famMode + "\n");
            if ("true".equals(famMode)) {
                writer.append("famModeLocation=" + famModeLocation + "\n");
            }
            if (genericCredentialForSessionDelete) {
                writer.append("archiveUser=" + archiveUser + "\n");
                writer.append("archivePassword=" + archivePassword + "\n");
            } else {
                writer.append("archiveUser=" + userUsedForLogin + "\n");
                writer.append("archivePassword=" + passwordUsedForLogin + "\n");
            }
            writer.append("sessions=" + sessions + "\n");
            utils.saveFileOnDisk(fileOutput, writer.toString());
            utils.moveFromNodeToMaster(fileOutput, launcher,writer.toString());
            

        } catch (IOException ex) {
            ex.printStackTrace();
            listener.getLogger().print("ERROR - Failed to create sdi.properties file on " + fileOutput);
            listener.getLogger().print(ex.getMessage());

        }

    }

    public void deleteSessions(Run run, Logger logger) throws Exception {

        //First check if dsi file exist:
        File tmpFile = new File(run.getRootDir().getPath() + File.separator + "sdi.properties");
        if (!tmpFile.exists()) {
            return;
        } else {
            Properties buildSdi = loadProperties(run.getRootDir().getPath(), logger);
            if ("true".equals(buildSdi.getProperty("famMode"))) {
                long timeInMs = System.currentTimeMillis();
                File destFile = new File(buildSdi.getProperty("famModeLocation").trim() + File.separator + run.getNumber() + "-" + timeInMs + "-sdi.properties");
                
                Files.copy( tmpFile.toPath(), destFile.toPath() );
                

            } else {

                String apiUrl = null;
                String userCredentials = buildSdi.getProperty("archiveUser").trim() + ":" + buildSdi.getProperty("archivePassword").trim();
                boolean requireAuth = true;
                if ("false".equals(buildSdi.getProperty("requireAuth").trim())) {
                    requireAuth = false;
                }

                //If this is a dedicted user, need to update the session's owner belfore trying to delete it:
                if ("true".equals(buildSdi.getProperty("genericCredentialForSessionDelete").trim())) {
                    String updateSessionOwner = "{\"update\":{\"owner\":\"" + buildSdi.getProperty("archiveUser").trim() + "\"},\"rs\":{\"filter\":{\"@c\":\".InFilter\",\"attName\":\"id\",\"operand\":\"IN\",\"values\":[" + buildSdi.getProperty("sessions") + "]}}}";
                    apiUrl = buildSdi.getProperty("url").trim();
                    apiUrl = apiUrl + "update";
                    HttpURLConnection conn = getVAPIConnection(apiUrl, requireAuth, userCredentials);
                    OutputStream os = conn.getOutputStream();
                    os.write(updateSessionOwner.getBytes(StandardCharsets.UTF_8));
                    os.flush();
                    if (conn.getResponseCode() != HttpURLConnection.HTTP_OK && conn.getResponseCode() != HttpURLConnection.HTTP_NO_CONTENT && conn.getResponseCode() != HttpURLConnection.HTTP_ACCEPTED && conn.getResponseCode() != HttpURLConnection.HTTP_CREATED && conn.getResponseCode() != HttpURLConnection.HTTP_PARTIAL && conn.getResponseCode() != HttpURLConnection.HTTP_RESET) {
                        String reason = "";
                        if (conn.getResponseCode() == 503) {
                            reason = "Failed to delete sessions.  vAPI process failed to connect to remote Verisium Manager server.";
                        }
                        if (conn.getResponseCode() == 401) {
                            reason = "Failed to delete sessions.  Authentication Error";
                        }
                        if (conn.getResponseCode() == 408) {
                            reason = "Failed to delete sessions.  No more licences are available for vAPI";
                        }
                        logger.log(Level.SEVERE, reason);
                        processErrorFromRespone(conn, logger);

                    }
                    conn.disconnect();
                }

                apiUrl = buildSdi.getProperty("url").trim();
                apiUrl = apiUrl + "delete";
                HttpURLConnection conn = getVAPIConnection(apiUrl, requireAuth, userCredentials);
                OutputStream os = conn.getOutputStream();
                String input = "{\"rs\":{\"filter\":{\"@c\":\".InFilter\",\"attName\":\"id\",\"operand\":\"IN\",\"values\":[" + buildSdi.getProperty("sessions") + "]}},\"with-session-dir\":" + buildSdi.getProperty("deleteAlsoSessionDirectory").trim() + "}";
                os.write(input.getBytes(StandardCharsets.UTF_8));
                os.flush();

                if (conn.getResponseCode() != HttpURLConnection.HTTP_OK && conn.getResponseCode() != HttpURLConnection.HTTP_NO_CONTENT && conn.getResponseCode() != HttpURLConnection.HTTP_ACCEPTED && conn.getResponseCode() != HttpURLConnection.HTTP_CREATED && conn.getResponseCode() != HttpURLConnection.HTTP_PARTIAL && conn.getResponseCode() != HttpURLConnection.HTTP_RESET) {
                    String reason = "";
                    if (conn.getResponseCode() == 503) {
                        reason = "Failed to delete sessions.  vAPI process failed to connect to remote Verisium Manager server.";
                    }
                    if (conn.getResponseCode() == 401) {
                        reason = "Failed to delete sessions.  Authentication Error";
                    }
                    if (conn.getResponseCode() == 408) {
                        reason = "Failed to delete sessions.  No more licences are available for vAPI";
                    }
                    logger.log(Level.SEVERE, reason);
                    processErrorFromRespone(conn, logger);

                } else {
                    logger.log(Level.INFO, "Sessions " + buildSdi.getProperty("sessions") + "were deleted from Verisium Manager DB");
                }

                conn.disconnect();
            }
        }

    }

    public void processErrorFromRespone(HttpURLConnection conn, Logger logger) {
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

            logger.log(Level.SEVERE, errorMessage);

        }
    }

    private Properties loadProperties(String path, Logger logger) throws IOException {

        Properties prop = new Properties();
        InputStream input = null;
        try {

            input = new FileInputStream(path + File.separator + "sdi.properties");
            // load a properties file
            prop.load(input);

        } catch (IOException ex) {
            logger.log(Level.SEVERE, "Failed to read " + path + File.separator + "sdi.properties during delete of session operation", ex);
            throw ex;
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return prop;
    }

    public HttpURLConnection getVAPIConnection(String apiUrl, boolean requireAuth, String authString) throws Exception {

        URL url = new URL(apiUrl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        if (apiUrl.indexOf("https://") > -1) {
            Utils.configureAllowAll((HttpsURLConnection) conn);
        }

        conn.setDoOutput(true);
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");

        // set the connection timeouts to one minute and the read timeout to 30 minutes by default
        conn.setConnectTimeout(60000);
        conn.setReadTimeout(1800000);

        if (requireAuth) {
            // ----------------------------------------------------------------------------------------
            // Authentication
            // ----------------------------------------------------------------------------------------

            byte[] authEncBytes = Base64.encodeBase64(authString.getBytes(StandardCharsets.UTF_8));
            String authStringEnc = new String(authEncBytes, StandardCharsets.UTF_8);
            conn.setRequestProperty("Authorization", "Basic " + authStringEnc);
            // ----------------------------------------------------------------------------------------
        }

        return conn;
    }

}
