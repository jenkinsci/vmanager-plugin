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
package org.jenkinsci.plugins.vmanager;

import hudson.model.Run;
import hudson.plugins.vmanager.BuildStatusMap;
import java.text.DateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author tyanai
 */
public class PostActionBase {

    public static final int numberOfBuilds = 15;
    public VMGRRun vmgrRun;

    public String getTimestampSortData(VMGRRun run) {

        return String.valueOf(run.getRun().getTimeInMillis());
    }

    public String getBuildOwner(VMGRRun run) {

        // This is the only time we set true.  It means to load inot the hashmap for each line, the rest of the get
        // methods will use the hashmap

        // return BuildStatusMap.getValue(run.getId(), run.getNumber(), run.getWorkspace()+"", "owner", true);
        return BuildStatusMap.getValue(
                run.getRun().getId(), run.getRun().getNumber(), run.getJobWorkingDir() + "", "owner", true);
    }

    public String getSessionTriage(VMGRRun run) {

        String output = "#";
        String sessionId = BuildStatusMap.getValue(
                run.getRun().getId(), run.getRun().getNumber(), run.getJobWorkingDir() + "", "id", false);

        // If id is having more than 1 sesison, set url to internal list of sessions, not to the vManager outside
        // regression
        if (sessionId.indexOf(",") > 0) {
            output = "../" + run.getRun().getNumber() + "/vManagerSessionsView";
        } else {
            String url = BuildStatusMap.getValue(
                    run.getRun().getId(), run.getRun().getNumber(), run.getJobWorkingDir() + "", "url", false);
            if ("NA".equals(url)) {
                return "NA";
            }
            output = Utils.getRegressionURLFromVAPIURL(url) + "?sessionid=" + sessionId;
        }

        return output;
    }

    public String getSessionStatus(VMGRRun run) {
        return BuildStatusMap.getValue(
                run.getRun().getId(), run.getRun().getNumber(), run.getJobWorkingDir() + "", "status", false);
    }

    public String getSessionName(VMGRRun run) {
        String sessionName = BuildStatusMap.getValue(
                run.getRun().getId(), run.getRun().getNumber(), run.getJobWorkingDir() + "", "name", false);
        if (!"NA".equals(sessionName)) {
            String sessionCode = BuildStatusMap.getValue(
                    run.getRun().getId(), run.getRun().getNumber(), run.getJobWorkingDir() + "", "session_code", false);
            sessionName = sessionName + " (" + sessionCode + ")";
        }
        return sessionName;
    }

    public String getTotalRuns(VMGRRun run) {
        return BuildStatusMap.getValue(
                run.getRun().getId(),
                run.getRun().getNumber(),
                run.getJobWorkingDir() + "",
                "total_runs_in_session",
                false);
    }

    public String getPassedRuns(VMGRRun run) {
        return BuildStatusMap.getValue(
                run.getRun().getId(), run.getRun().getNumber(), run.getJobWorkingDir() + "", "passed_runs", false);
    }

    public String getFailedRuns(VMGRRun run) {
        return BuildStatusMap.getValue(
                run.getRun().getId(), run.getRun().getNumber(), run.getJobWorkingDir() + "", "failed_runs", false);
    }

    public String getOtherRuns(VMGRRun run) {
        return BuildStatusMap.getValue(
                run.getRun().getId(), run.getRun().getNumber(), run.getJobWorkingDir() + "", "other_runs", false);
    }

    public String getRunningRuns(VMGRRun run) {
        return BuildStatusMap.getValue(
                run.getRun().getId(), run.getRun().getNumber(), run.getJobWorkingDir() + "", "running", false);
    }

    public String getWaitingRuns(VMGRRun run) {
        return BuildStatusMap.getValue(
                run.getRun().getId(), run.getRun().getNumber(), run.getJobWorkingDir() + "", "waiting", false);
    }

    public String getTotalSessions(VMGRRun run) {
        return BuildStatusMap.getValue(
                run.getRun().getId(),
                run.getRun().getNumber(),
                run.getJobWorkingDir() + "",
                "number_of_entities",
                false);
    }

    public String getTimestampString(VMGRRun run) {

        return DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM)
                .format(new Date(run.getRun().getTimeInMillis()));
    }

    public String getBuildColumnSortData(Run<?, ?> build) {

        return String.valueOf(build.getNumber());
    }

    public String getSessionName(String sessionId) {
        String idNames = BuildStatusMap.getValue(
                vmgrRun.getRun().getId(),
                vmgrRun.getRun().getNumber(),
                vmgrRun.getJobWorkingDir() + "",
                "idNames",
                false);

        if (!idNames.equals("NA")) {
            List<String> items = Arrays.asList(idNames.split("\\s*,\\s*"));
            Iterator<String> iter = items.iterator();
            while (iter.hasNext()) {
                String item = iter.next();
                if (sessionId.equals(item.substring(0, item.indexOf("$@$")))) {
                    return item.substring(item.indexOf("$@$") + 3, item.length());
                }
            }
        }

        return "NO_NAME";
    }

    public String getSessionLinkForBuild(String sessionId) {
        // String url = BuildStatusMap.getValue(run.getId(), run.getNumber(), run.getWorkspace()+"", "url", false);
        String url = BuildStatusMap.getValue(
                vmgrRun.getRun().getId(), vmgrRun.getRun().getNumber(), vmgrRun.getJobWorkingDir() + "", "url", false);
        if ("NA".equals(url)) {
            return "NA";
        }
        url = url.replaceAll("/vapi", "");

        return url + "/regression/index.html?sessionid=" + sessionId;
    }
}
