/*
 * The MIT License
 *
 * Copyright 2017 Cadence.
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
import java.util.Comparator;
import javax.annotation.Nonnull;

/**
 *
 * @author tyanai
 */
public class VMGRRun {

    private Run run = null;
    private String jobWorkingDir = null;
    private String generalWorkingDir = null;

    public VMGRRun(Run run, String jobWorkingDir, String generalWorkingDir) {
        this.run = run;
        this.jobWorkingDir = jobWorkingDir;
        this.generalWorkingDir = generalWorkingDir;
    }

    public Run getRun() {
        return run;
    }

    public String getJobWorkingDir() {
        return jobWorkingDir;
    }

    public String getGeneralWorkingDir() {
        return generalWorkingDir;
    }

    public void setRun(Run run) {
        this.run = run;
    }

    public void setJobWorkingDir(String jobWorkingDir) {
        this.jobWorkingDir = jobWorkingDir;
    }

    public void setGeneralWorkingDir(String generalWorkingDir) {
        this.generalWorkingDir = generalWorkingDir;
    }

    public static final Comparator<VMGRRun> ORDER_BY_DATE = new Comparator<VMGRRun>() {
        public int compare(@Nonnull VMGRRun lhs, @Nonnull VMGRRun rhs) {
            long lt = lhs.run.getTimeInMillis();
            long rt = rhs.run.getTimeInMillis();
            if (lt > rt) return -1;
            if (lt < rt) return 1;
            return 0;
        }
    };
}
