package hudson.plugins.vmanager;

import java.util.HashMap;
import java.util.Map;
import org.jenkinsci.plugins.vmanager.SessionState;
import org.jenkinsci.plugins.vmanager.SessionStatusHolder;

public class BuildStatusMap {

    static Map<String, SessionState> buildMap = new HashMap<String, SessionState>();

    public static String getValue(
            String buildId, int buildNumber, String workingJobDir, String key, boolean firstTimeForAGivenLine) {

        String jobKey = workingJobDir + "." + buildNumber + "." + buildId;

        if (firstTimeForAGivenLine) {
            // System.out.println("Trying to load properties for build: " + jobKey );
            SessionStatusHolder sessionStatusHolder = new SessionStatusHolder(buildNumber, workingJobDir, buildId);
            SessionState sessionState = sessionStatusHolder.loadSessionFromFile();
            buildMap.put(jobKey, sessionState);
        }

        if (buildMap.get(jobKey) != null) {
            return getValue(key, buildMap.get(jobKey));

        } else {
            // System.out.println("Couldn't find a value for vManager dashboard for build " + buildId);
            return "NA";
        }
    }

    private static String getValue(String key, SessionState session) {

        if ("status".equals(key)) return session.getStatus();
        if ("name".equals(key)) return session.getName();
        if ("session_code".equals(key)) return session.getSessionCode();
        if ("total_runs_in_session".equals(key)) return session.getTotalRuns();
        if ("passed_runs".equals(key)) return session.getPassed();
        if ("failed_runs".equals(key)) return session.getFailed();
        if ("running".equals(key)) return session.getRunning();
        if ("waiting".equals(key)) return session.getWaiting();
        if ("other_runs".equals(key)) return session.getOther();
        if ("owner".equals(key)) return session.getOwner();
        if ("number_of_entities".equals(key)) return session.getNumOfSession();
        if ("id".equals(key)) return session.getId();
        if ("url".equals(key)) return session.getServerUrl();
        if ("idNames".equals(key)) return session.getIdNames();
        else {
            return "NA";
        }
    }
}
