package org.jenkinsci.plugins.vmanager.dsl.post;

import hudson.FilePath;
import hudson.Launcher;
import hudson.model.Run;
import hudson.model.TaskListener;
import java.io.*;
import jenkins.model.RunAction2;
import org.jenkinsci.plugins.vmanager.PostActionBase;
import org.jenkinsci.plugins.vmanager.ReportManager;
import org.jenkinsci.plugins.vmanager.SummaryReportParams;
import org.jenkinsci.plugins.vmanager.VAPIConnectionParam;

public class ReportBuildAction extends PostActionBase implements Serializable, RunAction2 {
    @Serial
    private static final long serialVersionUID = 1L;

    private transient Run<?, ?> build;
    private transient TaskListener listener;
    private transient ReportManager reportManager;

    @Override
    public String getIconFileName() {
        return "/plugin/vmanager-plugin/img/report.png";
    }

    @Override
    public String getDisplayName() {
        return "Verisium Manager Summary Report";
    }

    @Override
    public String getUrlName() {
        return "vManagerSummaryReport";
    }

    public int getBuildNumber() {
        return this.build.number;
    }

    public Run<?, ?> getBuild() {
        return build;
    }

    public ReportBuildAction(
            final Run<?, ?> build,
            SummaryReportParams summaryReportParams,
            VAPIConnectionParam vAPIConnectionParam,
            TaskListener listener,
            FilePath filePath,
            Launcher launcher) {

        this.build = build;
        this.listener = listener;

        // listener.getLogger().println("Setting FilePath as: " + filePath);
        // listener.getLogger().println("Setting secondary FilePath as: " + fp);
        this.reportManager = new ReportManager(build, summaryReportParams, vAPIConnectionParam, listener, filePath);
        try {
            boolean isStreaming = false;
            if ("stream".equals(summaryReportParams.vManagerVersion)) {
                isStreaming = true;
            }
            this.reportManager.retrievReportFromServer(isStreaming, launcher);
            this.reportManager.emailSummaryReport();
        } catch (Exception e) {
            listener.getLogger().println("Failed to get summary report from server");
            e.printStackTrace();
        }
    }

    @Override
    public void onAttached(Run<?, ?> run) {
        this.build = run;
    }

    @Override
    public void onLoad(Run<?, ?> run) {
        this.build = run;
    }

    public String getReportFromWorkspace() {

        ReportManager reportManager = new ReportManager(build, null, null, listener, null);

        return reportManager.getReportFromWorkspace();
    }

    @Serial
    private void writeObject(ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();
        // Handle serialization of transient fields if necessary
    }

    @Serial
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        // Restore transient fields
        this.listener = TaskListener.NULL; // or any other default value or logic to restore the listener
    }
}
