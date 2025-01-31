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
package org.jenkinsci.plugins.vmanager.dsl.post;

import com.google.common.collect.ImmutableSet;
import hudson.EnvVars;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import java.io.IOException;
import java.util.Set;
import javax.annotation.Nonnull;
import jakarta.servlet.ServletException;
import net.sf.json.JSONObject;
import org.jenkinsci.plugins.vmanager.Utils;
//import org.jenkinsci.plugins.workflow.steps.AbstractStepDescriptorImpl;
//import org.jenkinsci.plugins.workflow.steps.AbstractStepImpl;
import org.jenkinsci.plugins.workflow.steps.Step;
import org.jenkinsci.plugins.workflow.steps.StepContext;
import org.jenkinsci.plugins.workflow.steps.StepDescriptor;
import org.jenkinsci.plugins.workflow.steps.StepExecution;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest2;

/**
 *
 * @author tyanai
 */
public class VMGRPostLaunchStep extends Step {

    private String vAPIUrl;
    private boolean authRequired;
    private boolean advConfig;
    private String vAPIUser;
    private String vAPIPassword;
    private boolean dynamicUserId;
    private int connTimeout = 1;
    private int readTimeout = 30;

    private boolean advancedFunctions;
    private boolean retrieveSummaryReport;

    private boolean runReport;
    private boolean metricsReport;
    private boolean vPlanReport;

    private String testsViewName;
    private String metricsViewName;
    private String vplanViewName;
    private int testsDepth = 6;
    private int metricsDepth = 6;
    private int vPlanDepth = 6;

    private String metricsInputType;
    private String metricsAdvanceInput;
    private String vPlanInputType;
    private String vPlanAdvanceInput;
    private String vPlanxFileName;

    private String summaryType;
    private boolean ctxInput;
    private String ctxAdvanceInput;
    private String freeVAPISyntax;
    private boolean deleteReportSyntaxInputFile;
    private String vManagerVersion;
    private boolean sendEmail;
    private String emailList;
    private String emailType;
    private String emailInputFile;
    private boolean deleteEmailInputFile;
    private String summaryMode;
    private boolean ignoreSSLError;

    // Fields in config.jelly must match the parameter names in the "DataBoundConstructor"
    @DataBoundConstructor
    public VMGRPostLaunchStep(String vAPIUrl, String vAPIUser, String vAPIPassword, boolean authRequired, boolean advConfig, boolean dynamicUserId, int connTimeout, int readTimeout, boolean advancedFunctions,
            boolean retrieveSummaryReport, boolean runReport, boolean metricsReport, boolean vPlanReport, String testsViewName, String metricsViewName, String vplanViewName, int testsDepth, int metricsDepth,
            int vPlanDepth, String metricsInputType, String metricsAdvanceInput, String vPlanInputType, String vPlanAdvanceInput, String vPlanxFileName, String summaryType, boolean ctxInput,
            String ctxAdvanceInput, String freeVAPISyntax, boolean deleteReportSyntaxInputFile, String vManagerVersion, boolean sendEmail, String emailList, String emailType, String emailInputFile, boolean deleteEmailInputFile, String summaryMode, boolean ignoreSSLError) {

        this.vAPIUrl = vAPIUrl;
        this.authRequired = authRequired;
        this.advConfig = advConfig;
        this.vAPIUser = vAPIUser;
        this.vAPIPassword = vAPIPassword;
        this.dynamicUserId = dynamicUserId;
        this.connTimeout = connTimeout;
        this.readTimeout = readTimeout;
        this.advancedFunctions = advancedFunctions;
        this.retrieveSummaryReport = retrieveSummaryReport;
        this.runReport = runReport;
        this.metricsReport = metricsReport;
        this.vPlanReport = vPlanReport;
        this.testsViewName = testsViewName;
        this.metricsViewName = metricsViewName;
        this.vplanViewName = vplanViewName;
        this.testsDepth = testsDepth;
        this.metricsDepth = metricsDepth;
        this.vPlanDepth = vPlanDepth;

        this.metricsInputType = metricsInputType;
        this.metricsAdvanceInput = metricsAdvanceInput;
        this.vPlanInputType = vPlanInputType;
        this.vPlanAdvanceInput = vPlanAdvanceInput;
        this.vPlanxFileName = vPlanxFileName;
        this.summaryType = summaryType;
        this.ctxInput = ctxInput;
        this.ctxAdvanceInput = ctxAdvanceInput;
        this.freeVAPISyntax = freeVAPISyntax;
        this.deleteReportSyntaxInputFile = deleteReportSyntaxInputFile;

        this.vManagerVersion = vManagerVersion;
        this.sendEmail = sendEmail;
        this.emailList = emailList;
        this.emailType = emailType;
        this.emailInputFile = emailInputFile;
        this.deleteEmailInputFile = deleteEmailInputFile;
        this.summaryMode = summaryMode;
        this.ignoreSSLError = ignoreSSLError;

    }

    public String getSummaryMode() {
        return summaryMode;
    }

    public String getEmailType() {
        return emailType;
    }

    public String getEmailInputFile() {
        return emailInputFile;
    }

    public boolean isDeleteEmailInputFile() {
        return deleteEmailInputFile;
    }

    public boolean isIgnoreSSLError() {
        return ignoreSSLError;
    }

    public String getVManagerVersion() {
        return vManagerVersion;
    }

    public boolean isSendEmail() {
        return sendEmail;
    }

    public String getEmailList() {
        return emailList;
    }

    public boolean isDeleteReportSyntaxInputFile() {
        return deleteReportSyntaxInputFile;
    }

    public String getSummaryType() {
        return summaryType;
    }

    public boolean isCtxInput() {
        return ctxInput;
    }

    public String getCtxAdvanceInput() {
        return ctxAdvanceInput;
    }

    public String getFreeVAPISyntax() {
        return freeVAPISyntax;
    }

    public String getMetricsInputType() {
        return metricsInputType;
    }

    public String getMetricsAdvanceInput() {
        return metricsAdvanceInput;
    }

    public String getVPlanInputType() {
        return vPlanInputType;
    }

    public String getVPlanAdvanceInput() {
        return vPlanAdvanceInput;
    }

    public String getVPlanxFileName() {
        return vPlanxFileName;
    }

    public String getTestsViewName() {
        return testsViewName;
    }

    public String getMetricsViewName() {
        return metricsViewName;
    }

    public String getVplanViewName() {
        return vplanViewName;
    }

    public int getTestsDepth() {
        return testsDepth;
    }

    public int getMetricsDepth() {
        return metricsDepth;
    }

    public int getVPlanDepth() {
        return vPlanDepth;
    }

    public boolean isRunReport() {
        return runReport;
    }

    public boolean isMetricsReport() {
        return metricsReport;
    }

    public boolean isVPlanReport() {
        return vPlanReport;
    }

    public boolean isAdvancedFunctions() {
        return advancedFunctions;
    }

    public boolean isRetrieveSummaryReport() {
        return retrieveSummaryReport;
    }

    public String getVAPIUrl() {
        return vAPIUrl;
    }

    public boolean isAuthRequired() {
        return authRequired;
    }

    public boolean isAdvConfig() {
        return advConfig;
    }

    public String getVAPIUser() {
        return vAPIUser;
    }

    public String getVAPIPassword() {
        return vAPIPassword;
    }

    public boolean isDynamicUserId() {
        return dynamicUserId;
    }

    public int getConnTimeout() {
        return connTimeout;
    }

    public int getReadTimeout() {
        return readTimeout;
    }

    @Override
    public StepExecution start(StepContext context) throws Exception {
        return new VMGRPostLaunchStepImpl(this, context);
    }

    @Extension
    public static class DescriptorImpl extends StepDescriptor {

        public DescriptorImpl() {
            load();
        }

        @Override
        public String getFunctionName() {
            return "vmanagerPostBuildActions";
        }

        @Nonnull
        @Override
        public String getDisplayName() {
            return "vManager Post Build Actions";
        }

        @Override
        public boolean configure(StaplerRequest2 req, JSONObject formData) throws FormException {

            save();
            return super.configure(req, formData);
        }

        @Override
        public Set<Class<?>> getRequiredContext() {
            return ImmutableSet.of(FilePath.class, Run.class, Launcher.class, TaskListener.class, EnvVars.class);
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

        public ListBoxModel doFillVManagerVersionItems() {
            ListBoxModel items = new ListBoxModel();
            items.add("19.09 and above", "stream");
            items.add("Lower than 19.09", "html");
            return items;
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
    }
}
