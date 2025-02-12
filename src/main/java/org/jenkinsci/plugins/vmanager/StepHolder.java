package org.jenkinsci.plugins.vmanager;

public class StepHolder {

    private String inaccessibleResolver;
    private String stoppedResolver;
    private String failedResolver;
    private String doneResolver;
    private String suspendedResolver;
    private boolean waitTillSessionEnds;
    private int stepSessionTimeout = 0;
    private JUnitRequestHolder jUnitRequestHolder = null;
    private boolean markBuildAsFailedIfAllRunFailed = false;
    private boolean failJobIfAllRunFailed = false;
    private boolean markBuildAsPassedIfAllRunPassed = false;
    private boolean failJobUnlessAllRunPassed = false;
    private boolean pauseSessionOnBuildInterruption = false;

    public StepHolder(
            String inaccessibleResolver,
            String stoppedResolver,
            String failedResolver,
            String doneResolver,
            String suspendedResolver,
            boolean waitTillSessionEnds,
            int stepSessionTimeout,
            JUnitRequestHolder jUnitRequestHolder,
            boolean markBuildAsFailedIfAllRunFailed,
            boolean failJobIfAllRunFailed,
            boolean markBuildAsPassedIfAllRunPassed,
            boolean failJobUnlessAllRunPassed,
            boolean pauseSessionOnBuildInterruption) {
        super();
        this.inaccessibleResolver = inaccessibleResolver;
        this.stoppedResolver = stoppedResolver;
        this.failedResolver = failedResolver;
        this.doneResolver = doneResolver;
        this.suspendedResolver = suspendedResolver;
        this.waitTillSessionEnds = waitTillSessionEnds;
        this.stepSessionTimeout = stepSessionTimeout;
        this.jUnitRequestHolder = jUnitRequestHolder;
        this.markBuildAsFailedIfAllRunFailed = markBuildAsFailedIfAllRunFailed;
        this.failJobIfAllRunFailed = failJobIfAllRunFailed;
        this.markBuildAsPassedIfAllRunPassed = markBuildAsPassedIfAllRunPassed;
        this.failJobUnlessAllRunPassed = failJobUnlessAllRunPassed;
        this.pauseSessionOnBuildInterruption = pauseSessionOnBuildInterruption;
    }

    public JUnitRequestHolder getjUnitRequestHolder() {
        return jUnitRequestHolder;
    }

    public boolean isMarkBuildAsPassedIfAllRunPassed() {
        return markBuildAsPassedIfAllRunPassed;
    }

    public boolean isFailJobUnlessAllRunPassed() {
        return failJobUnlessAllRunPassed;
    }

    public boolean isPauseSessionOnBuildInterruption() {
        return pauseSessionOnBuildInterruption;
    }

    public void setPauseSessionOnBuildInterruption(boolean pauseSessionOnBuildInterruption) {
        this.pauseSessionOnBuildInterruption = pauseSessionOnBuildInterruption;
    }

    public void setMarkBuildAsPassedIfAllRunPassed(boolean markBuildAsPassedIfAllRunPassed) {
        this.markBuildAsPassedIfAllRunPassed = markBuildAsPassedIfAllRunPassed;
    }

    public void setFailJobUnlessAllRunPassed(boolean failJobUnlessAllRunPassed) {
        this.failJobUnlessAllRunPassed = failJobUnlessAllRunPassed;
    }

    public void setjUnitRequestHolder(JUnitRequestHolder jUnitRequestHolder) {
        this.jUnitRequestHolder = jUnitRequestHolder;
    }

    public int getStepSessionTimeout() {
        return stepSessionTimeout;
    }

    public void setStepSessionTimeout(int stepSessionTimeout) {
        this.stepSessionTimeout = stepSessionTimeout;
    }

    public String getInaccessibleResolver() {
        return inaccessibleResolver;
    }

    public void setInaccessibleResolver(String inaccessibleResolver) {
        this.inaccessibleResolver = inaccessibleResolver;
    }

    public String getStoppedResolver() {
        return stoppedResolver;
    }

    public void setStoppedResolver(String stoppedResolver) {
        this.stoppedResolver = stoppedResolver;
    }

    public String getFailedResolver() {
        return failedResolver;
    }

    public void setFailedResolver(String failedResolver) {
        this.failedResolver = failedResolver;
    }

    public String getDoneResolver() {
        return doneResolver;
    }

    public void setDoneResolver(String doneResolver) {
        this.doneResolver = doneResolver;
    }

    public String getSuspendedResolver() {
        return suspendedResolver;
    }

    public void setSuspendedResolver(String suspendedResolver) {
        this.suspendedResolver = suspendedResolver;
    }

    public boolean isWaitTillSessionEnds() {
        return waitTillSessionEnds;
    }

    public void setWaitTillSessionEnds(boolean waitTillSessionEnds) {
        this.waitTillSessionEnds = waitTillSessionEnds;
    }

    public boolean isMarkBuildAsFailedIfAllRunFailed() {
        return markBuildAsFailedIfAllRunFailed;
    }

    public void setMarkBuildAsFailedIfAllRunFailed(boolean markBuildAsFailedIfAllRunFailed) {
        this.markBuildAsFailedIfAllRunFailed = markBuildAsFailedIfAllRunFailed;
    }

    public boolean isFailJobIfAllRunFailed() {
        return failJobIfAllRunFailed;
    }

    public void setFailJobIfAllRunFailed(boolean failJobIfAllRunFailed) {
        this.failJobIfAllRunFailed = failJobIfAllRunFailed;
    }
}
