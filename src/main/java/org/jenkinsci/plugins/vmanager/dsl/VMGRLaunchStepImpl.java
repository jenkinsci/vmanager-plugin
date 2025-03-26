package org.jenkinsci.plugins.vmanager.dsl;

import hudson.EnvVars;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.Run;
import hudson.model.TaskListener;
import java.io.File;
import org.jenkinsci.plugins.vmanager.JUnitRequestHolder;
import org.jenkinsci.plugins.vmanager.StepHolder;
import org.jenkinsci.plugins.vmanager.Utils;
import org.jenkinsci.plugins.vmanager.VMGRBuildArchiver;
import org.jenkinsci.plugins.workflow.steps.SynchronousNonBlockingStepExecution;
import org.jenkinsci.plugins.workflow.steps.StepContext;



public class VMGRLaunchStepImpl extends SynchronousNonBlockingStepExecution {

    private static final long serialVersionUID = 1000009076155338047L;
    
    private transient final VMGRLaunchStep step;

    VMGRLaunchStepImpl(VMGRLaunchStep step, StepContext context) {
        super(context);
        this.step = step;

    }

    @Override
    protected Void run() throws Exception {

        TaskListener listener = getContext().get(TaskListener.class);
        EnvVars envVars = getContext().get(EnvVars.class);
        Launcher launcher = getContext().get(Launcher.class);
        FilePath filePath = getContext().get(FilePath.class);
        String buildId = envVars.get("BUILD_ID");
        int buildNumber = Integer.parseInt(envVars.get("BUILD_NUMBER"));

        String workspace = envVars.get("WORKSPACE");

        //If this is a master/node configuration, we need the master workspace.  The above is the node workspace
        listener.getLogger().println("Setup is using nodes for job execution: " + step.isPipelineNodes());
        if (step.isPipelineNodes()) {
            //Get the name of the job
            String tmpJobName = envVars.get("JOB_NAME");
            /*
             String tmpJobName = workspace.substring(workspace.lastIndexOf(File.separator),workspace.length());
             if (workspace.lastIndexOf(File.separator) < 0){
                 throw new Exception("Failed to extract the name of the job fromt the node workspace");
             }
             */
            listener.getLogger().println("Master Job Name: " + tmpJobName);
            workspace = step.getMasterWorkspaceLocation() + File.separator + tmpJobName;
            listener.getLogger().println("Master worspace location: " + workspace);
        }

        //Chekc if workspace is there, unless create it
        File theWSDir = new File(workspace);
        // if the directory does not exist, create it
        if (!theWSDir.exists()) {
            listener.getLogger().println("creating workspace directory: " + theWSDir.getName());
            boolean result = false;

            try {
                result = theWSDir.mkdir();
            } catch (SecurityException se) {
                listener.getLogger().println("Failed to create workspace dir.  Permisisons issues");
                throw se;
            }
            if (result) {
                listener.getLogger().println("Workspace dir created");
            }
        }

        Run run = getContext().get(Run.class);

        String workingJobDir = run.getRootDir().getAbsolutePath();
        listener.getLogger().println("Root dir is: " + workingJobDir);
        listener.getLogger().println("Running Cadence vManager Session Launcher step");
        listener.getLogger().println("The HOST for vAPI is: " + step.getVAPIUrl());
        listener.getLogger().println("The vAPIUser for vAPI is: " + step.getVAPIUser());
        listener.getLogger().println("The vAPIPassword for vAPI is: *******");
        listener.getLogger().println("The authRequired for vAPI is: " + step.isAuthRequired());
        listener.getLogger().println("The id is: " + buildId);
        listener.getLogger().println("The number is: " + buildNumber);
        listener.getLogger().println("The workspace dir is: " + workspace);
        listener.getLogger().println("The FilePath dir is: " + filePath);

        if (step.isAdvConfig()) {
            listener.getLogger().println("The connection timeout is: " + step.getConnTimeout() + " minutes");
            listener.getLogger().println("The read api timeout is: " + step.getReadTimeout() + " minutes");
        } else {
            listener.getLogger().println("The connection timeout is: 1 minutes");
            listener.getLogger().println("The read api timeout is: 30 minutes");
        }

        listener.getLogger().println("In case build is interrupted, sesssion will get paused: " + step.isPauseSessionOnBuildInterruption());

        //Check if this is user's batch or launch
        listener.getLogger().println("The execution type set is " + step.getExecutionType());
        if ("batch".equals(step.getExecutionType())) {
            listener.getLogger().println("The session input file name is: " + step.getSessionsInputFile());
            listener.getLogger().println("The deleteSessionInputFile : " + step.isDeleteSessionInputFile());
        } if ("hybrid".equals(step.getExecutionType())) {
            listener.getLogger().println("Hybrid batch model with script: " + step.getExecutionScript());
            listener.getLogger().println("Hybrid batch model with shell: " + step.getExecutionShellLocation());
            listener.getLogger().println("Hybrid batch model with vsif: " + step.getExecutionVsifFile());
        } else {
            listener.getLogger().println("The vsif to be executed is for vAPI is " + step.getVsifType());
            listener.getLogger().println("The vSIFName for vAPI is: " + step.getVSIFName());
            listener.getLogger().println("The vSIFPathForExternalVSIF Input for vAPI is: " + step.getVSIFInputFile());
            listener.getLogger().println("The deleteInputFile for vAPI is: " + step.isDeleteInputFile());
            if (step.isEnvVarible()) {
                listener.getLogger().println("An environment varible file was selected.");
            }
            if (step.isAttrValues()) {
                listener.getLogger().println("An attribute values file was selected.");
            }
            if (step.isDefineVarible()) {
                listener.getLogger().println("A define varible file was selected.");
            }

            if (step.isUseUserOnFarm()) {
                listener.getLogger().println("An User's Credential use was selected.");
                listener.getLogger().println("The User's Credential type is: " + step.getUserFarmType());
                listener.getLogger().println("User is using private stored SSH key: " + step.isUserPrivateSSHKey());

                if ("dynamic".equals(step.getUserFarmType())) {
                    listener.getLogger().println("The credential file is: " + step.getCredentialInputFile());
                    listener.getLogger().println("The credential file was set to be deleted after use: " + step.isDeleteCredentialInputFile());
                }
                if (step.getEnvSourceInputFile() != null && !"".equals(step.getEnvSourceInputFile().trim())) {
                    listener.getLogger().println("The User's source file is: " + step.getEnvSourceInputFile());
                    listener.getLogger().println("The User's source file type is: " + step.getEnvSourceInputFileType());

                } else {
                    listener.getLogger().println("The User's source file wasn't set");
                }

            }
        }

        StepHolder stepHolder = null;
        JUnitRequestHolder jUnitRequestHolder = null;

        if (step.isWaitTillSessionEnds()) {
            listener.getLogger().println("Build set to finish only when session finish to run");

            listener.getLogger().println("In case session is at state \'inaccessible\' the build will " + step.getInaccessibleResolver());
            listener.getLogger().println("In case session is at state \'failed\' the build will " + step.getFailedResolver());
            listener.getLogger().println("In case session is at state \'stopped\' the build will " + step.getStoppedResolver());
            listener.getLogger().println("In case session is at state \'suspended\' the build will " + step.getSuspendedResolver());
            listener.getLogger().println("In case session is at state \'done\' the build will " + step.getDoneResolver());
            listener.getLogger().println("Timeout for entire step is " + step.getStepSessionTimeout() + " minutes");
            listener.getLogger().println("User choosed to mark regression as Failed in case all runs are failing: " + step.isMarkBuildAsFailedIfAllRunFailed());
            listener.getLogger().println("User choosed to fail the job in case all runs are failing: " + step.isFailJobIfAllRunFailed());
            listener.getLogger().println("User choosed to mark regression as Passed in case all runs are passed: " + step.isMarkBuildAsPassedIfAllRunPassed());
            listener.getLogger().println("User choosed to fail the job unless all runs are passed: " + step.isFailJobUnlessAllRunPassed());

            listener.getLogger().println("Generate XML Report XML output: " + step.isGenerateJUnitXML());
            if (step.isGenerateJUnitXML()) {

                listener.getLogger().println("Do not append seed to test names: " + step.isNoAppendSeed());
                jUnitRequestHolder = new JUnitRequestHolder(step.isGenerateJUnitXML(), step.isExtraAttributesForFailures(), step.getStaticAttributeList(), step.isNoAppendSeed());
                listener.getLogger().println("Extra Attributes in JUnit Report: " + step.isExtraAttributesForFailures());
                if (step.isExtraAttributesForFailures()) {
                    listener.getLogger().println("Extra Attributes list in JUnit Report is: " + step.getStaticAttributeList());
                }

            }

            stepHolder = new StepHolder(step.getInaccessibleResolver(), step.getStoppedResolver(), step.getFailedResolver(), step.getDoneResolver(), step.getSuspendedResolver(), step.isWaitTillSessionEnds(), step.getStepSessionTimeout(), jUnitRequestHolder, step.isMarkBuildAsFailedIfAllRunFailed(), step.isFailJobIfAllRunFailed(), step.isMarkBuildAsPassedIfAllRunPassed(), step.isFailJobUnlessAllRunPassed(), step.isPauseSessionOnBuildInterruption());
        }

        VMGRBuildArchiver vMGRBuildArchiver = null;
        if (step.isVMGRBuildArchive()) {
            listener.getLogger().println("Session was set to get deleted when build is deleted");
            listener.getLogger().println("Delete also session directory on disk: " + step.isDeleteAlsoSessionDirectory());
            listener.getLogger().println("Use dedicated credentials for deleting the session: " + step.isGenericCredentialForSessionDelete());
            listener.getLogger().println("Use FAM Mode: " + step.getFamMode());
            if ("true".equals(step.getFamMode())) {
                listener.getLogger().println("FAM Mode Location: " + step.getFamModeLocation());
            }
            if (step.isGenericCredentialForSessionDelete()) {
                listener.getLogger().println("Dedicated User for session delete: " + step.getArchiveUser());
                listener.getLogger().println("Dedicated password for session delete: *******");
            }
            vMGRBuildArchiver = new VMGRBuildArchiver(step.isVMGRBuildArchive(), step.isGenericCredentialForSessionDelete(), step.isGenericCredentialForSessionDelete(), step.getArchiveUser(), step.getArchivePassword(), step.getFamMode(), step.getFamModeLocation());

        }

        try {
            Utils utils = new Utils(run, listener,filePath);
            // Get the list of VSIF file to launch
            String[] vsifFileNames = null;
            String[] sessionNames = null;
            String jsonEnvInput = null;
            String jsonDefineInput = null;
            String jsonAttrValuesInput = null;
            String[] farmUserPassword = null;
            String tempUser = step.getVAPIUser();
            String tempPassword = step.getVAPIPassword();
            String tmpExecutionType = step.getExecutionType();

            if ("batch".equals(step.getExecutionType())) {
                if (step.getSessionsInputFile() == null || step.getSessionsInputFile().trim().equals("")) {
                    listener.getLogger().println("The session input file chosen is dynamic. Dynamic workspace directory: '" + workspace + "'");
                } else {
                    listener.getLogger().println("The session input file chosen is static. Sessions input file name is: '" + step.getSessionsInputFile().trim() + "'");
                }

                sessionNames = utils.loadDataFromInputFiles(buildId, buildNumber, "" + workspace, step.getSessionsInputFile(), listener, step.isDeleteSessionInputFile(), "session names", "sessions.input");
                if (sessionNames.length == 0){
                    listener.getLogger().println("No session were found within sessions.input file.  Exit Job.\n");
                    throw new Exception("No session were found within sessions.input file.  Exit Job.\n"); //false;
                }
            } else if ("hybrid".equals(step.getExecutionType())) {
                //Launch the session and create the sessions.input
                tmpExecutionType = "batch"; // once we found the sessin name, the execution continues as if user did the batch first
                //BatchExecManager batchExecManager = new BatchExecManager(listener, step.getExecutionScript(), step.getExecutionShellLocation(), step.getExecutionVsifFile(),buildId, buildNumber);
                //batchExecManager.execBatchCommand(filePath);
                utils.batchExecManager(listener, step.getExecutionScript(), step.getExecutionShellLocation(), step.getExecutionVsifFile(),buildId, buildNumber, launcher);
                sessionNames = utils.loadDataFromInputFiles(buildId, buildNumber, "" + workspace, "", listener, false, "session names", "sessions.input");
                if (sessionNames.length == 0) {
                    listener.getLogger().println("No session were found within sessions.input file.  Exit Job.\n");
                    throw new Exception("Failed to launch sessions for build " + buildId + " " + buildNumber + "\n"); //false;
                }
                
                
            } else {

                if ("static".equals(step.getVsifType())) {
                    listener.getLogger().println("The VSIF file chosen is static. VSIF file static location is: '" + step.getVSIFName() + "'");
                    vsifFileNames = new String[1];
                    vsifFileNames[0] = step.getVSIFName();
                } else {
                    if (step.getVSIFInputFile() == null || step.getVSIFInputFile().trim().equals("")) {
                        //listener.getLogger().println("The VSIF file chosen is dynamic. VSIF directory dynamic workspace directory: '" + build.getWorkspace() + "'");
                    } else {
                        listener.getLogger().println("The VSIF file chosen is static. VSIF file name is: '" + step.getVSIFInputFile().trim() + "'");
                    }

                    vsifFileNames = utils.loadDataFromInputFiles(buildId, buildNumber, "" + workspace, step.getVSIFInputFile(), listener, step.isDeleteInputFile(), "VSIF", "vsif.input");

                }

                //check if user set an environment variables in addition:
                if (step.isEnvVarible()) {
                    if (step.getEnvVaribleFile() == null || step.getEnvVaribleFile().trim().equals("")) {
                        listener.getLogger().println("The environment varible file chosen is dynamic. Env File directory dynamic workspace directory: '" + workspace + "'");
                    } else {
                        listener.getLogger().println("The environment varible file chosen is static. Environment file name is: '" + step.getEnvVaribleFile().trim() + "'");
                    }
                    jsonEnvInput = utils.loadJSONEnvInput(buildId, buildNumber, "" + workspace, step.getEnvVaribleFile(), listener);
                    listener.getLogger().println("Found the following environment for the vsif: " + jsonEnvInput);
                }

                //check if user set an attribute values in addition:
                if (step.isAttrValues()) {
                    if (step.getAttrValuesFile() == null || step.getAttrValuesFile().trim().equals("")) {
                        listener.getLogger().println("The attribute values file chosen is dynamic. Attribute values File directory dynamic workspace directory: '" + workspace + "'");
                    } else {
                        listener.getLogger().println("The attribute values file chosen is static. Attribute values file name is: '" + step.getAttrValuesFile().trim() + "'");
                    }
                    jsonAttrValuesInput = utils.loadJSONAttrValuesInput(buildId, buildNumber, "" + workspace, step.getAttrValuesFile(), listener);
                    listener.getLogger().println("Found the following attribute values for the vsif: " + jsonAttrValuesInput);
                }
                
                //check if user set an define values in addition:
                if (step.isDefineVarible()) {
                    if (step.getDefineVaribleFile() == null || step.getDefineVaribleFile().trim().equals("")) {
                        listener.getLogger().println("The define values file chosen is dynamic. Define values File directory dynamic workspace directory: '" + workspace + "'");
                    } else {
                        listener.getLogger().println("The define values file chosen is static. Define values file name is: '" + step.getDefineVaribleFile().trim() + "'");
                    }
                    jsonDefineInput = utils.loadJSONDefineInput(buildId, buildNumber, "" + workspace, step.getDefineVaribleFile(), listener);
                    listener.getLogger().println("Found the following define values for the vsif: " + jsonDefineInput);
                }

                if ("dynamic".equals(step.getUserFarmType())) {
                    if (step.getCredentialInputFile() == null || step.getCredentialInputFile().trim().equals("")) {
                        listener.getLogger().println("The credential file chosen is dynamic. Credential directory dynamic workspace directory: '" + workspace + "'");
                    } else {
                        listener.getLogger().println("The credential file chosen is static. Credential file name is: '" + step.getCredentialInputFile().trim() + "'");
                    }
                    farmUserPassword = utils.loadFileCredentials(buildId, buildNumber, "" + workspace, step.getCredentialInputFile(), listener, step.isDeleteCredentialInputFile());

                    //Tal Yanai
                    //In case this is a private user SSH, use the dynamic information for the vAPI login as well
                    if (step.isUserPrivateSSHKey()) {
                        tempUser = farmUserPassword[0];
                        tempPassword = farmUserPassword[1];
                    }
                }

            }

            // Now call the actual launch
            // ----------------------------------------------------------------------------------------------------------------
            String output = utils.executeVSIFLaunch(vsifFileNames, step.getVAPIUrl(), step.isAuthRequired(), tempUser, tempPassword, listener, step.isDynamicUserId(), buildId, buildNumber,
                    "" + workspace, step.getConnTimeout(), step.getReadTimeout(), step.isAdvConfig(), jsonEnvInput, step.isUseUserOnFarm(), step.getUserFarmType(), farmUserPassword, stepHolder, step.getEnvSourceInputFile(), workingJobDir, vMGRBuildArchiver, step.isUserPrivateSSHKey(), jsonAttrValuesInput, tmpExecutionType, sessionNames, step.getEnvSourceInputFileType(), launcher, jsonDefineInput);
            if (!"success".equals(output)) {
                listener.getLogger().println("Failed to launch vsifs for build " + buildId + " " + buildNumber + "\n");
                listener.getLogger().println(output + "\n");
                throw new Exception("Failed to launch vsifs for build " + buildId + " " + buildNumber + "\n"); //false;
            }
            // ----------------------------------------------------------------------------------------------------------------

        } catch (Exception e) {
            //listener.getLogger().println("Failed to build " + build.getId() + " " + build.getNumber());
            listener.getLogger().println(e.getMessage());
            for (StackTraceElement ste : e.getStackTrace()) {
                listener.getLogger().println(" " + ste);
            }

            throw e; //"false;

        }

        return null; //true
    }
}
