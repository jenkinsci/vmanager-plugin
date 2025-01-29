package hudson.plugins.vmanager;

import hudson.Extension;
import hudson.model.Descriptor;
import hudson.model.Job;
import hudson.model.Run;
import hudson.plugins.view.dashboard.DashboardPortlet;
import java.io.File;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.kohsuke.stapler.DataBoundConstructor;

import java.util.PriorityQueue;
import org.jenkinsci.plugins.vmanager.Utils;
import org.jenkinsci.plugins.vmanager.VMGRRun;

public class VmgrBuilds extends DashboardPortlet {

    /**
     * Number of latest builds which will be displayed on the screen
     */
    private int numBuilds = 10;

    @DataBoundConstructor
    public VmgrBuilds(String name, int numBuilds) {
        super(name);
        this.numBuilds = numBuilds;

    }

    public int getNumBuilds() {
        return numBuilds <= 0 ? 10 : numBuilds; 
    }

    public String getTimestampSortData(VMGRRun run) {

        return String.valueOf(run.getRun().getTimeInMillis());
    }

    public String getBuildOwner(VMGRRun run) {

        //This is the only time we set true.  It means to load inot the hashmap for each line, the rest of the get methods will use the hashmap
        

        //return BuildStatusMap.getValue(run.getId(), run.getNumber(), run.getWorkspace()+"", "owner", true);
        return BuildStatusMap.getValue(run.getRun().getId(), run.getRun().getNumber(), run.getJobWorkingDir() + "", "owner", true);
    }

    public String getSessionTriage(VMGRRun run) {
        //String url = BuildStatusMap.getValue(run.getId(), run.getNumber(), run.getWorkspace()+"", "url", false);
        String url = BuildStatusMap.getValue(run.getRun().getId(), run.getRun().getNumber(), run.getJobWorkingDir() + "", "url", false);
        if ("NA".equals(url)) {
            return "NA";
        }
        
        String sessionId = BuildStatusMap.getValue(run.getRun().getId(), run.getRun().getNumber(), run.getJobWorkingDir() + "", "id", false);
        if (sessionId != null) {
            if (sessionId.indexOf(",") > -1) {
                
                sessionId = sessionId.substring(0, sessionId.indexOf(","));
            }
        }

        return Utils.getRegressionURLFromVAPIURL(url) + "?sessionid=" + sessionId;
    }

    public String getSessionStatus(VMGRRun run) {
        return BuildStatusMap.getValue(run.getRun().getId(), run.getRun().getNumber(), run.getJobWorkingDir() + "", "status", false);
    }

    public String getSessionName(VMGRRun run) {
        String sessionName = BuildStatusMap.getValue(run.getRun().getId(), run.getRun().getNumber(), run.getJobWorkingDir() + "", "name", false);
        if (!"NA".equals(sessionName)) {
            String sessionCode = BuildStatusMap.getValue(run.getRun().getId(), run.getRun().getNumber(), run.getJobWorkingDir() + "", "session_code", false);
            sessionName = sessionName + " (" + sessionCode + ")";
        }
        return sessionName;
    }

    public String getTotalRuns(VMGRRun run) {
        return BuildStatusMap.getValue(run.getRun().getId(), run.getRun().getNumber(), run.getJobWorkingDir() + "", "total_runs_in_session", false);
    }

    public String getPassedRuns(VMGRRun run) {
        return BuildStatusMap.getValue(run.getRun().getId(), run.getRun().getNumber(), run.getJobWorkingDir() + "", "passed_runs", false);
    }

    public String getFailedRuns(VMGRRun run) {
        return BuildStatusMap.getValue(run.getRun().getId(), run.getRun().getNumber(), run.getJobWorkingDir() + "", "failed_runs", false);
    }

    public String getOtherRuns(VMGRRun run) {
        return BuildStatusMap.getValue(run.getRun().getId(), run.getRun().getNumber(), run.getJobWorkingDir() + "", "other_runs", false);
    }

    public String getRunningRuns(VMGRRun run) {
        return BuildStatusMap.getValue(run.getRun().getId(), run.getRun().getNumber(), run.getJobWorkingDir() + "", "running", false);
    }

    public String getWaitingRuns(VMGRRun run) {
        return BuildStatusMap.getValue(run.getRun().getId(), run.getRun().getNumber(), run.getJobWorkingDir() + "", "waiting", false);
    }

    public String getTotalSessions(VMGRRun run) {
        
        return BuildStatusMap.getValue(run.getRun().getId(), run.getRun().getNumber(), run.getJobWorkingDir() + "", "number_of_entities", false);
    }

    public String getTimestampString(VMGRRun run) {

        return DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM).format(new Date(run.getRun().getTimeInMillis()));
    }


    public List<VMGRRun> getFinishedVMGRBuilds() {
        List<Job> jobs = getDashboardJobs();

        String workingDir = null;
        PriorityQueue<VMGRRun> queue = new PriorityQueue<VMGRRun>(numBuilds, VMGRRun.ORDER_BY_DATE);
        for (Job job : jobs) {

            Run lb = job.getLastBuild();
            if (lb != null) {
                workingDir = job.getBuildDir().getAbsolutePath() + File.separator + lb.getNumber();
                VMGRRun vMGRRun = null;
                //if (lb != null) {
                vMGRRun = new VMGRRun(lb, workingDir, job.getBuildDir().getAbsolutePath());
                queue.add(vMGRRun);
            }
        }

        List<VMGRRun> recentBuilds = new ArrayList<VMGRRun>(numBuilds);
        VMGRRun build;
        while ((build = queue.poll()) != null) {
            recentBuilds.add(build);
            if (recentBuilds.size() == numBuilds) {
                break;
            }

            Run pb = build.getRun().getPreviousBuild();
            if (pb != null) {
                workingDir = build.getGeneralWorkingDir() + File.separator + pb.getNumber();
                VMGRRun previusBuild = new VMGRRun(pb, workingDir, build.getGeneralWorkingDir());
                queue.add(previusBuild);
            }

        }

        return recentBuilds;
    }

    /**
     * for unit test
     * @return 
     */
    protected List<Job> getDashboardJobs() {
        return getDashboard().getJobs();
    }

    public String getBuildColumnSortData(Run<?, ?> build) {

        return String.valueOf(build.getNumber());
    }

    @Extension
    public static class DescriptorImpl extends Descriptor<DashboardPortlet> {

        @Override
        public String getDisplayName() {
            return "Verisium Manager Latest Sessions";
        }
    }

}
