package org.jenkinsci.plugins.vmanager.dsl.post;


import hudson.FilePath;
import hudson.Launcher;
import hudson.model.Run;
import hudson.model.TaskListener;
//import org.jenkinsci.plugins.workflow.steps.AbstractSynchronousNonBlockingStepExecution;
import org.jenkinsci.plugins.workflow.steps.StepContext;
import org.jenkinsci.plugins.workflow.steps.SynchronousNonBlockingStepExecution;
//import org.jenkinsci.plugins.workflow.steps.StepContextParameter;

public class VMGRPostLaunchStepImpl extends SynchronousNonBlockingStepExecution<Void> {
    
   private static final long serialVersionUID = 6000009076155338047L;
   private transient final VMGRPostLaunchStep step;
   
   VMGRPostLaunchStepImpl(VMGRPostLaunchStep step, StepContext context) {
        super(context);
        this.step = step;
    }
   
  
    
   @Override
   protected Void run() throws Exception {
       
       TaskListener listener = getContext().get(TaskListener.class);
       FilePath ws = getContext().get(FilePath.class);
       Run build = getContext().get(Run.class);
       Launcher launcher = getContext().get(Launcher.class);
       
       listener.getLogger().println("Running vManager Post Job step.");
       DSLPublisher publisher;
       if (step.isAdvancedFunctions()){
           
            /*
            String vAPIPassword = "";
            if ( ((VMGRPostLaunchStep.DescriptorImpl)(step.getDescriptor())).getVAPIPassword() != null){
                vAPIPassword = ((VMGRPostLaunchStep.DescriptorImpl)(step.getDescriptor())).getVAPIPassword().getPlainText();
            } else {
                listener.getLogger().println("Warning - no password supplied for vManager Post Job.");
            }
            */

           publisher = new DSLPublisher(step.getVAPIUrl(), step.getVAPIUser(), step.getVAPIPassword(), step.isAuthRequired(), step.isAdvConfig(), step.isDynamicUserId(), step.getConnTimeout(), step.getReadTimeout(), step.isAdvancedFunctions(),
            step.isRetrieveSummaryReport(), step.isRunReport(), step.isMetricsReport(), step.isVPlanReport(), step.getTestsViewName(), step.getMetricsViewName(), step.getVplanViewName(), step.getTestsDepth(), step.getMetricsDepth(),
            step.getVPlanDepth(), step.getMetricsInputType(), step.getMetricsAdvanceInput(), step.getVPlanInputType(), step.getVPlanAdvanceInput(), step.getVPlanxFileName(), step.getSummaryType(), step.isCtxInput(),
            step.getCtxAdvanceInput(), step.getFreeVAPISyntax(), step.isDeleteReportSyntaxInputFile(),step.getVManagerVersion(), step.isSendEmail(), step.getEmailList(),step.getEmailType(), step.getEmailInputFile(),step.isDeleteEmailInputFile(), step.getSummaryMode(), step.isIgnoreSSLError(),null,"text");
            
       } else {
           publisher = new DSLPublisher();
       }
       listener.getLogger().println("1 - Minus");
       publisher.perform(build, ws, launcher, listener);

       return null;
   }   
}
