package org.jenkinsci.plugins.vmanager;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class UnitTestFormatter {

    List runs = null;
    String sessionId = null;
    JUnitRequestHolder jUnitRequestHolder = null;
    Map<String, String> extraAttrLabels = null;

    public UnitTestFormatter(
            List runs, String sessionId, JUnitRequestHolder jUnitRequestHolder, Map<String, String> extraAttrLabels) {
        super();
        this.runs = runs;
        this.sessionId = sessionId;
        this.jUnitRequestHolder = jUnitRequestHolder;
        this.extraAttrLabels = extraAttrLabels;
    }

    public List getRuns() {
        return runs;
    }

    public void setRuns(JSONArray runs) {
        this.runs = runs;
    }

    public void dumpXMLFile(String workPlacePath, int buildNumber, String buildID, Utils utils) throws IOException {

        if (runs.size() > 0) {
            // Flush the output into workspace
            // String fileOutput = workPlacePath + File.separator + buildNumber + "." + buildID + "." + sessionId +
            // ".session_runs.xml";
            String fileOutput = "session_runs.xml";
            if (utils.getFilePath() == null) {
                // Pipeline always run on master
                fileOutput = workPlacePath + File.separator + fileOutput;
            }

            StringBuffer writer = new StringBuffer();
            writer.append("<testsuite tests=\"Verisium Manager\">" + "\n");
            Iterator<JSONObject> runsIter = runs.iterator();
            JSONObject tmpRun = null;
            String testStatus = "NA";
            String testGroup = "NA";
            String name = "NA";
            String testSeed = "NA";
            int testDuration = 0;
            String testFirstErrorCode = "NA";
            String testFirstErrorDescription = "NA";

            while (runsIter.hasNext()) {
                tmpRun = runsIter.next();
                if (tmpRun.has("status")) {
                    testStatus = tmpRun.getString("status");
                } else {
                    testStatus = "NA";
                }
                if (tmpRun.has("test_group")) {
                    testGroup = stripNonValidXMLCharacters(tmpRun.getString("test_group"));
                } else {
                    testGroup = "NA";
                }
                if (tmpRun.has("test_name")) {
                    name = stripNonValidXMLCharacters(tmpRun.getString("test_name"));
                } else {
                    name = "NA";
                }
                if (tmpRun.has("computed_seed")) {
                    testSeed = stripNonValidXMLCharacters(tmpRun.getString("computed_seed"));
                } else {
                    testSeed = "NA";
                }

                if (tmpRun.has("duration")) {
                    try {
                        testDuration =
                                Integer.valueOf(tmpRun.getString("duration")).intValue();
                    } catch (Exception e) {
                        // In case it's not a number like "undefined"
                    }
                } else {
                    testDuration = 0;
                }

                // Check if user choose to append the seed to the test name:
                String seedNameForAppending = "";
                if (!jUnitRequestHolder.isNoAppendSeed()) {
                    seedNameForAppending = " : Seed-" + testSeed;
                }

                if ("failed".equals(testStatus)) {
                    if (tmpRun.has("first_failure_name")) {
                        testFirstErrorCode = stripNonValidXMLCharacters(tmpRun.getString("first_failure_name"));
                    } else {
                        testFirstErrorCode = "RUN_STILL_IN_PROGRESS";
                    }
                    if (tmpRun.has("first_failure_description")) {
                        testFirstErrorDescription =
                                stripNonValidXMLCharacters(tmpRun.getString("first_failure_description"));
                    } else {
                        testFirstErrorDescription =
                                "    Run is in state running,other or waiting.\n     Reason for run to mark as failed is because session change status to such that build was marked as failed.";
                    }
                    writer.append("		<testcase classname=\"" + testGroup + "\" name=\"" + name + seedNameForAppending
                            + "\" time=\"" + testDuration + "\">" + "\n");
                    writer.append("			<failure message=\"" + testFirstErrorCode + "\" type=\"" + testFirstErrorCode
                            + "\">First Error Description: \n" + testFirstErrorDescription + "\n" + "Computed Seed: \n"
                            + testSeed + "\n" + stripNonValidXMLCharacters(addExtraAttrValues(tmpRun)) + "</failure>"
                            + "\n");
                    writer.append("		</testcase>" + "\n");
                } else if ("stopped".equals(testStatus)
                        || "running".equals(testStatus)
                        || "other".equals(testStatus)
                        || "waiting".equals(testStatus)) {
                    writer.append("		<testcase classname=\"" + testGroup + "\" name=\"" + name + seedNameForAppending
                            + "\" time=\"" + testDuration + "\">" + "\n");
                    writer.append("		 <skipped />" + "\n");
                    writer.append("		</testcase>" + "\n");
                } else {
                    writer.append("		<testcase classname=\"" + testGroup + "\" name=\"" + name + seedNameForAppending
                            + "\" time=\"" + testDuration + "\"/>" + "\n");
                }
            }

            writer.append("</testsuite>" + "\n");

            utils.saveFileOnDisk(fileOutput, writer.toString());
        }
    }

    private String addExtraAttrValues(JSONObject tmpRun) {
        if (!jUnitRequestHolder.istExtraAttributesForFailures()) {
            return "";
        }

        if (jUnitRequestHolder.getStaticAttributeList() == null
                || "".equals(jUnitRequestHolder.getStaticAttributeList().trim())) {
            return "";
        }

        List<String> items =
                Arrays.asList(jUnitRequestHolder.getStaticAttributeList().split("\\s*,\\s*"));

        Iterator<String> iter = items.iterator();
        String extraAttributesForRuns = "";

        String tmpAttr = null;
        String attrValue = null;
        while (iter.hasNext()) {
            tmpAttr = iter.next();
            if (tmpAttr.indexOf(" ") > 0
                    || tmpAttr.equals("first_failure_name")
                    || tmpAttr.equals("first_failure_description")
                    || tmpAttr.equals("computed_seed")
                    || tmpAttr.equals("test_group")
                    || tmpAttr.equals("test_name")) {
                continue;
            } else {
                if (tmpRun.has(tmpAttr)) {

                    attrValue = tmpRun.getString(tmpAttr);
                    attrValue = attrValue.replaceAll("<__SEPARATOR__>", "\n    ");
                    extraAttributesForRuns =
                            extraAttributesForRuns + extraAttrLabels.get(tmpAttr) + ":\n    " + attrValue + "\n";
                } else {
                    attrValue = "NA";
                }
            }
        }

        return extraAttributesForRuns;
    }

    public static String stripNonValidXMLCharacters(String t) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < t.length(); i++) {
            char c = t.charAt(i);
            switch (c) {
                case '<':
                    sb.append("&lt;");
                    break;
                case '>':
                    sb.append("&gt;");
                    break;
                case '\"':
                    sb.append("&quot;");
                    break;
                case '&':
                    sb.append("&amp;");
                    break;
                case '\'':
                    sb.append("&apos;");
                    break;
                default:
                    if (c > 0x7e) {
                        sb.append("&#" + ((int) c) + ";");
                    } else {
                        sb.append(c);
                    }
            }
        }
        return sb.toString();
    }
}
