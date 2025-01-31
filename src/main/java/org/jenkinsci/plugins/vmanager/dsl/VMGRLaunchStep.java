package org.jenkinsci.plugins.vmanager.dsl;

import com.google.common.collect.ImmutableSet;
import hudson.EnvVars;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.Util;
import hudson.model.AbstractProject;
import hudson.model.Descriptor;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import jakarta.servlet.ServletException;
import net.sf.json.JSONObject;
import org.jenkinsci.plugins.vmanager.Utils;
import org.kohsuke.stapler.DataBoundConstructor;
import org.jenkinsci.plugins.workflow.steps.Step;
import org.jenkinsci.plugins.workflow.steps.StepContext;
import org.jenkinsci.plugins.workflow.steps.StepDescriptor;
import org.jenkinsci.plugins.workflow.steps.StepExecution;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest2;

public class VMGRLaunchStep extends Step {

    private final String vAPIUrl;
    private boolean authRequired;
    private boolean advConfig;
    private final String vAPIUser;
    private final String vAPIPassword;
    private final String vSIFName;
    private final String vSIFInputFile;
    private String credentialInputFile;
    private boolean deleteInputFile;
    private boolean deleteCredentialInputFile;
    private boolean useUserOnFarm;
    private boolean dynamicUserId;

    private String vsifType;
    private String userFarmType;
    private String envSourceInputFile;

    private int connTimeout = 1;
    private int readTimeout = 30;
    private boolean envVarible;
    private String envVaribleFile;
    private boolean attrValues;
    private String attrValuesFile;

    private boolean pipelineNodes;
    private String masterWorkspaceLocation;

    private String envSourceInputFileType = "BSH";
    private String inaccessibleResolver = "fail";
    private String stoppedResolver = "fail";
    private String failedResolver = "fail";
    private String doneResolver = "ignore";
    private String suspendedResolver = "ignore";
    private boolean waitTillSessionEnds;
    private int stepSessionTimeout = 30;

    private boolean generateJUnitXML;
    private boolean extraAttributesForFailures;
    private String staticAttributeList;
    private boolean markBuildAsFailedIfAllRunFailed;
    private boolean failJobIfAllRunFailed;
    private boolean markBuildAsPassedIfAllRunPassed;
    private boolean failJobUnlessAllRunPassed;
    private boolean userPrivateSSHKey;

    private final boolean vMGRBuildArchive;
    private boolean deleteAlsoSessionDirectory;
    private boolean genericCredentialForSessionDelete;
    private String archiveUser;
    private String archivePassword;

    private String famMode;
    private String famModeLocation;
    private boolean noAppendSeed;

    private final String executionType;
    private String sessionsInputFile;
    private boolean deleteSessionInputFile;
    private boolean pauseSessionOnBuildInterruption;

    private String executionScript;
    private String executionShellLocation;
    private String executionVsifFile;

    private String defineVaribleFile;
    private boolean defineVarible;
    private String defineVariableType;
    private String defineVariableText;

    
    @DataBoundConstructor
    public VMGRLaunchStep(String vAPIUrl, String vAPIUser, String vAPIPassword, String executionType, boolean vMGRBuildArchive, String vSIFName, String vSIFInputFile) {

        this.vAPIUrl = vAPIUrl;
        this.vAPIUser = vAPIUser;
        this.vAPIPassword = vAPIPassword;
        this.executionType = executionType;
        this.vMGRBuildArchive = vMGRBuildArchive;
        this.vSIFName = vSIFName;
        this.vSIFInputFile = vSIFInputFile;

    }

    
    @Deprecated
    public VMGRLaunchStep(String vAPIUrl, String vAPIUser, String vAPIPassword, String vSIFName, String vSIFInputFile, String credentialInputFile, boolean deleteInputFile, boolean deleteCredentialInputFile, boolean useUserOnFarm, boolean authRequired, String vsifType, String userFarmType,
            boolean dynamicUserId, boolean advConfig, int connTimeout, int readTimeout, boolean envVarible, String envVaribleFile, String inaccessibleResolver, String stoppedResolver, String failedResolver, String doneResolver, String suspendedResolver, boolean waitTillSessionEnds, int stepSessionTimeout,
            boolean generateJUnitXML, boolean extraAttributesForFailures, String staticAttributeList, boolean markBuildAsFailedIfAllRunFailed, boolean failJobIfAllRunFailed, String envSourceInputFile, boolean vMGRBuildArchive, boolean deleteAlsoSessionDirectory, boolean genericCredentialForSessionDelete,
            String archiveUser, String archivePassword, String famMode, String famModeLocation, boolean noAppendSeed, boolean pipelineNodes, String masterWorkspaceLocation, boolean markBuildAsPassedIfAllRunPassed, boolean failJobUnlessAllRunPassed, boolean userPrivateSSHKey, boolean attrValues,
            String attrValuesFile, String executionType, String sessionsInputFile, boolean deleteSessionInputFile, boolean pauseSessionOnBuildInterruption, String envSourceInputFileType, String executionScript, String executionShellLocation, String executionVsifFile, String defineVaribleFile, boolean defineVarible, String defineVariableType, String defineVariableText) {
        this.vAPIUrl = vAPIUrl;
        this.vAPIUser = vAPIUser;
        this.vAPIPassword = vAPIPassword;
        this.vSIFName = vSIFName;
        this.vSIFInputFile = vSIFInputFile;
        this.credentialInputFile = credentialInputFile;
        this.authRequired = authRequired;
        this.advConfig = advConfig;
        this.envVarible = envVarible;
        this.attrValues = attrValues;
        this.deleteInputFile = deleteInputFile;
        this.deleteCredentialInputFile = deleteCredentialInputFile;
        this.useUserOnFarm = useUserOnFarm;
        this.vsifType = vsifType;
        this.userFarmType = userFarmType;
        this.dynamicUserId = dynamicUserId;
        this.envVaribleFile = envVaribleFile;
        this.attrValuesFile = attrValuesFile;
        this.pipelineNodes = pipelineNodes;
        this.masterWorkspaceLocation = masterWorkspaceLocation;
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
        this.pauseSessionOnBuildInterruption = pauseSessionOnBuildInterruption;

        this.executionScript = executionScript;
        this.executionShellLocation = executionShellLocation;
        this.executionVsifFile = executionVsifFile;

        this.defineVaribleFile = defineVaribleFile;
        this.defineVarible = defineVarible;
        this.defineVariableType = defineVariableType;
        this.defineVariableText = defineVariableText;
    }

    /**
     * We'll use this from the
     * <p>
     * config.jelly</p>.
     * @param connTimeout
     */
    @DataBoundSetter
    public void setConnTimeout(int connTimeout) {
        this.connTimeout = connTimeout;
    }

    @DataBoundSetter
    public void setReadTimeout(int readTimeout) {
        this.readTimeout = readTimeout;
    }

    @DataBoundSetter
    public void setStepSessionTimeout(int stepSessionTimeout) {
        this.stepSessionTimeout = stepSessionTimeout;
    }

    @DataBoundSetter
    public void setVsifType(@CheckForNull String vsifType) {
        this.vsifType = Util.fixEmpty(vsifType);
    }

    @DataBoundSetter
    public void setDefineVariableType(@CheckForNull String defineVariableType) {
        this.defineVariableType = Util.fixEmpty(defineVariableType);
    }

    @DataBoundSetter
    public void setUserFarmType(@CheckForNull String userFarmType) {
        this.userFarmType = Util.fixEmpty(userFarmType);
    }

    @DataBoundSetter
    public void setFamMode(@CheckForNull String famMode) {
        this.famMode = Util.fixEmpty(famMode);
    }

    @DataBoundSetter
    public void setMasterWorkspaceLocation(@CheckForNull String masterWorkspaceLocation) {
        this.masterWorkspaceLocation = Util.fixEmpty(masterWorkspaceLocation);
    }

    @DataBoundSetter
    public void setDefineVariableText(@CheckForNull String defineVariableText) {
        this.defineVariableText = Util.fixEmpty(defineVariableText);
    }

    @DataBoundSetter
    public void setDeleteAlsoSessionDirectory(boolean deleteAlsoSessionDirectory) {
        this.deleteAlsoSessionDirectory = deleteAlsoSessionDirectory;
    }

    @DataBoundSetter
    public void setDeleteInputFile(boolean deleteInputFile) {
        this.deleteInputFile = deleteInputFile;
    }

    @DataBoundSetter
    public void setDeleteCredentialInputFile(boolean deleteCredentialInputFile) {
        this.deleteCredentialInputFile = deleteCredentialInputFile;
    }

    @DataBoundSetter
    public void setUseUserOnFarm(boolean useUserOnFarm) {
        this.useUserOnFarm = useUserOnFarm;
    }

    @DataBoundSetter
    public void setAuthRequired(boolean authRequired) {
        this.authRequired = authRequired;
    }

    @DataBoundSetter
    public void setDynamicUserId(boolean dynamicUserId) {
        this.dynamicUserId = dynamicUserId;
    }

    @DataBoundSetter
    public void setAdvConfig(boolean advConfig) {
        this.advConfig = advConfig;
    }

    @DataBoundSetter
    public void setDefineVarible(boolean defineVarible) {
        this.defineVarible = defineVarible;
    }

    @DataBoundSetter
    public void setExecutionScript(@CheckForNull String executionScript) {
        this.executionScript = Util.fixEmpty(executionScript);
    }

    @DataBoundSetter
    public void setDefineVaribleFile(@CheckForNull String defineVaribleFile) {
        this.defineVaribleFile = Util.fixEmpty(defineVaribleFile);
    }

    @DataBoundSetter
    public void setExecutionShellLocation(@CheckForNull String executionShellLocation) {
        this.executionShellLocation = Util.fixEmpty(executionShellLocation);
    }

    @DataBoundSetter
    public void setExecutionVsifFile(@CheckForNull String executionVsifFile) {
        this.executionVsifFile = Util.fixEmpty(executionVsifFile);
    }

    @DataBoundSetter
    public void setMarkBuildAsFailedIfAllRunFailed(boolean markBuildAsFailedIfAllRunFailed) {
        this.markBuildAsFailedIfAllRunFailed = markBuildAsFailedIfAllRunFailed;
    }

    @DataBoundSetter
    public void setFailJobIfAllRunFailed(boolean failJobIfAllRunFailed) {
        this.failJobIfAllRunFailed = failJobIfAllRunFailed;
    }

    @DataBoundSetter
    public void setGenericCredentialForSessionDelete(boolean genericCredentialForSessionDelete) {
        this.genericCredentialForSessionDelete = genericCredentialForSessionDelete;
    }

    @DataBoundSetter
    public void setnoAppendSeed(boolean noAppendSeed) {
        this.noAppendSeed = noAppendSeed;
    }

    @DataBoundSetter
    public void setPipelineNodes(boolean pipelineNodes) {
        this.pipelineNodes = pipelineNodes;
    }

    @DataBoundSetter
    public void setMarkBuildAsPassedIfAllRunPassed(boolean markBuildAsPassedIfAllRunPassed) {
        this.markBuildAsPassedIfAllRunPassed = markBuildAsPassedIfAllRunPassed;
    }

    @DataBoundSetter
    public void setFailJobUnlessAllRunPassed(boolean failJobUnlessAllRunPassed) {
        this.failJobUnlessAllRunPassed = failJobUnlessAllRunPassed;
    }

    @DataBoundSetter
    public void setUserPrivateSSHKey(boolean userPrivateSSHKey) {
        this.userPrivateSSHKey = userPrivateSSHKey;
    }

    @DataBoundSetter
    public void setAttrValues(boolean attrValues) {
        this.attrValues = attrValues;
    }

    @DataBoundSetter
    public void setEnvVarible(boolean envVarible) {
        this.envVarible = envVarible;
    }

    @DataBoundSetter
    public void setWaitTillSessionEnds(boolean waitTillSessionEnds) {
        this.waitTillSessionEnds = waitTillSessionEnds;
    }

    @DataBoundSetter
    public void setGenerateJUnitXML(boolean generateJUnitXML) {
        this.generateJUnitXML = generateJUnitXML;
    }

    @DataBoundSetter
    public void setDeleteSessionInputFile(boolean deleteSessionInputFile) {
        this.deleteSessionInputFile = deleteSessionInputFile;
    }

    @DataBoundSetter
    public void setPauseSessionOnBuildInterruption(boolean pauseSessionOnBuildInterruption) {
        this.pauseSessionOnBuildInterruption = pauseSessionOnBuildInterruption;
    }

    @DataBoundSetter
    public void setExtraAttributesForFailures(boolean extraAttributesForFailures) {
        this.extraAttributesForFailures = extraAttributesForFailures;
    }


    @DataBoundSetter
    public void setCredentialInputFile(@CheckForNull String credentialInputFile) {
        this.credentialInputFile = Util.fixEmpty(credentialInputFile);
    }

    @DataBoundSetter
    public void setEnvVaribleFile(@CheckForNull String envVaribleFile) {
        this.envVaribleFile = Util.fixEmpty(envVaribleFile);
    }

    @DataBoundSetter
    public void setStaticAttributeList(@CheckForNull String staticAttributeList) {
        this.staticAttributeList = Util.fixEmpty(staticAttributeList);
    }

    @DataBoundSetter
    public void setAttrValuesFile(@CheckForNull String attrValuesFile) {
        this.attrValuesFile = Util.fixEmpty(attrValuesFile);
    }

    @DataBoundSetter
    public void setSessionsInputFile(@CheckForNull String sessionsInputFile) {
        this.sessionsInputFile = Util.fixEmpty(sessionsInputFile);
    }

    @DataBoundSetter
    public void setInaccessibleResolver(@CheckForNull String inaccessibleResolver) {
        this.inaccessibleResolver = Util.fixEmpty(inaccessibleResolver);

    }

    @DataBoundSetter
    public void setStoppedResolver(@CheckForNull String stoppedResolver) {
        this.stoppedResolver = Util.fixEmpty(stoppedResolver);

    }

    @DataBoundSetter
    public void setFailedResolver(@CheckForNull String failedResolver) {
        this.failedResolver = Util.fixEmpty(failedResolver);

    }

    @DataBoundSetter
    public void setDoneResolver(@CheckForNull String doneResolver) {
        this.doneResolver = Util.fixEmpty(doneResolver);

    }

    @DataBoundSetter
    public void setSuspendedResolver(@CheckForNull String suspendedResolver) {
        this.suspendedResolver = Util.fixEmpty(suspendedResolver);

    }

    @DataBoundSetter
    public void setEnvSourceInputFileType(@CheckForNull String envSourceInputFileType) {
        this.envSourceInputFileType = Util.fixEmpty(envSourceInputFileType);
    }

    @DataBoundSetter
    public void setEnvSourceInputFile(@CheckForNull String envSourceInputFile) {
        this.envSourceInputFile = Util.fixEmpty(envSourceInputFile);
    }

    @DataBoundSetter
    public void setArchiveUser(@CheckForNull String archiveUser) {
        this.archiveUser = Util.fixEmpty(archiveUser);
    }

    @DataBoundSetter
    public void setArchivePassword(@CheckForNull String archivePassword) {
        this.archivePassword = Util.fixEmpty(archivePassword);
    }

    @DataBoundSetter
    public void setFamModeLocation(@CheckForNull String famModeLocation) {
        this.famModeLocation = Util.fixEmpty(famModeLocation);
    }

    public String getExecutionScript() {
        return executionScript;
    }

    public String getExecutionShellLocation() {
        return executionShellLocation;
    }

    public String getExecutionVsifFile() {
        return executionVsifFile;
    }

    public String getSessionsInputFile() {
        return sessionsInputFile;
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

    public boolean isNoAppendSeed() {
        return noAppendSeed;
    }

    public boolean isPauseSessionOnBuildInterruption() {
        return pauseSessionOnBuildInterruption;
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

    public String getMasterWorkspaceLocation() {
        return masterWorkspaceLocation;
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

    public boolean isPipelineNodes() {
        return pipelineNodes;
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

    @Override
    public StepExecution start(StepContext context) throws Exception {
        return new VMGRLaunchStepImpl(this, context);
    }

    @Extension
    public static class DescriptorImpl extends StepDescriptor {

        public DescriptorImpl() {
            load();
        }

        @Override
        public String getFunctionName() {
            return "vmanagerLaunch";
        }

        @Nonnull
        @Override
        public String getDisplayName() {
            return "Cadence vManager Session Launcher";
        }

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

        @Override
        public boolean configure(StaplerRequest2 req, JSONObject formData) throws Descriptor.FormException {

            save();
            return super.configure(req, formData);
        }

        public FormValidation doTestConnection(@QueryParameter("vAPIUser") final String vAPIUser, @QueryParameter("vAPIPassword") final String vAPIPassword,
                @QueryParameter("vAPIUrl") final String vAPIUrl, @QueryParameter("authRequired") final boolean authRequired) throws IOException,
                ServletException {
            try {

                Utils utils = new Utils();
                String output = utils.checkVAPIConnection(vAPIUrl, authRequired, vAPIUser, vAPIPassword);
                if (!output.startsWith("Failed")) {
                    return FormValidation.ok("Success. " + output);
                } else {
                    return FormValidation.error(output);
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

        @Override
        public Set<Class<?>> getRequiredContext() {
            //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            return ImmutableSet.of(FilePath.class, Run.class, Launcher.class, TaskListener.class, EnvVars.class);
        }

    }
}
