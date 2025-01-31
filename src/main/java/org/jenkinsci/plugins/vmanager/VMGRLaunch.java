package org.jenkinsci.plugins.vmanager;

import com.cloudbees.plugins.credentials.CredentialsMatchers;
import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.common.StandardListBoxModel;
import com.cloudbees.plugins.credentials.common.StandardUsernamePasswordCredentials;
import com.cloudbees.plugins.credentials.domains.DomainRequirement;
import hudson.Extension;
import hudson.Launcher;
import hudson.model.BuildListener;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Item;
import hudson.model.Run;
import hudson.model.listeners.RunListener;
import hudson.security.ACL;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;

import jakarta.servlet.ServletException;
import jenkins.model.Jenkins;

import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.jenkinsci.plugins.tokenmacro.TokenMacro;
import org.kohsuke.stapler.AncestorInPath;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest2;

public class VMGRLaunch extends Builder {

    private final String vAPIUrl;
    private final boolean authRequired;
    private final boolean advConfig;
    private final String vAPIUser;
    private final String vAPIPassword;
    private final String vAPICredentials;
    private final String vSIFName;
    private final String vSIFInputFile;
    private final String credentialInputFile;
    private final boolean deleteInputFile;
    private final boolean deleteCredentialInputFile;
    private final boolean useUserOnFarm;
    private final boolean dynamicUserId;
    private final String vsifType;
    private final String userFarmType;
    private final String envSourceInputFile;

    private int connTimeout = 1;
    private int readTimeout = 30;
    private final boolean envVarible;

    private final String envVaribleFile;

    private final boolean attrValues;
    private final String attrValuesFile;

    private final String inaccessibleResolver;
    private final String envSourceInputFileType;
    private final String stoppedResolver;
    private final String failedResolver;
    private final String doneResolver;
    private final String suspendedResolver;
    private final boolean waitTillSessionEnds;

    private final boolean pauseSessionOnBuildInterruption;
    private int stepSessionTimeout = 0;

    private final boolean generateJUnitXML;
    private final boolean extraAttributesForFailures;
    private final String staticAttributeList;
    private final boolean markBuildAsFailedIfAllRunFailed;
    private final boolean failJobIfAllRunFailed;
    private final boolean markBuildAsPassedIfAllRunPassed;
    private final boolean failJobUnlessAllRunPassed;
    private final boolean userPrivateSSHKey;
    //private final String extraAttributesForFailuresInputFile;
    //private final boolean deleteExtraAttributesFile;

    private final boolean vMGRBuildArchive;
    private final boolean deleteAlsoSessionDirectory;
    private final boolean genericCredentialForSessionDelete;
    private final String archiveUser;
    private final String archivePassword;

    private final String famMode;
    private final String famModeLocation;
    private final boolean noAppendSeed;

    private final String executionType;
    private final String sessionsInputFile;
    private final boolean deleteSessionInputFile;
    private final String envVariableType;
    private final String envVariableText;
    private final String attrVariableType;
    private final String attrVariableText;

    private final String defineVaribleFile;
    private final boolean defineVarible;
    private final String defineVariableType;
    private final String defineVariableText;
    private String defineVaribleFileFix;

    //Variable that might contain macros
    private String sessionsInputFileFix;
    private String vSIFInputFileFix;
    private String vSIFNameFix;
    private String credentialInputFileFix;
    private String envSourceInputFileFix;
    private String envVaribleFileFix;

    private String attrValuesFileFix;
    private String famModeLocationFix;
    private final String executionScript;
    private final String executionShellLocation;
    private final String executionVsifFile;
    private final String credentialType;

    // Fields in config.jelly must match the parameter names in the
    // "DataBoundConstructor"
    @DataBoundConstructor
    public VMGRLaunch(String vAPIUrl, String vAPIUser, String vAPIPassword, String vSIFName, String vSIFInputFile, String credentialInputFile, boolean deleteInputFile, boolean deleteCredentialInputFile, boolean useUserOnFarm, boolean authRequired, String vsifType, String userFarmType,
            boolean dynamicUserId, boolean advConfig, int connTimeout, int readTimeout, boolean envVarible, String envVaribleFile, String inaccessibleResolver, String stoppedResolver, String failedResolver, String doneResolver, String suspendedResolver, boolean waitTillSessionEnds,
            int stepSessionTimeout, boolean generateJUnitXML, boolean extraAttributesForFailures, String staticAttributeList, boolean markBuildAsFailedIfAllRunFailed, boolean failJobIfAllRunFailed, String envSourceInputFile, boolean vMGRBuildArchive, boolean deleteAlsoSessionDirectory,
            boolean genericCredentialForSessionDelete, String archiveUser, String archivePassword, String famMode, String famModeLocation, boolean noAppendSeed, boolean markBuildAsPassedIfAllRunPassed, boolean failJobUnlessAllRunPassed, boolean userPrivateSSHKey, boolean attrValues,
            String attrValuesFile, String executionType, String sessionsInputFile, boolean deleteSessionInputFile, String envVariableType, String envVariableText, String attrVariableType, String attrVariableText, boolean pauseSessionOnBuildInterruption, String envSourceInputFileType,
            String executionScript, String executionShellLocation, String executionVsifFile, String defineVaribleFile, boolean defineVarible, String defineVariableType, String defineVariableText, String vAPICredentials, String credentialType) {
        this.vAPIUrl = vAPIUrl;
        this.vAPIUser = vAPIUser;
        this.vAPIPassword = vAPIPassword;
        this.vSIFName = vSIFName;
        this.vSIFInputFile = vSIFInputFile;
        this.credentialInputFile = credentialInputFile;
        this.authRequired = authRequired;
        this.advConfig = advConfig;
        this.envVarible = envVarible;
        this.deleteInputFile = deleteInputFile;
        this.deleteCredentialInputFile = deleteCredentialInputFile;
        this.useUserOnFarm = useUserOnFarm;
        this.vsifType = vsifType;
        this.userFarmType = userFarmType;
        this.dynamicUserId = dynamicUserId;
        this.envVaribleFile = envVaribleFile;
        this.attrValues = attrValues;
        this.attrValuesFile = attrValuesFile;
        this.envSourceInputFile = envSourceInputFile;

        this.connTimeout = connTimeout;
        this.readTimeout = readTimeout;

        this.envSourceInputFileType = envSourceInputFileType;
        this.inaccessibleResolver = inaccessibleResolver;
        this.stoppedResolver = stoppedResolver;
        this.failedResolver = failedResolver;
        this.doneResolver = doneResolver;
        this.suspendedResolver = suspendedResolver;
        this.waitTillSessionEnds = waitTillSessionEnds;
        this.stepSessionTimeout = stepSessionTimeout;

        this.generateJUnitXML = generateJUnitXML;
        this.extraAttributesForFailures = extraAttributesForFailures;
        this.markBuildAsFailedIfAllRunFailed = markBuildAsFailedIfAllRunFailed;
        this.failJobIfAllRunFailed = failJobIfAllRunFailed;
        this.markBuildAsPassedIfAllRunPassed = markBuildAsPassedIfAllRunPassed;
        this.failJobUnlessAllRunPassed = failJobUnlessAllRunPassed;
        this.userPrivateSSHKey = userPrivateSSHKey;
        this.staticAttributeList = staticAttributeList;

        this.vMGRBuildArchive = vMGRBuildArchive;
        this.deleteAlsoSessionDirectory = deleteAlsoSessionDirectory;
        this.genericCredentialForSessionDelete = genericCredentialForSessionDelete;
        this.archiveUser = archiveUser;
        this.archivePassword = archivePassword;

        this.famMode = famMode;
        this.famModeLocation = famModeLocation;
        this.noAppendSeed = noAppendSeed;

        this.executionType = executionType;
        this.sessionsInputFile = sessionsInputFile;
        this.deleteSessionInputFile = deleteSessionInputFile;

        this.envVariableType = envVariableType;
        this.envVariableText = envVariableText;
        this.attrVariableType = attrVariableType;
        this.attrVariableText = attrVariableText;
        this.pauseSessionOnBuildInterruption = pauseSessionOnBuildInterruption;

        this.executionScript = executionScript;
        this.executionShellLocation = executionShellLocation;
        this.executionVsifFile = executionVsifFile;

        this.defineVaribleFile = defineVaribleFile;
        this.defineVarible = defineVarible;
        this.defineVariableType = defineVariableType;
        this.defineVariableText = defineVariableText;
        this.vAPICredentials = vAPICredentials;
        this.credentialType = credentialType;

    }

    /**
     * We'll use this from the
     * <p>
     * config.jelly</p>.
     * @return 
     */
    public String getExecutionVsifFile() {
        return executionVsifFile;
    }

    public String getExecutionShellLocation() {
        return executionShellLocation;
    }

    public String getExecutionScript() {
        return executionScript;
    }

    public String getSessionsInputFile() {
        return sessionsInputFile;
    }

    public boolean isPauseSessionOnBuildInterruption() {
        return pauseSessionOnBuildInterruption;
    }

    public String getAttrVariableType() {
        return attrVariableType;
    }

    public String getAttrVariableText() {
        return attrVariableText;
    }

    public String getEnvVariableType() {
        return envVariableType;
    }

    public String getEnvVariableText() {
        return envVariableText;
    }

    public boolean isDeleteSessionInputFile() {
        return deleteSessionInputFile;
    }

    public String getExecutionType() {
        return executionType;
    }

    public boolean isExtraAttributesForFailures() {
        return extraAttributesForFailures;
    }

    public String getCredentialType() {
        return credentialType;
    }

    public boolean isNoAppendSeed() {
        return noAppendSeed;
    }

    public boolean isMarkBuildAsFailedIfAllRunFailed() {
        return markBuildAsFailedIfAllRunFailed;
    }

    public boolean isFailJobIfAllRunFailed() {
        return failJobIfAllRunFailed;
    }

    public boolean isMarkBuildAsPassedIfAllRunPassed() {
        return markBuildAsPassedIfAllRunPassed;
    }

    public boolean isFailJobUnlessAllRunPassed() {
        return failJobUnlessAllRunPassed;
    }

    public boolean isUserPrivateSSHKey() {
        return userPrivateSSHKey;
    }

    public String getStaticAttributeList() {
        return staticAttributeList;
    }

    public boolean isGenerateJUnitXML() {
        return generateJUnitXML;
    }

    public String getVAPIUrl() {
        return vAPIUrl;
    }

    public String getVAPIUser() {
        return vAPIUser;
    }

    public String getVAPIPassword() {
        return vAPIPassword;
    }

    public String getVSIFName() {
        return vSIFName;
    }

    public String getEnvVaribleFile() {
        return envVaribleFile;
    }

    public String getAttrValuesFile() {
        return attrValuesFile;
    }

    public String getEnvSourceInputFile() {
        return envSourceInputFile;
    }

    public String getVSIFInputFile() {
        return vSIFInputFile;
    }

    public String getCredentialInputFile() {
        return credentialInputFile;
    }

    public boolean isAuthRequired() {
        return authRequired;
    }

    public boolean isDeleteInputFile() {
        return deleteInputFile;
    }

    public boolean isDeleteCredentialInputFile() {
        return deleteCredentialInputFile;
    }

    public boolean isUseUserOnFarm() {
        return useUserOnFarm;
    }

    public boolean isDynamicUserId() {
        return dynamicUserId;
    }

    public String getVsifType() {
        return vsifType;
    }

    public String getUserFarmType() {
        return userFarmType;
    }

    public boolean isAdvConfig() {
        return advConfig;
    }

    public boolean isEnvVarible() {
        return envVarible;
    }

    public boolean isAttrValues() {
        return attrValues;
    }

    public int getConnTimeout() {
        return connTimeout;
    }

    public int getReadTimeout() {
        return readTimeout;
    }

    public int getStepSessionTimeout() {
        return stepSessionTimeout;
    }

    public String getInaccessibleResolver() {
        return inaccessibleResolver;
    }

    public String getEnvSourceInputFileType() {
        return envSourceInputFileType;
    }

    public String getStoppedResolver() {
        return stoppedResolver;
    }

    public String getFailedResolver() {
        return failedResolver;
    }

    public String getDoneResolver() {
        return doneResolver;
    }

    public String getSuspendedResolver() {
        return suspendedResolver;
    }

    public boolean isWaitTillSessionEnds() {
        return waitTillSessionEnds;
    }

    public boolean isVMGRBuildArchive() {
        return vMGRBuildArchive;
    }

    public boolean isDeleteAlsoSessionDirectory() {
        return deleteAlsoSessionDirectory;
    }

    public boolean isGenericCredentialForSessionDelete() {
        return genericCredentialForSessionDelete;
    }

    public String getArchiveUser() {
        return archiveUser;
    }

    public String getArchivePassword() {
        return archivePassword;
    }

    public String getFamMode() {
        return famMode;
    }

    public String getFamModeLocation() {
        return famModeLocation;
    }

    public String getDefineVaribleFile() {
        return defineVaribleFile;
    }

    public boolean isDefineVarible() {
        return defineVarible;
    }

    public String getDefineVariableType() {
        return defineVariableType;
    }

    public String getDefineVariableText() {
        return defineVariableText;
    }

    public String getVAPICredentials() {
        return vAPICredentials;
    }

    @Override
    public boolean perform(AbstractBuild build, Launcher launcher, BuildListener listener) {

        String workingJobDir = build.getRootDir().getAbsolutePath();
        listener.getLogger().println("Root dir is: " + workingJobDir);
        
        String macroVAPIUrl = vAPIUrl;
        try {
            macroVAPIUrl = TokenMacro.expandAll(build, listener, vAPIUrl);
        } catch (Exception e) {
            e.printStackTrace();
            listener.getLogger().println("Failed to extract out macro from the input of vAPIUrl: " + vAPIUrl);
        }
        
        listener.getLogger().println("The HOST for vAPI is: " + macroVAPIUrl);
        listener.getLogger().println("The user/password type for vAPI is: " + credentialType);
        String tempUser = vAPIUser;
        String tempPassword = vAPIPassword;
        if ("credential".equals(credentialType)) {
            //overwrite the plain text with the credentials
            StandardUsernamePasswordCredentials c = CredentialsProvider.findCredentialById(this.vAPICredentials, StandardUsernamePasswordCredentials.class, build, Collections.<DomainRequirement>emptyList());
            if (c != null){
                tempUser = c.getUsername();
                tempPassword = c.getPassword().getPlainText();
            }
            listener.getLogger().println("Credentials set with ID " + this.vAPICredentials);
        }
        listener.getLogger().println("The vAPIUser for vAPI is: " + tempUser);
        listener.getLogger().println("The vAPIPassword for vAPI is: *******");
        listener.getLogger().println("The authRequired for vAPI is: " + authRequired);
        listener.getLogger().println("The id is: " + build.getId());
        listener.getLogger().println("The number is: " + build.getNumber());
        listener.getLogger().println("The workspace dir is: " + build.getWorkspace());
        if (advConfig) {
            listener.getLogger().println("The connection timeout is: " + connTimeout + " minutes");
            listener.getLogger().println("The read api timeout is: " + readTimeout + " minutes");
        } else {
            listener.getLogger().println("The connection timeout is: 1 minutes");
            listener.getLogger().println("The read api timeout is: 30 minutes");
        }

        listener.getLogger().println("In case build is interrupted, sesssion will get paused: " + pauseSessionOnBuildInterruption);

        //Check if this is user's batch or launch
        listener.getLogger().println("The execution type set is " + executionType);
        if ("batch".equals(executionType)) {
            try {
                sessionsInputFileFix = TokenMacro.expandAll(build, listener, sessionsInputFile);
            } catch (Exception e) {
                e.printStackTrace();
                listener.getLogger().println("Failed to extract out macro from the input of sessionsInputFile: " + sessionsInputFile);
                sessionsInputFileFix = sessionsInputFile;
            }
            listener.getLogger().println("The session input file name is: " + sessionsInputFileFix);
            listener.getLogger().println("The deleteSessionInputFile : " + deleteSessionInputFile);
        }
        if ("hybrid".equals(executionType)) {
            listener.getLogger().println("Hybrid batch model with script: " + executionScript);
            listener.getLogger().println("Hybrid batch model with shell: " + executionShellLocation);
            listener.getLogger().println("Hybrid batch model with vsif: " + executionVsifFile);

        } else {
            listener.getLogger().println("The vsif to be executed is " + vsifType);

            try {
                vSIFNameFix = TokenMacro.expandAll(build, listener, vSIFName);
            } catch (Exception e) {
                e.printStackTrace();
                listener.getLogger().println("Failed to extract out macro from the input of vSIFName: " + vSIFName);
                vSIFNameFix = vSIFName;
            }
            listener.getLogger().println("The vSIFName is: " + vSIFNameFix);

            try {
                vSIFInputFileFix = TokenMacro.expandAll(build, listener, vSIFInputFile);
            } catch (Exception e) {
                e.printStackTrace();
                listener.getLogger().println("Failed to extract out macro from the input of vSIFInputFile: " + vSIFInputFile);
                vSIFInputFileFix = vSIFInputFile;
            }
            listener.getLogger().println("The vSIF Path For External VSIF Input is: " + vSIFInputFileFix);
            listener.getLogger().println("The deleteInputFile for vAPI is: " + deleteInputFile);
            if (envVarible) {
                listener.getLogger().println("An environment varible file was selected.");

            }

            if (attrValues) {
                listener.getLogger().println("An attribute values file was selected.");

            }

            if (defineVarible) {
                listener.getLogger().println("A define varible file was selected.");
            }

            if (useUserOnFarm) {
                listener.getLogger().println("An User's Credential use was selected.");
                listener.getLogger().println("The User's Credential type is: " + userFarmType);
                listener.getLogger().println("User is using private stored SSH key: " + userPrivateSSHKey);

                if ("dynamic".equals(userFarmType)) {
                    try {
                        credentialInputFileFix = TokenMacro.expandAll(build, listener, credentialInputFile);
                    } catch (Exception e) {
                        e.printStackTrace();
                        listener.getLogger().println("Failed to extract out macro from the input of credentialInputFile: " + credentialInputFile);
                        credentialInputFileFix = credentialInputFile;
                    }
                    listener.getLogger().println("The credential file is: " + credentialInputFileFix);
                    listener.getLogger().println("The credential file was set to be deleted after use: " + deleteCredentialInputFile);
                }
                if (envSourceInputFile != null && !"".equals(envSourceInputFile.trim())) {
                    try {
                        envSourceInputFileFix = TokenMacro.expandAll(build, listener, envSourceInputFile);
                    } catch (Exception e) {
                        e.printStackTrace();
                        listener.getLogger().println("Failed to extract out macro from the input of envSourceInputFile: " + envSourceInputFile);
                        envSourceInputFileFix = envSourceInputFile;
                    }
                    listener.getLogger().println("The User's source file is: " + envSourceInputFileFix);
                    listener.getLogger().println("The User's source file type is: " + envSourceInputFileType);

                } else {
                    listener.getLogger().println("The User's source file wasn't set");
                }

            }

        }

        StepHolder stepHolder = null;
        JUnitRequestHolder jUnitRequestHolder = null;

        if (waitTillSessionEnds) {
            listener.getLogger().println("Build set to finish only when session finish to run");

            listener.getLogger().println("In case session is at state \'inaccessible\' the build will " + inaccessibleResolver);
            listener.getLogger().println("In case session is at state \'failed\' the build will " + failedResolver);
            listener.getLogger().println("In case session is at state \'stopped\' the build will " + stoppedResolver);
            listener.getLogger().println("In case session is at state \'suspended\' the build will " + suspendedResolver);
            listener.getLogger().println("In case session is at state \'done\' the build will " + doneResolver);
            listener.getLogger().println("Timeout for entire step is " + stepSessionTimeout + " minutes");
            listener.getLogger().println("User choosed to mark regression as Failed in case all runs are failing: " + markBuildAsFailedIfAllRunFailed);
            listener.getLogger().println("User choosed to fail the job in case all runs are failing: " + failJobIfAllRunFailed);
            listener.getLogger().println("User choosed to mark regression as Passed in case all runs are passed: " + markBuildAsPassedIfAllRunPassed);
            listener.getLogger().println("User choosed to fail the job unless all runs are passed: " + failJobUnlessAllRunPassed);

            listener.getLogger().println("Generate XML Report XML output: " + generateJUnitXML);
            if (generateJUnitXML) {
                listener.getLogger().println("Do not append seed to test names: " + noAppendSeed);
                jUnitRequestHolder = new JUnitRequestHolder(generateJUnitXML, extraAttributesForFailures, staticAttributeList, noAppendSeed);

                listener.getLogger().println("Extra Attributes in JUnit Report: " + extraAttributesForFailures);
                if (extraAttributesForFailures) {
                    listener.getLogger().println("Extra Attributes list in JUnit Report is: " + staticAttributeList);
                }

            }

            stepHolder = new StepHolder(inaccessibleResolver, stoppedResolver, failedResolver, doneResolver, suspendedResolver, waitTillSessionEnds, stepSessionTimeout, jUnitRequestHolder, markBuildAsFailedIfAllRunFailed, failJobIfAllRunFailed, markBuildAsPassedIfAllRunPassed, failJobUnlessAllRunPassed, pauseSessionOnBuildInterruption);
        }

        VMGRBuildArchiver vMGRBuildArchiver = null;
        if (vMGRBuildArchive) {
            listener.getLogger().println("Session was set to get deleted when build is deleted");
            listener.getLogger().println("Delete also session directory on disk: " + deleteAlsoSessionDirectory);
            listener.getLogger().println("Use dedicated credentials for deleting the session: " + genericCredentialForSessionDelete);
            listener.getLogger().println("Use FAM Mode: " + famMode);
            if ("true".equals(famMode)) {
                try {
                    famModeLocationFix = TokenMacro.expandAll(build, listener, famModeLocation);
                } catch (Exception e) {
                    e.printStackTrace();
                    listener.getLogger().println("Failed to extract out macro from the input of famModeLocation: " + famModeLocation);
                    famModeLocationFix = famModeLocation;
                }
                listener.getLogger().println("FAM Mode Location: " + famModeLocationFix);
            }
            if (genericCredentialForSessionDelete) {
                listener.getLogger().println("Dedicated User for session delete: " + archiveUser);
                listener.getLogger().println("Dedicated password for session delete: *******");
            }
            vMGRBuildArchiver = new VMGRBuildArchiver(vMGRBuildArchive, deleteAlsoSessionDirectory, genericCredentialForSessionDelete, archiveUser, archivePassword, famMode, famModeLocationFix);

        }

        try {
            Utils utils = new Utils(build, listener);
            // Get the list of VSIF file to launch
            String[] vsifFileNames = null;
            String[] sessionNames = null;
            String jsonEnvInput = null;
            String jsonAttrValuesInput = null;
            String jsonDefineInput = null;
            String[] farmUserPassword = null;

            String tmpExecutionType = executionType;

            if ("batch".equals(executionType)) {
                if (sessionsInputFile == null || sessionsInputFile.trim().equals("")) {
                    listener.getLogger().println("The session input file chosen is dynamic. Dynamic workspace directory: '" + build.getWorkspace() + "'");
                } else {
                    listener.getLogger().println("The session input file chosen is static. Sessions input file name is: '" + sessionsInputFileFix.trim() + "'");
                }

                sessionNames = utils.loadDataFromInputFiles(build.getId(), build.getNumber(), "" + build.getWorkspace(), sessionsInputFileFix, listener, deleteSessionInputFile, "session names", "sessions.input");
                if (sessionNames.length == 0) {
                    listener.getLogger().println("No session were found within sessions.input file.  Exit Job.\n");
                    return false;
                }

            } else if ("hybrid".equals(executionType)) {
                //Launch the session and create the sessions.input
                tmpExecutionType = "batch"; // once we found the sessin name, the execution continues as if user did the batch first
                //BatchExecManager batchExecManager = new BatchExecManager(listener,TokenMacro.expandAll(build, listener, executionScript),executionShellLocation,executionVsifFile,build.getId(), build.getNumber());
                utils.batchExecManager(listener, TokenMacro.expandAll(build, listener, executionScript), executionShellLocation, TokenMacro.expandAll(build, listener, executionVsifFile), build.getId(), build.getNumber(), launcher);
                //batchExecManager.execBatchCommand(build.getExecutor().getCurrentWorkspace());
                sessionNames = utils.loadDataFromInputFiles(build.getId(), build.getNumber(), "" + build.getWorkspace(), "", listener, false, "session names", "sessions.input");
                if (sessionNames.length == 0) {
                    listener.getLogger().println("No session were found within sessions.input file.  Exit Job.\n");
                    return false;
                }

            } else {
                if ("static".equals(vsifType)) {
                    listener.getLogger().println("The VSIF file chosen is static. VSIF file static location is: '" + vSIFNameFix + "'");
                    vsifFileNames = new String[1];
                    vsifFileNames[0] = vSIFNameFix;
                } else {
                    if (vSIFInputFile == null || vSIFInputFile.trim().equals("")) {
                        listener.getLogger().println("The VSIF file chosen is dynamic. VSIF directory dynamic workspace directory: '" + build.getWorkspace() + "'");
                    } else {
                        listener.getLogger().println("The VSIF file chosen is dynamic. VSIF file name is: '" + vSIFInputFileFix.trim() + "'");
                    }

                    vsifFileNames = utils.loadDataFromInputFiles(build.getId(), build.getNumber(), "" + build.getWorkspace(), vSIFInputFileFix, listener, deleteInputFile, "VSIF", "vsif.input");

                }

                //check if user set an environment variables in addition:
                if (envVarible) {
                    if (envVariableType == null || "".equals(envVariableType) || "file".equals(envVariableType)) {
                        envVaribleFileFix = envVaribleFile;
                        if (envVaribleFile == null || envVaribleFile.trim().equals("")) {
                            listener.getLogger().println("The environment varible file chosen is dynamic. Env File directory dynamic workspace directory: '" + build.getWorkspace() + "'");
                        } else {

                            try {
                                envVaribleFileFix = TokenMacro.expandAll(build, listener, envVaribleFile);
                            } catch (Exception e) {
                                e.printStackTrace();
                                listener.getLogger().println("Failed to extract out macro from the input of envVaribleFile: " + envVaribleFile);
                                envVaribleFileFix = envVaribleFile;
                            }

                            listener.getLogger().println("The environment varible file chosen is static. Environment file name is: '" + envVaribleFileFix.trim() + "'");
                        }
                        jsonEnvInput = utils.loadJSONEnvInput(build.getId(), build.getNumber(), "" + build.getWorkspace(), envVaribleFileFix, listener);
                        try {
                            jsonEnvInput = TokenMacro.expandAll(build, listener, jsonEnvInput);
                        } catch (Exception e) {
                            e.printStackTrace();
                            listener.getLogger().println("Failed to extract out macro from the input of envVaribleFile: " + envVaribleFileFix);
                        }
                        listener.getLogger().println("Found the following environment for the vsif: " + jsonEnvInput);
                    }
                    if ("textarea".equals(envVariableType)) {
                        String tmpEnvText = null;
                        try {
                            tmpEnvText = TokenMacro.expandAll(build, listener, StringUtils.normalizeSpace(envVariableText));
                        } catch (Exception e) {
                            e.printStackTrace();
                            listener.getLogger().println("Failed to extract out macro from the input of envVariableText: " + StringUtils.normalizeSpace(envVariableText));
                            tmpEnvText = StringUtils.normalizeSpace(envVariableText);
                        }
                        jsonEnvInput = "\"environment\":{  " + tmpEnvText + "}";
                        listener.getLogger().println("Found the following environment variable textarea for the vsif: " + jsonEnvInput);
                    }

                }

                //check if user set an define values in addition:
                if (defineVarible) {
                    if (defineVariableType == null || "".equals(defineVariableType) || "file".equals(defineVariableType)) {
                        defineVaribleFileFix = defineVaribleFile;
                        if (defineVaribleFile == null || defineVaribleFile.trim().equals("")) {
                            listener.getLogger().println("The define values file chosen is dynamic. Define File directory dynamic workspace directory: '" + build.getWorkspace() + "'");
                        } else {

                            try {
                                defineVaribleFileFix = TokenMacro.expandAll(build, listener, defineVaribleFile);
                            } catch (Exception e) {
                                e.printStackTrace();
                                listener.getLogger().println("Failed to extract out macro from the input of defineVaribleFile: " + defineVaribleFile);
                                defineVaribleFileFix = defineVaribleFile;
                            }

                            listener.getLogger().println("The define values file chosen is static. Define values file name is: '" + defineVaribleFileFix.trim() + "'");
                        }
                        jsonDefineInput = utils.loadJSONDefineInput(build.getId(), build.getNumber(), "" + build.getWorkspace(), defineVaribleFileFix, listener);
                        try {
                            jsonDefineInput = TokenMacro.expandAll(build, listener, jsonDefineInput);
                        } catch (Exception e) {
                            e.printStackTrace();
                            listener.getLogger().println("Failed to extract out macro from the input of defineVaribleFile: " + defineVaribleFileFix);
                        }
                        listener.getLogger().println("Found the following define values for the vsif: " + jsonDefineInput);
                    }
                    if ("textarea".equals(defineVariableType)) {
                        String tmpAttrText = null;
                        String fetchDefineJsonFromTextArea = utils.loadJSONDefineValuesFromTextArea(build.getId(), build.getNumber(), "" + build.getWorkspace(), listener, defineVariableText);
                        try {
                            tmpAttrText = TokenMacro.expandAll(build, listener, StringUtils.normalizeSpace(fetchDefineJsonFromTextArea));
                        } catch (Exception e) {
                            e.printStackTrace();
                            listener.getLogger().println("Failed to extract out macro from the input of fetchJsonFromTextArea: " + StringUtils.normalizeSpace(fetchDefineJsonFromTextArea));
                            tmpAttrText = StringUtils.normalizeSpace(fetchDefineJsonFromTextArea);
                        }
                        jsonDefineInput = tmpAttrText;
                        listener.getLogger().println("Found the following define values textarea for the vsif: " + jsonDefineInput);
                    }
                }

                //check if user set an attribute values in addition:
                if (attrValues) {
                    if (attrVariableType == null || "".equals(attrVariableType) || "file".equals(attrVariableType)) {
                        attrValuesFileFix = attrValuesFile;
                        if (attrValuesFile == null || attrValuesFile.trim().equals("")) {
                            listener.getLogger().println("The attribute values file chosen is dynamic. Env File directory dynamic workspace directory: '" + build.getWorkspace() + "'");
                        } else {

                            try {
                                attrValuesFileFix = TokenMacro.expandAll(build, listener, attrValuesFile);
                            } catch (Exception e) {
                                e.printStackTrace();
                                listener.getLogger().println("Failed to extract out macro from the input of attrValuesFile: " + attrValuesFile);
                                attrValuesFileFix = attrValuesFile;
                            }

                            listener.getLogger().println("The attribute values file chosen is static. Attribute values file name is: '" + attrValuesFileFix.trim() + "'");
                        }
                        jsonAttrValuesInput = utils.loadJSONAttrValuesInput(build.getId(), build.getNumber(), "" + build.getWorkspace(), attrValuesFileFix, listener);
                        try {
                            jsonAttrValuesInput = TokenMacro.expandAll(build, listener, jsonAttrValuesInput);
                        } catch (Exception e) {
                            e.printStackTrace();
                            listener.getLogger().println("Failed to extract out macro from the input of attrValuesFile: " + attrValuesFileFix);
                        }
                        listener.getLogger().println("Found the following attribute values for the vsif: " + jsonAttrValuesInput);
                    }
                    if ("textarea".equals(attrVariableType)) {
                        String tmpAttrText = null;
                        String fetchJsonFromTextArea = utils.loadJSONAttrValuesFromTextArea(build.getId(), build.getNumber(), "" + build.getWorkspace(), listener, attrVariableText);
                        try {
                            tmpAttrText = TokenMacro.expandAll(build, listener, StringUtils.normalizeSpace(fetchJsonFromTextArea));
                        } catch (Exception e) {
                            e.printStackTrace();
                            listener.getLogger().println("Failed to extract out macro from the input of fetchJsonFromTextArea: " + StringUtils.normalizeSpace(fetchJsonFromTextArea));
                            tmpAttrText = StringUtils.normalizeSpace(fetchJsonFromTextArea);
                        }
                        jsonAttrValuesInput = tmpAttrText;
                        listener.getLogger().println("Found the following attribute values textarea for the vsif: " + jsonAttrValuesInput);
                    }
                }

                if ("dynamic".equals(userFarmType)) {
                    if (credentialInputFile == null || credentialInputFile.trim().equals("")) {
                        listener.getLogger().println("The credential file chosen is dynamic. Credential directory dynamic workspace directory: '" + build.getWorkspace() + "'");
                    } else {
                        listener.getLogger().println("The credential file chosen is static. Credential file name is: '" + credentialInputFileFix.trim() + "'");
                    }
                    farmUserPassword = utils.loadFileCredentials(build.getId(), build.getNumber(), "" + build.getWorkspace(), credentialInputFileFix, listener, deleteCredentialInputFile);

                    //Tal Yanai
                    //In case this is a private user SSH, use the dynamic information for the vAPI login as well
                    if (userPrivateSSHKey) {
                        tempUser = farmUserPassword[0];
                        tempPassword = farmUserPassword[1];
                    }
                }
            }

            // Now call the actual launch
            // ----------------------------------------------------------------------------------------------------------------
            String output = utils.executeVSIFLaunch(vsifFileNames, macroVAPIUrl, authRequired, tempUser, tempPassword, listener, dynamicUserId, build.getId(), build.getNumber(),
                    "" + build.getWorkspace(), connTimeout, readTimeout, advConfig, jsonEnvInput, useUserOnFarm, userFarmType, farmUserPassword, stepHolder, envSourceInputFileFix, workingJobDir, vMGRBuildArchiver, userPrivateSSHKey, jsonAttrValuesInput, tmpExecutionType, sessionNames, envSourceInputFileType, launcher, jsonDefineInput);
            if (!"success".equals(output)) {
                listener.getLogger().println("Failed to launch vsifs for build " + build.getId() + " " + build.getNumber() + "\n");
                listener.getLogger().println(output + "\n");
                return false;
            }
            // ----------------------------------------------------------------------------------------------------------------

        } catch (Exception e) {
            listener.getLogger().println("Failed to build " + build.getId() + " " + build.getNumber());
            listener.getLogger().println(e.getMessage());
            for (StackTraceElement ste : e.getStackTrace()) {
                listener.getLogger().println(" " + ste);
            }

            listener.getLogger().println(ExceptionUtils.getFullStackTrace(e));

            return false;
        }

        return true;
    }

    // Overridden for better type safety.
    // If your plugin doesn't really define any property on Descriptor,
    // you don't have to do this.
    @Override
    public DescriptorImpl getDescriptor() {
        return (DescriptorImpl) super.getDescriptor();
    }

    @Extension
    public static final class VMGRDeletedJobListener extends RunListener<Run> {

        @Override
        public void onDeleted(Run run) {

            VMGRBuildArchiver vMGRBuildArchiver = new VMGRBuildArchiver();
            try {
                vMGRBuildArchiver.deleteSessions(run, logger);
            } catch (Exception ex) {
                logger.log(Level.SEVERE, "Failed to delete session during build removal.", ex);
            }

        }

        private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(VMGRDeletedJobListener.class.getName());
    }

    /**
     * Descriptor for {@link VMGRLaunch}. Used as a singleton. The class is
     * marked as public so that it can be accessed from views.
     *
     * <p>
     * See
     * <p>
     * src/main/resources/hudson/plugins/hello_world/HelloWorldBuilder/*.jelly</p>
     * for the actual HTML fragment for the configuration screen.
     */
    @Extension
    // This indicates to Jenkins that this is an implementation of an extension
    // point.
    public static final class DescriptorImpl extends BuildStepDescriptor<Builder> {

        /**
         * To persist global configuration information, simply store it in a
         * field and call save().
         *
         *
         *
         * /**
         * In order to load the persisted global configuration, you have to call
         * load() in the constructor.
         */
        public DescriptorImpl() {
            load();
        }

        /**
         * Performs on-the-fly validation of the form field 'name'.
         *
         * @param value This parameter receives the value that the user has
         * typed.
         * @return Indicates the outcome of the validation. This is sent to the
         * browser.
         * <p>
         * Note that returning {@link FormValidation#error(String)} does not
         * prevent the form from being saved. It just means that a message will
         * be displayed to the user.
         * @throws java.io.IOException
         * @throws jakarta.servlet.ServletException
         */
        public FormValidation doCheckVAPIUrl(@QueryParameter String value) throws IOException, ServletException {
            if (value.length() == 0) {
                return FormValidation.error("Please set the Verisium Manager vAPI HOST ");
            }
            if (value.length() < 4) {
                return FormValidation.warning("Isn't the name too short?");
            }
            return FormValidation.ok();
        }

        public FormValidation doCheckStaticAttributeList(@QueryParameter String value) throws IOException, ServletException {
            if (value != null) {
                if (value.indexOf(";") > 0) {
                    return FormValidation.error("(;) is not allowed for seperation.  Please use only comma as a seperator.");
                } else if (value.indexOf("|") > 0) {
                    return FormValidation.error("(|) is not allowed for seperation.  Please use only comma as a seperator.");
                } else if (value.indexOf(".") > 0) {
                    return FormValidation.error("(.) is not allowed for seperation.  Please use only comma as a seperator.");
                }
            

                List<String> items = Arrays.asList(value.split("\\s*,\\s*"));

                Iterator<String> iter = items.iterator();

                String tmpAttr = null;
                while (iter.hasNext()) {
                    tmpAttr = iter.next();
                    if (tmpAttr.indexOf(" ") > 0) {
                        return FormValidation.error("'" + tmpAttr + "' is not a valid option for Verisium Manager attribute code name. Attribute code names can't have space.  Try using underscore instaed.");
                    } else if (tmpAttr.equals("first_failure_name")) {
                        return FormValidation.warning("'" + tmpAttr + "' is already included as part of the stack error message by default.");
                    } else if (tmpAttr.equals("first_failure_description")) {
                        return FormValidation.warning("'" + tmpAttr + "' is already included as part of the stack error message by default.");
                    } else if (tmpAttr.equals("computed_seed")) {
                        return FormValidation.warning("'" + tmpAttr + "' is already included as part of the stack error message by default.");
                    } else if (tmpAttr.equals("test_group")) {
                        return FormValidation.warning("'" + tmpAttr + "' is already included as part of the stack error message by default.");
                    } else if (tmpAttr.equals("test_name")) {
                        return FormValidation.warning("'" + tmpAttr + "' is already included as part of the stack error message by default.");
                    }
                }
            }

            return FormValidation.ok();
        }

        public boolean isApplicable(Class<? extends AbstractProject> aClass) {
            // Indicates that this builder can be used with all kinds of project
            // types
            return true;
        }

        public ListBoxModel doFillInaccessibleResolverItems() {
            ListBoxModel items = new ListBoxModel();
            items.add("Mark the build as failed", "fail");
            items.add("Continue, and move to the next build step", "continue");
            items.add("Ignore, and continue to wait", "ignore");
            return items;
        }

        public ListBoxModel doFillEnvSourceInputFileTypeItems() {
            ListBoxModel items = new ListBoxModel();
            items.add("bash", "BSH");
            items.add("csh", "CSH");
            return items;
        }

        public ListBoxModel doFillStoppedResolverItems() {
            ListBoxModel items = new ListBoxModel();
            items.add("Mark the build as failed", "fail");
            items.add("Continue, and move to the next build step", "continue");
            items.add("Ignore, and continue to wait", "ignore");
            return items;
        }

        public ListBoxModel doFillFailedResolverItems() {
            ListBoxModel items = new ListBoxModel();
            items.add("Mark the build as failed", "fail");
            items.add("Continue, and move to the next build step", "continue");
            items.add("Ignore, and continue to wait", "ignore");
            return items;
        }

        public ListBoxModel doFillDoneResolverItems() {
            ListBoxModel items = new ListBoxModel();
            items.add("Ignore, and continue to wait", "ignore");
            items.add("Continue, and move to the next build step", "continue");
            items.add("Mark the build as failed", "fail");
            return items;
        }

        public ListBoxModel doFillSuspendedResolverItems() {
            ListBoxModel items = new ListBoxModel();
            items.add("Ignore, and continue to wait", "ignore");
            items.add("Continue, and move to the next build step", "continue");
            items.add("Mark the build as failed", "fail");
            return items;
        }

        public ListBoxModel doFillVAPICredentialsItems(
                @AncestorInPath Item item,
                @QueryParameter String vAPICredentials
        ) {
            StandardListBoxModel result = new StandardListBoxModel();
            if (item == null) {
                if (!Jenkins.getInstance().hasPermission(Jenkins.ADMINISTER)) {
                    return result.includeCurrentValue(vAPICredentials); // (2)
                }
            } else {
                if (!item.hasPermission(Item.EXTENDED_READ)
                        && !item.hasPermission(CredentialsProvider.USE_ITEM)) {
                    return result.includeCurrentValue(vAPICredentials); // (2)
                }
            }
            return result
                    .includeEmptyValue()
                    .includeMatchingAs(ACL.SYSTEM, Jenkins.getInstance(), StandardUsernamePasswordCredentials.class, Collections.<DomainRequirement>emptyList(), CredentialsMatchers.always())
                    .includeCurrentValue(vAPICredentials); // (5)

        }

        /**
         * This human readable name is used in the configuration screen.
         */
        public String getDisplayName() {
            return "Cadence vManager Session Launcher";
        }

        @Override
        public boolean configure(StaplerRequest2 req, JSONObject formData) throws FormException {

            save();
            return super.configure(req, formData);
        }

        public FormValidation doTestConnection(@QueryParameter("vAPIUser") final String vAPIUser, @QueryParameter("vAPIPassword") final String vAPIPassword,
                @QueryParameter("vAPIUrl") final String vAPIUrl, @QueryParameter("authRequired") final boolean authRequired,
                @QueryParameter("credentialType") final String credentialType, @QueryParameter("vAPICredentials") final String vAPICredentials, @AncestorInPath Item item)
                throws IOException, ServletException {
            try {
                String tempUser = vAPIUser;
                String tempPassword = vAPIPassword;
                boolean foundMatchUserPassword = false;
                if ("credential".equals(credentialType)) {
                    //System.out.println("Trying to find the credential...");
                    //overwrite the plain text with the credentials
                    //StandardUsernamePasswordCredentials c = CredentialsProvider.findCredentialById(vAPICredentials, StandardUsernamePasswordCredentials.class, item, Collections.<DomainRequirement>emptyList());
                    List<StandardUsernamePasswordCredentials> listOfC = CredentialsProvider.lookupCredentials(StandardUsernamePasswordCredentials.class, item, ACL.SYSTEM, Collections.<DomainRequirement>emptyList());
                    Iterator<StandardUsernamePasswordCredentials> cIterator = listOfC.iterator();
                    StandardUsernamePasswordCredentials tmpHolder = null;
                    while (cIterator.hasNext()) {
                        tmpHolder = cIterator.next();
                        if (vAPICredentials.equals(tmpHolder.getId())) {
                            tempUser = tmpHolder.getUsername();
                            tempPassword = tmpHolder.getPassword().getPlainText();
                            foundMatchUserPassword = true;
                            break;
                        }

                    }

                } else {
                    foundMatchUserPassword = true;
                }

                if (foundMatchUserPassword) {
                    Utils utils = new Utils();
                    String output = utils.checkVAPIConnection(vAPIUrl, authRequired, tempUser, tempPassword);
                    if (!output.startsWith("Failed")) {
                        return FormValidation.ok("Success. " + output);
                    } else {
                        return FormValidation.error(output);
                    }
                } else {
                    return FormValidation.error("Could not extract the user/password from the supplied Credential object.  Object was not found within your Jenkins domain.");
                }
            } catch (Exception e) {
                return FormValidation.error("Client error : " + e.getMessage());
            }
        }

        public FormValidation doTestArchiveUser(@QueryParameter("archiveUser") final String archiveUser, @QueryParameter("archivePassword") final String archivePassword,
                @QueryParameter("vAPIUrl") final String vAPIUrl) throws IOException,
                ServletException {
            try {

                Utils utils = new Utils();
                String output = utils.checkVAPIConnection(vAPIUrl, true, archiveUser, archivePassword);
                if (!output.startsWith("Failed")) {
                    return FormValidation.ok("Success. " + output);
                } else {
                    return FormValidation.error(output);
                }
            } catch (Exception e) {
                return FormValidation.error("Client error : " + e.getMessage());
            }
        }

        public FormValidation doTestExtraStaticAttr(@QueryParameter("vAPIUser") final String vAPIUser, @QueryParameter("vAPIPassword") final String vAPIPassword,
                @QueryParameter("vAPIUrl") final String vAPIUrl, @QueryParameter("authRequired") final boolean authRequired, @QueryParameter("staticAttributeList") final String staticAttributeList) throws IOException,
                ServletException {
            try {

                Utils utils = new Utils();
                String output = utils.checkExtraStaticAttr(vAPIUrl, authRequired, vAPIUser, vAPIPassword, staticAttributeList);
                if (!output.startsWith("Failed")) {
                    return FormValidation.ok("Success. " + output);
                } else {
                    return FormValidation.error(output);
                }
            } catch (Exception e) {
                return FormValidation.error("Client error : " + e.getMessage());
            }
        }

        public FormValidation doCheckVAPICredentials(
                @AncestorInPath Item item, // (2)
                @QueryParameter String value // (1)

        ) {
            if (item == null) {
                if (!Jenkins.getInstance().hasPermission(Jenkins.ADMINISTER)) {
                    return FormValidation.ok(); // (3)
                }
            } else {
                if (!item.hasPermission(Item.EXTENDED_READ)
                        && !item.hasPermission(CredentialsProvider.USE_ITEM)) {
                    return FormValidation.ok(); // (3)
                }
            }
            if (StringUtils.isBlank(value)) { // (4)
                return FormValidation.ok(); // (4)
            }
            //.includeMatchingAs(ACL.SYSTEM,Jenkins.getInstance(),StandardUsernamePasswordCredentials.class,Collections.<DomainRequirement>emptyList(),CredentialsMatchers.always())
            if (CredentialsProvider.listCredentials( // (6)
                    StandardUsernamePasswordCredentials.class, // (1)
                    item,
                    ACL.SYSTEM,
                    Collections.<DomainRequirement>emptyList(),
                    CredentialsMatchers.always() // (6)
            ).isEmpty()) {
                return FormValidation.error("Cannot find currently selected credentials");
            }
            return FormValidation.ok();
        }

    }
}
