package com.viglet.turing.index;

import com.opentext.demo.otsn.rest.RestClient;
import com.opentext.demo.otsn.rest.StatusMessage;

import org.apache.commons.httpclient.methods.RequestEntity;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.json.JSONObject;

import java.util.Properties;
import java.io.InputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 * Created by IntelliJ IDEA.
 * User: sakella
 * Date: Jun 17, 2011
 * Time: 11:58:48 AM
 * To change this template use File | Settings | File Templates.
 */
public class IndexerUtil {

    private static final String BASE_URL = "http://util1.saas.nstein.net/rest";
    private static final String AUTHENTICATION_URL = BASE_URL + "/authenticate?";
    private static final String INDEX_CLONING_URL = BASE_URL + "/cloneindex?";
    private static final String INDEX_RESET_URL = BASE_URL + "/resetindex?";
    private static final String INDEX_DELETE_URL = BASE_URL + "/deleteindex?";
    private static final String INDEX_LIST_URL = BASE_URL + "/listindexes?";
    private static Properties props = new Properties();

   public static void main(String args[])throws Exception{
        BufferedReader commandBuffer = null;
        try
        {
            InputStream iStream = IndexerUtil.class.getClassLoader().getResourceAsStream("index-credentials.properties");
            if(iStream != null){
                 props.load(iStream);
            }
            String userName = props.getProperty("username");
            String password = props.getProperty("password");
            if(userName == null || password == null){
                System.out.println("Properties missing, make sure the properties [username] and [password] are properly configured in [index-credentials.properties]");
                System.exit(-1);
            }
            commandBuffer = new BufferedReader(new InputStreamReader(System.in));
            System.out.println();
            System.out.println("OTSN Indexing Operations.....");
            boolean prompt = true;
            while(prompt){
                System.out.flush();
                System.out.println();
                System.out.println("1. List Indexes");
                System.out.println("2. Clone Index");
                System.out.println("3. Reset Index");
                System.out.println("4. Delete Index");
                System.out.println("5. Exit");
                //System.out.println("6. Authenticate");
                System.out.println();
                System.out.println("Select one of the above operations:");
                System.out.flush();
                int selection = 0;
                try{
                selection = Integer.parseInt(commandBuffer.readLine().trim());
                }catch(Exception e){
                    selection = -1;
                }
                switch(selection)
                {
                    case 1:
                    {
                        listIndexes(userName, password);
                         break;
                    }
                    case 2:
                    {
                        System.out.println("Enter the name of Index to be cloned:");
                        System.out.flush();
                        String sourceIndex = commandBuffer.readLine().trim();
                        System.out.println("Enter a name for the new Index:");
                        System.out.flush();
                        String targetIndex = commandBuffer.readLine().trim();
                        //System.out.println("cloning " + sourceIndex + " to " + targetIndex);
                        cloneIndex(userName, password, sourceIndex, targetIndex);
                         break;
                    }
                    case 3:
                    {
                        System.out.println("Enter the name of Index that has to be used as source:");
                        System.out.flush();
                        String sourceIndex = commandBuffer.readLine().trim();
                        System.out.println("Enter the name of the Index that need to be reset:");
                        System.out.flush();
                        String targetIndex = commandBuffer.readLine().trim();
                        //System.out.println("Resetting " + sourceIndex + " to " + targetIndex);
                        resetIndex(userName, password, sourceIndex, targetIndex);
                         break;
                    }
                     case 4:
                    {
                        System.out.println("Enter the name of Index that has to be deleted:");
                        System.out.flush();
                        String sourceIndex = commandBuffer.readLine().trim();
                        System.out.println("Are you sure you want to delete the Index[ " + sourceIndex + " ] Y/N:");
                        System.out.flush();
                        String response = commandBuffer.readLine().trim();
                        if("y".equalsIgnoreCase(response)){
                            //System.out.println("Deleting the index: " + sourceIndex);
                            deleteIndex(userName, password, sourceIndex);
                        }

                         break;
                    }
                    case 5:
                    {
                        prompt = false;
                        break;
                    }
                    /*
                    case 6:
                    {
                        authenticate(userName, password);
                         break;
                    }
                    */

                    default:
                    {
                        System.out.println("Invalid Input, try again.");
                        break;
                    }
                }
            }

        }catch(Exception e){
            e.printStackTrace();
        }

    }

    public static String authenticate(String userName, String password){
      String restUrl = AUTHENTICATION_URL + "username=" + userName + "&password=" + password;
      System.out.println("Initiating the Authentication...");
      StatusMessage status = RestClient.performGet(restUrl, null);
      String authToken = parseJsonForAuthToken(status.getResponseBody());
      if(authToken == null){
           System.out.println("Failed to authenticate, validate the credentials");
      }else{
          System.out.println("Authentication Successfull.");
      }
      return authToken;
    }

    public static void listIndexes(String userName, String password){
        try{
          String authToken = authenticate(userName, password);
          if(authToken == null){
               return;
          }
          String restUrl = INDEX_LIST_URL + "cid=" + userName + "&token=" + authToken;
          StatusMessage status = RestClient.performGet(restUrl, null);
          parseJsonForIndexList(status.getResponseBody());
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    public static void cloneIndex(String userName, String password, String sourceIndex, String targetIndex){
        try{
          String authToken = authenticate(userName, password);
          if(authToken == null){
               return;
          }
          String restUrl = INDEX_CLONING_URL + "cid=" + userName + "&token=" + authToken;
          String cloningRequest = "{\"CLONE\":\"" + targetIndex + "\",\"MASTER\":\"" + sourceIndex + "\"}";
          RequestEntity body = new StringRequestEntity(cloningRequest, "application/json", "UTF-8");
          long startTime = System.currentTimeMillis();
          System.out.println("Initiating the Index cloning: " + sourceIndex + " --> " + targetIndex);
          StatusMessage status = RestClient.performPost(restUrl, body);
          long totalTime = (System.currentTimeMillis() - startTime) / 1000;
          System.out.println("Index Cloning Operation took [" + totalTime + "] Seconds.");
          System.out.println("Index Cloning Status:" + parseJsonForMsg(status.getResponseBody()));
        }catch (Exception e){
            e.printStackTrace();
        }
   }

    public static void resetIndex(String userName, String password, String sourceIndex, String targetIndex){
        try{
          String authToken = authenticate(userName, password);
          if(authToken == null){
               return;
          }
          String restUrl = INDEX_RESET_URL + "cid=" + userName + "&token=" + authToken;
          String cloningRequest = "{\"CLONE\":\"" + targetIndex + "\",\"MASTER\":\"" + sourceIndex + "\"}";
          RequestEntity body = new StringRequestEntity(cloningRequest, "application/json", "UTF-8");
          long startTime = System.currentTimeMillis();
          System.out.println("Initiating the Index Reset: " + sourceIndex + " --> " + targetIndex);
          StatusMessage status = RestClient.performPut(restUrl, body);
          long totalTime = (System.currentTimeMillis() - startTime) / 1000;
          System.out.println("Index Reset Operation took [" + totalTime + "] Seconds.");
          System.out.println("Index Reset Status:" + parseJsonForMsg(status.getResponseBody()));
        }catch (Exception e){
            e.printStackTrace();
        }
   }
    public static void deleteIndex(String userName, String password, String index){
        try{
          String authToken = authenticate(userName, password);
          if(authToken == null){
               return;
          }
          String restUrl = INDEX_DELETE_URL + "cid=" + userName + "&token=" + authToken + "&index=" + index;
          long startTime = System.currentTimeMillis();
          System.out.println("Initiating the Index Delete for: " + index);
          StatusMessage status = RestClient.performDelete(restUrl, null);
          long totalTime = (System.currentTimeMillis() - startTime) / 1000;
          System.out.println("Index Delete Operation took [" + totalTime + "] Seconds.");
          System.out.println("Index Delete Status:" + parseJsonForMsg(status.getResponseBody()));
        }catch (Exception e){
            e.printStackTrace();
        }
   }

    private static String parseJsonForAuthToken(String jsonString){
        String token = null;
        try{
        JSONObject json = new JSONObject(jsonString);
        if(json.has("Token")){
            token = json.getString("Token");
        }else{
            System.out.println("Failed to Authenticate");
        }

        }catch(Exception e){
            e.printStackTrace();
        }
       return token;
    }

    private static String parseJsonForMsg(String jsonString){
        String msg = null;
        try{
        JSONObject json = new JSONObject(jsonString);
        if(json.has("Msg")){
            msg = json.getString("Msg");
        }else{
            System.out.println("No Message found in the rest response");
        }

        }catch(Exception e){
            e.printStackTrace();
        }
       return msg;
    }

    public static void parseJsonForIndexList(String jsonStr)throws Exception{
       JSONObject json = new JSONObject(jsonStr);
       int indexCount = 0;
       System.out.println("");
       if(json.has("Index_Count")){
           indexCount = json.getInt("Index_Count");
           System.out.println("Total Index Count:" + indexCount);
       }
       if(json.has("Index")){
         System.out.println("Listing Indexes....");
         System.out.println("");
         JSONObject jobj = json.getJSONObject("Index");
         if(jobj != null){
             for(int i = 0; i < indexCount; i++){
                System.out.println("Index Name --> " + jobj.get(String.valueOf(i)));
             }
         }
      }
    }
}
