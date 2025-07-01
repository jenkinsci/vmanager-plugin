# Cadence Verisium Manager Jenkins Plugin - Pipeline Properties

The setup and configuration of Verisium Manager Jenkins Plugin Pipeline are controlled by setting options for the pipeline.

These options are set in the Jenkins pipeline definition script, and are set in name: value pairs, separated by commas.

The array is given as an argument to the declaration of the pipeline object.

<br><br>

## Verisium Manager Session Launcher

The properties and options available are listed below in alphabetic order. Some have more details described in the main readme page.

| Property |  Type  | Description | Default | Mandatory |
|:-----|:--------:|:--------|:--------|:--------:|
| advConfig|boolean |Enable customization of the connection parameters |false | no |
|archivePassword|string|password for session delete operation (only valid with genericCredentialForSessionDelete=true)|empty string|no|
|archiveUser|string|userid for session delete operation (only valid with genericCredentialForSessionDelete=true)|empty string|no|
|attrValues|boolean|Select to include input file for setting list of vsif attributes values|false|no|
|attrValuesFile|string|Location for the vsif attributes values input file|empty string|no|
|authRequired|boolean|Set if to authenticate the REST API or not|false|Only if setting vAPIPassword|
|connTimeout|integer|Timeout (in minutes) for REST connection to establish connectivity|1|no|
|credentialInputFile|string|Location of the file containing the user/password for the dynamic user for use when userFarmType is true|false|no|
|defineVarible|boolean|Select to include input file for setting list of env variables avaiable at session execution time|false|no|
|defineVaribleFile|string|Location for the  env variables input file|empty string|no|
|deleteAlsoSessionDirectory|boolean|Inititate REST call to vManager to delete session  (only relevant when vMGRBuildArchive=true)|false|no|
|deleteCredentialInputFile|boolean|Delete of the file containing the user/password for the dynamic user for use when userFarmType is true after reading the values|false|no|
|deleteInputFile|boolean|Delete the input file for the dynamic session list after reading from it|false|no|
|deleteSessionInputFile|boolean|Delete of the file containing the sessions for sessionsInputFile after reading the values from it|false|no|
|doneResolver|string|<p>**fail** - fail the job</p><p>**ignore**  - wait till the session gets to 'completed' state</p><p>**continue** - move on and mark the job as done - do not wait till the session gets to 'completed' state</p>|continue| no|
|dynamicUserId|boolean|Set if to use different user id for every job run|false|no|
|envSourceInputFile|string|Source file when performing SSH login (when useUserOnFarm is true)|false|no|
|envSourceInputFileType|string|Enum (CSH / BASH) - Script type when envSourceInputFile is set|BASH|no|
|envVarible|boolean|Select to include input file for setting list of vsif env variables avaiable at session execution time|false|no|
|envVaribleFile|string|Location for the vsif env variables input file|empty string|no|
|executionScript|string|Full path for the batch script location (valid only when executionType is hybrid)|false|Only if executionType is hybrid|
|executionShellLocation|string|Full path for the shell type (valid only when executionType is hybrid)|false|Only if executionType is hybrid|
|executionType|string|<p>**fail** - fail the job</p><p>**ignore**  - wait till the session gets to 'completed' state</p><p>**continue** - move on and mark the job as done - do not wait till the session gets to 'completed' state</p>|launcher|yes|
|executionVsifFile|string|Full path for vsif location (valid only when executionType is hybrid)|false|Only if executionType is hybrid|
|extraAttributesForFailures|boolean|Include extra attributes for failure stack -  separated by comma for the Junit XML file (per run)|false|no|
|failedResolver|string|<p>**fail** - fail the job</p><p>**ignore**  - wait till the session gets to 'completed' state</p><p>**continue** - move on and mark the job as done - do not wait till the session gets to 'completed' state</p>|fail| no|
|failJobIfAllRunFailed|boolean|If checked - the Jenkins job will fail and stop its execution when all session runs failed|false|no|
|failJobUnlessAllRunPassed|boolean|If checked - the Jenkins job will fail and stop its execution unless all runs passed|false|no|
|famMode|boolean|<p>**false** - Delete the vManager session using built-in REST call (calid only when vMGRBuildArchive=true)</p><p>**true** - Delete the sesssion using an external user's script</p>|false|no|
|famModeLocation|string|Location for the executable script that delete a  vManager session (only valid when famMode=true)|empty string|no|
|generateJUnitXML|boolean|Generate the junit XML standard file for the session's runs|false|no|
|genericCredentialForSessionDelete|boolean|Specify dedicated credentials for deleting the sessions (only relevant when vMGRBuildArchive=true)|false|no|
|inaccessibleResolver|string|<p>**fail** - fail the job</p><p>**ignore**  - wait till the session gets to 'completed' state</p><p>**continue** - move on and mark the job as done - do not wait till the session gets to 'completed' state</p>|fail| no|
|markBuildAsFailedIfAllRunFailed|boolean|If checked - Jenkins Dashboard  will show the job as Failed when all Session runs failed|false|no|
|markBuildAsPassedIfAllRunPassed|boolean|If checked - the Dashboard will show the session status as Failed instead of completed when not all runs passed.|false|no|
|noAppendSeed|boolean|If checked - the junit report will only contain the test name  without appending the seed that was used (only relevant when generateJUnitXML=true)|false|no|
|pauseSessionOnBuildInterruption|boolean|Pause the session within the vManager System when Job is paused/interupted|false|no|
|readTimeout|integer|Timeout (in minutes) for REST connection to wait for response from the server|30|no|
|sessionsInputFile|string|Location of sessions list that were just launched from batch.  Only valid when executionType is batch|empty string|Only if executionType is batch|
|staticAttributeList|string|Static extra attributes for failure stack - separated by comma for the Junit XML file (per run)|empty string|no|
|stepSessionTimeout|integer|Time (in minutes) to wait for session to complete (only relevant when waitTillSessionEnds=true)|false|30|
|stoppedResolver|string|<p>**fail** - fail the job</p><p>**ignore**  - wait till the session gets to 'completed' state</p><p>**continue** - move on and mark the job as done - do not wait till the session gets to 'completed' state</p>|fail|no|
|suspendedResolver|string|<p>**fail** - fail the job</p><p>**ignore**  - wait till the session gets to 'completed' state</p><p>**continue** - move on and mark the job as done - do not wait till the session gets to 'completed' state</p>|continue| no|
|userFarmType|string|Enum (static / dynamic) - Set if to use dynamic user id for every job run or static userid (same as the main REST API)|static|no|
|userPrivateSSHKey|boolean|Set this to SSH into user's Linux account using SSH public key|false|no|
|useUserOnFarm|boolean|Use user's Linux account to launch the vManager regression|false|no|
|vAPIPassword|string|The password of the user making the connection to vManager|empty string|yes|
|vAPIUrl|string|The url location of the vManager Server|empty string|yes|
|vAPIUser|string|The user of the user making the connection to vManager|vAPI|yes|
|vMGRBuildArchive|boolean|Define if to delete the session from vManager when the job is removed in Jenkins|false|yes|
|vSIFInputFile|string|Path to the dynamic list of vsif (available to Jenkins User)|empty string|yes|
|vSIFName|string|Path to the static vSIF within the Linux OS NFS|empty string|yes|
|vsifType|string|Enum (static / dynamic)|empty string|Only if 'session launcher' is used|
|waitTillSessionEnds|boolean|When set to true - the job will wait till session is completed|false|no|


<br><br>


## Verisium Manager Post Build Actions

| Property |  Type  | Description | Default | Mandatory |
|:-----|:--------:|:--------|:--------|:--------:|
|advConfig|boolean|Enable customization of the connection parameters|false|yes|
|authRequired|boolean|Set if to authenticate the REST API or not|false|yes|
|connTimeout|integer|Timeout (in minutes) for REST connection to establish connectivity|1|yes|
|dynamicUserId|boolean|Set if to use different user id for every job run|false|yes|
|readTimeout|integer|Timeout (in minutes) for REST connection to wait for response from the server|30|yes|
|vAPIPassword|string|The password of the user making the connection to vManager|empty string|yes|
|vAPIUrl|string|The url location of the vManager Server|empty string|yes|
|vAPIUser|string|The user of the user making the connection to vManager|vAPI|yes|
|advancedFunctions|boolean|Set to true in order to enable the post plugin functionality |false|yes|
|retrieveSummaryReport|boolean|Set to true in order to enable a summary report page|false|yes|
|summaryMode|string|<p>**full** - built in functionality to bring all aspects of the summary report</p><p>**selfmade** - Bring the summary report yourself (view mode only) - 19.09 and above only</p>|full|yes|
|vManagerVersion|string|<p>**stream** - report comes in a streaming type of trasport from the server</p><p>**html** - report is being fetched as html content at once</p>|stream|yes|
|ignoreSSLError|boolean|ignore none certified ssl|false|yes|
|summaryType|string|<p>**freesyntax** - set this option in order to supply your own json file (vAPI format) to bring the summmary report</p><p>**wizard** - set this option in order to use Jenkins Plugin wizard for the report settings</p>|wizard|yes|
|freeVAPISyntax|string|A file location with JSON representation of the vAPI input for the /reports/generate-summary-report vAPI (entire POST request) - Only relevant when summaryType=freesyntax |empty string|yes|
|runReport|boolean|Include Runs tree grades (only relevant when summaryType=wizard)|false|yes|
|metricsReport|boolean|Include Metrics grades  (only relevant when summaryType=wizard)|false|yes|
|vPlanReport|boolean|Include vPlan grades  (only relevant when summaryType=wizard)|false|yes|
|ctxInput|boolean|take advantage of the full summary report flags using json structure|false|yes|
|ctxAdvanceInput|string|JSON representation of the vAPI input for the summary report (ctxData part).  Only relevant when ctxInput=ture|empty string|yes|
|testsViewName|string|Runs Tree View (only relevant when runReport=true)|Test_Hierarchy|yes|
|testsDepth|integer|Max level to paint the runs report hierarchy (only relevant when runReport=true)|6|yes|
|metricsViewName|string|Metrics View (only relevant when metricsReport=true)|All_Metrics|yes|
|metricsDepth|integer|Max level to paint the metrics report hierarchy part (only relevant when metricsInputType=basic)|6|yes|
|metricsInputType|string|<p>**advance** - use vAPI metricsData JSON Syntax</p><p>**basic** - use only max depth</p>|basic|yes|
|metricsAdvanceInput|string|JSON representation of the API input for Metrics summary report (metricsData part). Relevant when metricsInputType=advance|empty string|yes|
|vPlanxFileName|string|Fullpath to the vPlan for the report (only relevant when vplanReport=true)|empty string|yes|
|vplanViewName|string|vPlan View (only relevant when vplanReport=true)|All_Vplan|yes|
|vPlanDepth|integer|Max level to paint the vPlan report hierarchy part (only relevant when metricsInputType=basic)|6|yes|
|vPlanInputType|string|<p>**advance** - use vAPI vPlanData JSON Syntax</p><p>**basic** - use only max depth</p>|basic|yes|
|vPlanAdvanceInput|string|JSON representation of the API input for vPlan summary report (vPlanData part).  Relevant when  vPlanInputType=advanced|empty string|yes|
|deleteReportSyntaxInputFile|boolean|Delete the JSON input file supplied for  summaryMode=selfmade|false|yes|
|sendEmail|boolean|Send the report also via email|false|yes|
|emailList|string|Comma separated list of emails.  Relevant when emailType=static|false|yes|
|emailType|string|<p>**static** - hardcoded list of emails</p><p>**dynamic** - input file with one or more email addresses</p>|static|yes|
|emailInputFile|string|Full path to input file with one or more email addresses.  Only relevant when emailType=dynamic|empty string|yes|
|deleteEmailInputFile|boolean|Delete the input file of the email addresses after reading from it.  Only relevant when emailType=dynamic|false|yes|