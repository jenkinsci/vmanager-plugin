package org.jenkins.plugins;

import hudson.model.TaskListener;
import java.io.BufferedReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import javax.net.ssl.HttpsURLConnection;
import org.apache.commons.codec.binary.Base64;
import org.jenkinsci.plugins.vmanager.ReportManager;
import org.jenkinsci.plugins.vmanager.Utils;
import org.jenkinsci.plugins.vmanager.SummaryReportParams;
import static org.jenkinsci.plugins.vmanager.Utils.configureAllowAll;
import org.jenkinsci.plugins.vmanager.VAPIConnectionParam;

public class TestPlugin {

    final String vAPIUrl = "https://ilvmanager08:50500/vmgr/vapi";
    final boolean authRequired = true;
    final String vAPIUser = "root";
    final String vAPIPassword = "letmein";
    final String vSIFName = "/home/tyanai/vsif/vm_basic.vsif";
    final String vSIFInputFile = "c:/temp/artifacts/vsifs.input";
    final boolean deleteInputFile = false;
    final String vsifType = "static";

    final int buildNumber = 83;
    final String buildID = "2014-45-45-34-56-78";
    final String buildArtifactPath = "c:/temp/artifacts";

    private String inaccessibleResolver = "fail";
    private String stoppedResolver = "fail";
    private String failedResolver = "fail";
    private String doneResolver = "ignore";
    private String suspendedResolver = "fail";
    private boolean waitTillSessionEnds = true;
    private int stepSessionTimeout = 10;

    private final boolean generateJUnitXML = true;
    private final boolean extraAttributesForFailures = true;
    private final String staticAttributeList = "top_files,test_group,verification_scope";
    private final boolean markBuildAsFailedIfAllRunFailed = false;
    private final boolean markJobAsFailedIfAllRunFailed = false;
    private final boolean markBuildAsPassedIfAllRunPassed = false;
    private final boolean failJobUnlessAllRunPassed = false;
    
    public static void main(String[] args) throws Exception {
        // TODO Auto-generated method stub

        TestPlugin testPlugin = new TestPlugin();
        testPlugin.test();

    }

    public void test() throws Exception {

        Utils utils = new Utils();

       
        SummaryReportParams summaryReportParams;
        VAPIConnectionParam vAPIConnectionParam;
        vAPIConnectionParam = new VAPIConnectionParam();
        vAPIConnectionParam.vAPIUrl = vAPIUrl;
        vAPIConnectionParam.authRequired = authRequired;
        vAPIConnectionParam.advConfig = false;
        vAPIConnectionParam.vAPIUser = vAPIUser;
        vAPIConnectionParam.vAPIPassword = vAPIPassword;
        vAPIConnectionParam.connTimeout = 1;
        vAPIConnectionParam.readTimeout = 30;

        summaryReportParams = new SummaryReportParams();
        summaryReportParams.runReport = true;
        summaryReportParams.metricsReport = true;
        summaryReportParams.vPlanReport = true;
        
        summaryReportParams.testsDepth = 6;
        summaryReportParams.vPlanDepth = 6;
        summaryReportParams.metricsDepth = 6;
        summaryReportParams.testsViewName = "Tal_ALL";
        summaryReportParams.vplanViewName = "All_Vplan";
        summaryReportParams.metricsViewName = "All_Metrics";
        
        summaryReportParams.metricsInputType = "basic";
        summaryReportParams.vPlanInputType = "basic";
        summaryReportParams.vPlanxFileName = "/home/segal/work/vpm/APB_UART.vplanx";
        
        summaryReportParams.summaryType = "wizard";
        summaryReportParams.ctxInput = true;
        summaryReportParams.ctxAdvanceInput = "{\"vplanFile\":\"\\/home\\/segal\\/work\\/vpm\\/APB_UART.vplanx\"}";
        
        summaryReportParams.summaryMode = "fffff";
       
        
        
        ReportManager reportManager = new ReportManager(summaryReportParams,vAPIConnectionParam,true);
        reportManager.retrievReportFromServer(false,null);

        /*
        
        //Test Reading VSIF input file
        String[] vsifFileNames = null;
        if ("static".equals(vsifType)) {
            vsifFileNames = new String[1];
            vsifFileNames[0] = vSIFName;
        } else {
            vsifFileNames = utils.loadVSIFFileNames(buildID, buildNumber, buildArtifactPath, vSIFInputFile, null, deleteInputFile);
        }

        //Test connection testing
        utils.checkVAPIConnection(vAPIUrl, authRequired, vAPIUser, vAPIPassword);

        //Test attributes for JUnit taken from runs
        String testAttr = utils.checkExtraStaticAttr(vAPIUrl, authRequired, vAPIUser, vAPIPassword, "comment,status,first_failure_name");
        System.out.println(testAttr);

        StepHolder stepHolder = null;
        if (waitTillSessionEnds) {
            JUnitRequestHolder jUnitRequestHolder = new JUnitRequestHolder(generateJUnitXML, extraAttributesForFailures, staticAttributeList, false);
            stepHolder = new StepHolder(inaccessibleResolver, stoppedResolver, failedResolver, doneResolver, suspendedResolver, waitTillSessionEnds, stepSessionTimeout, jUnitRequestHolder, markBuildAsFailedIfAllRunFailed, markJobAsFailedIfAllRunFailed, markBuildAsPassedIfAllRunPassed, failJobUnlessAllRunPassed);
        }
        String[] sessionNames = new String[3];
        sessionNames[0] = "vm_basic_scopes.root.19_09_10_13_57_26_6081";
        sessionNames[1] = "vm_basic_scopes.root.19_09_09_19_46_00_6347";
        sessionNames[2] = "vm_basic_scopes.root.19_09_09_12_43_21_5408	";
        utils.executeVSIFLaunch(vsifFileNames, vAPIUrl, authRequired, vAPIUser, vAPIPassword, null, false, buildID, buildNumber, buildArtifactPath, 0, 0, false, null, false, null, null, stepHolder, "", buildArtifactPath, null, false, null, "batch", sessionNames);

        //String buildStatus = BuildStatusMap.getValue(buildID, buildNumber, buildArtifactPath+"", "id", true);
        //System.out.println("Build status is '" + buildStatus + "'" );
        //utils.executeAPI("{}", "/sessions/count", vAPIUrl, authRequired, vAPIUser, vAPIPassword, "POST", null, false, buildID+"-1", buildNumber, buildArtifactPath,0,0,false);
        //utils.executeAPI("{}", "/runs/get?id=5", vAPIUrl, authRequired, vAPIUser, vAPIPassword, "GET", null, false, buildID+"-2", buildNumber, buildArtifactPath,0,0,false);

*/
    }
    
    public HttpURLConnection getVAPIConnection() throws Exception {

       
        //In case this is an SSL connections
        String apiUrl = "https://ilvmanager08:50500/vmgr/vapi/rest/sessions/sleep?time=10000";
        String requestMethod = "GET";
        int connConnTimeOut = 1;
        int connReadTimeout = 30;
        String user = "root";
        String password = "letmein";
        
                
        URL url = new URL(apiUrl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        conn.setDoOutput(true);
        conn.setRequestMethod(requestMethod);
        if ("PUT".equals(requestMethod) || "POST".equals(requestMethod)) {
            conn.setRequestProperty("Content-Type", "application/json");
        }

        // set the connection timeouts to one minute and the read timeout to 30 minutes by default
       
         conn.setConnectTimeout(connConnTimeOut * 60 * 1000);
       

        
        conn.setReadTimeout(connReadTimeout * 60 * 1000);
       
        conn.setReadTimeout(1800000);
        
        // ----------------------------------------------------------------------------------------
        // Authentication
        // ----------------------------------------------------------------------------------------

        String authString = user + ":" + password;
        
        byte[] authEncBytes = Base64.encodeBase64(authString.getBytes(Charset.forName("UTF-8")));
        String authStringEnc = new String(authEncBytes,Charset.forName("UTF-8"));
        conn.setRequestProperty("Authorization", "Basic " + authStringEnc);
        // ----------------------------------------------------------------------------------------
        return conn;
    }

}
