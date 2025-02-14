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
import java.io.IOException;

/**
 *
 * @author tyanai
 */
public class BatchExecManager implements java.io.Serializable {

    TaskListener jobListener = null;
    String executionScript = null;
    String executionShellLocation = null;
    String vsifFile = null;
    String buildId = null;
    int buildNumber = 0;

    public BatchExecManager(
            TaskListener listener,
            String executionScript,
            String executionShellLocation,
            String executionVsifFile,
            String buildId,
            int buildNumber) {
        this.jobListener = listener;
        this.executionScript = executionScript;
        this.executionShellLocation = executionShellLocation;
        this.vsifFile = executionVsifFile;
        this.buildId = buildId;
        this.buildNumber = buildNumber;
    }

    public void execBatchCommand(FilePath filePath) throws IOException, InterruptedException {

        String[] command = {executionShellLocation, executionScript, vsifFile};

        filePath.act(new InvokeExecutionTask(
                jobListener,
                executionScript,
                executionShellLocation,
                vsifFile,
                buildId,
                buildNumber,
                command,
                filePath));
    }
}
