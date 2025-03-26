package org.jenkinsci.plugins.vmanager.dsl.post;

import hudson.model.Action;
import hudson.model.Job;
import hudson.model.Run;
import hudson.plugins.vmanager.BuildStatusMap;
import java.io.File;
import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import jenkins.model.RunAction2;
import jenkins.tasks.SimpleBuildStep;
import org.jenkinsci.plugins.vmanager.PostActionBase;
import org.jenkinsci.plugins.vmanager.Utils;
import org.jenkinsci.plugins.vmanager.VMGRRun;


public class DSLBuildAction extends PostActionBase implements Serializable, RunAction2,  SimpleBuildStep.LastBuildAction  {

    private static final long serialVersionUID = 2000009076155338047L;
    
    private String message;
    private transient Run<?, ?> build;
    private final List projectActions;

    @Override
    public String getIconFileName() {
        return "symbol-link-outline plugin-ionicons-api";
    }

    @Override
    public String getDisplayName() {
        return "Verisium Manager Session Links";
    }

    @Override
    public String getUrlName() {
        return "vManagerSessionsView";
    }

    public String getMessage() {
        return this.message;
    }
    
    public List<String> getJobSessions() {
        
         
        Job job = build.getParent();
        String workingDir = job.getBuildDir() + File.separator + build.getNumber();
        vmgrRun = new VMGRRun(build,workingDir,job.getBuildDir().getAbsolutePath());
        
        String ids = BuildStatusMap.getValue(vmgrRun.getRun().getId(), vmgrRun.getRun().getNumber(), vmgrRun.getJobWorkingDir() + "", "id", true);
        List<String> items = Arrays.asList(ids.split("\\s*,\\s*"));

        return items;
    }
    
    public String getvManagerLink(boolean buildLevel) {
        String output = "#";
        //Check if to add a direct link for last succesfull build
        //Run lastSuccesfullRun = project.getLastSuccessfulBuild();
        if (this.build != null){
            Job job = this.build.getParent();
            String workingDir = job.getBuildDir() + File.separator + this.build.getNumber();
            VMGRRun tmpVmgrRun = new VMGRRun(this.build,workingDir,job.getBuildDir().getAbsolutePath());
            String id = BuildStatusMap.getValue(tmpVmgrRun.getRun().getId(), tmpVmgrRun.getRun().getNumber(), tmpVmgrRun.getJobWorkingDir() + "", "id", true);
           
            //If id is having more than 1 sesison, set url to internal list of sessions, not to the vManager outside regression
            if (id.indexOf(",") > 0){
                if (buildLevel){
                    output = "vManagerSessionsView";
                } else {
                    output = this.build.getNumber() + "/vManagerSessionsView";
                }
            } else {
                String vAPIUrl = BuildStatusMap.getValue(tmpVmgrRun.getRun().getId(), tmpVmgrRun.getRun().getNumber(), tmpVmgrRun.getJobWorkingDir() + "", "url", false);
                output = Utils.getRegressionURLFromVAPIURL(vAPIUrl) + "?sessionid=" + id;
            }
            
        } 
        
        return output;
        
        
    }

    public int getBuildNumber() {
        return this.build.number;
    }

    public Run<?, ?> getBuild() {
        return build;
    }

    public DSLBuildAction(final String message, final Run<?, ?> build)
    {
        this.message = message;
        this.build = build;
                
        List<DSLProjectAction> tmpProjectActions = new ArrayList<>();
        tmpProjectActions.add(new DSLProjectAction(build.getParent()));
        this.projectActions = tmpProjectActions;
    }

    @Override
    public Collection<? extends Action> getProjectActions() {
        return this.projectActions;
    }

    @Override
    public void onAttached(Run<?, ?> run) {
        this.build = run;
    }

    @Override
    public void onLoad(Run<?, ?> run) {
        this.build = run;
    }
    
    
    
}
