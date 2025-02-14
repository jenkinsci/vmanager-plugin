package org.jenkinsci.plugins.vmanager.dsl.post;

import hudson.model.Action;
import hudson.model.Job;
import hudson.model.Run;
import java.io.File;
import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import org.jenkinsci.plugins.vmanager.PostActionBase;
import org.jenkinsci.plugins.vmanager.VMGRRun;

public class DSLProjectAction extends PostActionBase implements Serializable, Action {
    @Serial
    private static final long serialVersionUID = 1L;

    private final transient Job<?, ?> project;

    @Override
    public String getIconFileName() {
        return "/plugin/vmanager-plugin/img/project_icon.png";
    }

    @Override
    public String getDisplayName() {
        return "Verisium Manager Jobs Overview";
    }

    @Override
    public String getUrlName() {
        return "VMGRBuildView";
    }

    public Job<?, ?> getProject() {
        return this.project;
    }

    public String getProjectName() {
        if (this.project == null) {
            return "Error - Project name was not set yet.  Please run build at least once after a Jenkins restart";
        }

        return this.project.getName();
    }

    public List<VMGRRun> getFinishedVMGRBuilds() {
        List<VMGRRun> recentBuilds = new ArrayList<>();

        if (project == null) {
            return recentBuilds;
        }

        List<? extends Run<?, ?>> builds = project.getBuilds();
        final Class<DSLBuildAction> buildClass = DSLBuildAction.class;
        VMGRRun tmpVMGRRun;
        Job job;
        String workingDir;
        int counter = 0;
        for (Run<?, ?> currentBuild : builds) {
            if (counter == PostActionBase.numberOfBuilds) break;
            DSLBuildAction action = currentBuild.getAction(buildClass);
            if (action != null) {
                job = currentBuild.getParent();
                workingDir = job.getBuildDir().getAbsolutePath() + File.separator + currentBuild.getNumber();
                tmpVMGRRun =
                        new VMGRRun(currentBuild, workingDir, job.getBuildDir().getAbsolutePath());
                recentBuilds.add(tmpVMGRRun);
                counter++;
            }
        }

        return recentBuilds;
    }

    DSLProjectAction(final Job<?, ?> project) {
        this.project = project;
    }
}
