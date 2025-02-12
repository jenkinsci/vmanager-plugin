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

/**
 *
 * @author tyanai
 */
public class SummaryReportParams {

    public boolean runReport;
    public boolean metricsReport;
    public boolean vPlanReport;
    public String testsViewName;
    public String metricsViewName;
    public String vplanViewName;
    public int testsDepth = 6;
    public int metricsDepth = 6;
    public int vPlanDepth = 6;

    public String metricsInputType;
    public String metricsAdvanceInput;
    public String vPlanInputType;
    public String vPlanAdvanceInput;
    public String vPlanxFileName;
    public String summaryType;
    public boolean ctxInput;
    public String ctxAdvanceInput;
    public String freeVAPISyntax;
    public boolean deleteReportSyntaxInputFile;
    public String vManagerVersion;
    public boolean sendEmail;
    public String emailList;
    public String summaryMode;

    public String emailType;
    public String emailInputFile;
    public boolean deleteEmailInputFile;
    public boolean ignoreSSLError;

    public static final String staticReportParams =
            "$jenkins_mode\"override\":true,\"sessionsViewName\":\"All_Sessions\",\"linkOutput\":$link_output,\"title\":\"Summary report\",\"includeSessions\":true,\"includeAll\":false$test_view_name$metrics_view_name$vplan_view_name$test_depth";
    public boolean includeTests = true;
    public static String metricsData =
            "{\"scope\":\"default\",\"extended\":false,\"instances\":true,\"types\":true,\"depth\":6}";
    public static String vPlanData = "{\"extended\":true,\"instances\":true,\"types\":true,\"depth\":6}";
    public static String ctxData = "{}";
}
