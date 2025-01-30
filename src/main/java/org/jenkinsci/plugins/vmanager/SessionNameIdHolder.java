package org.jenkinsci.plugins.vmanager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.util.Iterator;
import java.util.List;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import hudson.model.TaskListener;
import java.nio.charset.Charset;
import java.util.ArrayList;

public class SessionNameIdHolder {
	
	
	
	private String postSessionData = "{\"filter\":{\"@c\":\".ChainedFilter\",\"condition\":\"OR\",\"chain\":[" + "######"  + "]},\"pageLength\":10000,\"settings\":{\"write-hidden\":true,\"stream-mode\":false},\"projection\":{\"type\": \"SELECTION_ONLY\",\"selection\":[\"name\",\"id\"]}}";

	
	
	
	private String buildPostDataSessionPart(String[] listOfSessions){
		
		//Iterator<String> iter = listOfSessions.iterator();
		String result = "";
		int commaCounter = listOfSessions.length - 1;
                for (int i=0;i<listOfSessions.length;i++){
		//while (iter.hasNext()){
			result = result + "{\"attName\":\"name\",\"operand\":\"EQUALS\",\"@c\":\".AttValueFilter\",\"attValue\":\"" + listOfSessions[i].trim() + "\"}";
			if (commaCounter > 0) {
				result = result + ",";
			}
			commaCounter--;
		}
		
		return result;
       }
        
       
	
	public List<String> getSessionNames(String[] sessionNames, String url, boolean requireAuth, String user, String password, TaskListener listener, boolean dynamicUserId, String buildID, int buildNumber,
            String workPlacePath, int connConnTimeOut, int connReadTimeout, boolean advConfig, Utils utils) throws Exception{
		HttpURLConnection conn = null;
		//Utils utils = new Utils();
		listener.getLogger().print("Trying to get session ID for the session names supplied:\n");
		String apiURL = url + "/rest/sessions/list";
                List<String> sessionList = new ArrayList<String>();
                
                
		BufferedReader br = null;
		try {
			conn = utils.getVAPIConnection(apiURL, requireAuth, user, password, "POST", dynamicUserId, buildID, buildNumber, workPlacePath, listener, connConnTimeOut, connReadTimeout, advConfig);

			OutputStream os = conn.getOutputStream();
                        
                        String postData = this.postSessionData.replaceAll("######", buildPostDataSessionPart(sessionNames));
                        
                        //listener.getLogger().print("Post is: " + postData); 
                                               
			os.write(postData.getBytes(Charset.forName("UTF-8")));
			os.flush();

			if (checkResponseCode(conn)) {
				br = new BufferedReader(new InputStreamReader(conn.getInputStream(),Charset.forName("UTF-8")));
				StringBuilder result = new StringBuilder();
				String output;
				while ((output = br.readLine()) != null) {
					result.append(output);
				}

				JSONArray tmpArray = JSONArray.fromObject(result.toString());
				Iterator<JSONObject> iterator = tmpArray.iterator();
				JSONObject sessionObject = null;
				while (iterator.hasNext()) {
					sessionObject = iterator.next();
                                        SessionState st = extractSessionIDFromResponse(sessionObject);
                                        if (st != null){
                                            listener.getLogger().print("Found ID: " + st.getId() + " for session name: " + st.getName() +  "\n"); 
                                            //System.out.println("Found ID: " + st.getId() + " for session name: " + st.getName() +  "\n"); 
                                            sessionList.add(st.getId());
                                        }
					
				}
				
				
				
				

			}
		} catch (Exception e) {
                           e.printStackTrace(); 
			
		} finally {
                    if (conn != null){
                        conn.disconnect();
                    }
			                        
                    if (br != null){
                        br.close();
                    }
		}
                
                return sessionList;
	}
        
        private SessionState extractSessionIDFromResponse(JSONObject session) throws IOException, Exception{
		
                SessionState st = null;
            
		if (session.has("id")) {
                        st = new SessionState();
			st.setId(session.getString("id"));
                        st.setName(session.getString("name"));
		} 
		
                return st;
	}
	
	
	
	
	
	private boolean checkResponseCode(HttpURLConnection conn) {
		try {
			if (conn.getResponseCode() != HttpURLConnection.HTTP_OK && conn.getResponseCode() != HttpURLConnection.HTTP_NO_CONTENT && conn.getResponseCode() != HttpURLConnection.HTTP_ACCEPTED
					&& conn.getResponseCode() != HttpURLConnection.HTTP_CREATED && conn.getResponseCode() != HttpURLConnection.HTTP_PARTIAL && conn.getResponseCode() != HttpURLConnection.HTTP_RESET
					&& conn.getResponseCode() != 406) {
				System.out.println("Error - Got wrong response from /session/list request for session id - " + conn.getResponseCode());
				return false;
			} else {
				return true;
			}
		} catch (IOException e) {
			// MARK_BUILD_FAIL
			e.printStackTrace();
			return false;
		}
	}

	

}
