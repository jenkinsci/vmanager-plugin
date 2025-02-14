/*
 * The MIT License
 *
 * Copyright 2020 Cadence.
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

import hudson.FilePath;
import hudson.model.TaskListener;
import hudson.remoting.VirtualChannel;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import jenkins.MasterToSlaveFileCallable;
import org.apache.commons.lang.exception.ExceptionUtils;

/**
 *
 * @author tyanai
 */
public class InvokeExecutionTask extends MasterToSlaveFileCallable<Void> implements java.io.Serializable {

    TaskListener jobListener = null;
    String executionScript = null;
    String executionShellLocation = null;
    String vsifFile = null;
    String buildId = null;
    int buildNumber = 0;
    String[] command;
    FilePath filePath = null;

    public InvokeExecutionTask(
            TaskListener listener,
            String executionScript,
            String executionShellLocation,
            String executionVsifFile,
            String buildId,
            int buildNumber,
            String[] command,
            FilePath filePath) {
        this.jobListener = listener;
        this.executionScript = executionScript;
        this.executionShellLocation = executionShellLocation;
        this.vsifFile = executionVsifFile;
        this.buildId = buildId;
        this.buildNumber = buildNumber;
        this.command = command;
        this.filePath = filePath;
    }

    @Override
    public Void invoke(File f, VirtualChannel channel) throws IOException, InterruptedException {
        String sessionNameToMonitor = null;
        jobListener.getLogger().print("\nTrying to launch a session using batch on this agent workspace:\n");
        jobListener.getLogger().print("Select script for this execution is " + executionScript + "\n");
        jobListener.getLogger().print("Select shell type for this execution is " + executionShellLocation + "\n");
        jobListener.getLogger().print("Select vsif for this execution is " + vsifFile + "\n");
        boolean foundGoodVSIF = false;
        try {

            /*
            Launcher.ProcStarter ps = launcher.new ProcStarter();
            ps.cmds(command).readStdout();

            Proc proc = launcher.launch(ps);
            BufferedReader in = new BufferedReader(new InputStreamReader(proc.getStdout()));
            BufferedReader inError = new BufferedReader(new InputStreamReader(proc.getStderr()));
            */

            ProcessBuilder builder = new ProcessBuilder(command);
            builder.redirectErrorStream(true);
            Process proc = builder.start();
            BufferedReader in =
                    new BufferedReader(new InputStreamReader(proc.getInputStream(), StandardCharsets.UTF_8));
            BufferedReader inError =
                    new BufferedReader(new InputStreamReader(proc.getErrorStream(), StandardCharsets.UTF_8));

            String s = null;

            while ((s = in.readLine()) != null) {

                jobListener.getLogger().print(s + "\n");
                if (s.indexOf("*I,runner.sessionStarted: Session") > -1) {
                    foundGoodVSIF = true;
                    sessionNameToMonitor = s.substring(34, s.indexOf(" started."));

                    // Now creates the file of sessions.input
                    String fileOutput = buildNumber + "." + buildId + ".sessions.input";
                    StringBuffer writer = new StringBuffer();
                    writer.append(sessionNameToMonitor);
                    hudson.FilePath newFile = filePath.child(fileOutput);
                    newFile.write(writer.toString(), StandardCharsets.UTF_8.name());
                }
            }

            String sError = null;

            while ((sError = inError.readLine()) != null) {

                jobListener.getLogger().print(sError + "\n");
            }

            in.close();
            inError.close();

        } catch (IOException e) {
            jobListener.getLogger().println(e.getMessage());
            for (StackTraceElement ste : e.getStackTrace()) {
                jobListener.getLogger().println(" " + ste);
            }

            jobListener.getLogger().println(ExceptionUtils.getFullStackTrace(e));
        } catch (InterruptedException ex) {
            jobListener.getLogger().println(ex.getMessage());
            for (StackTraceElement ste : ex.getStackTrace()) {
                jobListener.getLogger().println(" " + ste);
            }

            jobListener.getLogger().println(ExceptionUtils.getFullStackTrace(ex));
        }

        if (foundGoodVSIF) {
            jobListener.getLogger().print("Found session name to monitor: " + sessionNameToMonitor + "\n");
        } else {
            throw new IOException("Failed to launch vsif using batch.  Job stopped.");
        }

        return null;
    }
}
