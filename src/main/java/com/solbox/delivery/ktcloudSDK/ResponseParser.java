package com.solbox.delivery.ktcloudSDK;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.HttpURLConnection;

class ResponseParser {

//	static String statusCodeParser(String result) throws Exception {
//		JSONObject jsonResult = new JSONObject(result);
//		if (jsonResult.getInt("statusCode") == HttpURLConnection.HTTP_CREATED
//				|| jsonResult.getInt("statusCode") == HttpURLConnection.HTTP_OK
//				|| jsonResult.getInt("statusCode") == HttpURLConnection.HTTP_ACCEPTED) {
//			return jsonResult.getString("response");
//		} else {
//			// fail logic should be here
//			System.out.println( jsonResult.getInt("statusCode") );
//			throw new Exception();
//		}
//	}

    static String statusCodeParser(String result) throws Exception {
        JSONObject jsonResult = new JSONObject(result);
        if (400 <= jsonResult.getInt("statusCode") && jsonResult.getInt("statusCode") < 600) {
            throw new Exception();
        } else {
            return jsonResult.getString("response");
        }
    }

    static boolean statusCodeParserInDeletion(String result, String successLog, String failLog) throws Exception {
        JSONObject jsonResult = new JSONObject(result);
        if (400 <= jsonResult.getInt("statusCode") && jsonResult.getInt("statusCode") < 600) {
            if (!failLog.equals("")) {
                KTCloudOpenAPI.LOGGER.trace(failLog);
            }
            return false;
        } else {
            KTCloudOpenAPI.LOGGER.trace(successLog);
            return true;
        }
    }


//	static String getProjectIdFromToken(String result) throws Exception {
//		JSONObject jsonResult = new JSONObject(result);
//		if (jsonResult.getInt("statusCode") == HttpURLConnection.HTTP_CREATED
//				|| jsonResult.getInt("statusCode") == HttpURLConnection.HTTP_OK
//				|| jsonResult.getInt("statusCode") == HttpURLConnection.HTTP_ACCEPTED) {
//			return jsonResult.getString("projectID");
//		} else {
//			// fail logic should be here
//			System.out.println( jsonResult.getInt("statusCode") );
//			throw new Exception();
//		}
//	}

    static String getProjectIdFromToken(String result) throws Exception {
        JSONObject jsonResult = new JSONObject(result);
        if (400 <= jsonResult.getInt("statusCode") && jsonResult.getInt("statusCode") < 600) {
            KTCloudOpenAPI.LOGGER.trace("token issued failed");
            throw new Exception();
        } else {
            return jsonResult.getString("projectID");
        }
    }

    static String VmCreateResponseParser(String response) throws JSONException {
        JSONObject fianlJsonObject = new JSONObject(response);
        JSONObject server = fianlJsonObject.getJSONObject("server");
        String ID = server.getString("id");
        return ID;
    }

    static String VmDetailResponseParser(String response, String networkName) throws JSONException {
        JSONObject fianlJsonObject = new JSONObject(response);
        JSONObject server = fianlJsonObject.getJSONObject("server");
        JSONObject addresses = server.getJSONObject("addresses");
        JSONArray Private = addresses.getJSONArray(networkName);
        String privateIP = "";
        for (int i = 0; i < Private.length(); i++) {
            JSONObject jsonObject = Private.getJSONObject(i);
            privateIP = jsonObject.getString("addr");
        }
        return privateIP;
    }

    static String volumeCreateResponseParser(String response) throws JSONException {
        JSONObject fianlJsonObject = new JSONObject(response);
        JSONObject volume = fianlJsonObject.getJSONObject("volume");
        String ID = volume.getString("id");
        return ID;
    }

    static String IPCreateResponseParser(String response) throws JSONException {
        JSONObject fianlJsonObject = new JSONObject(response);
        JSONObject nc_associateentpublicipresponse = fianlJsonObject.getJSONObject("nc_associateentpublicipresponse");
        String job_id = nc_associateentpublicipresponse.getString("job_id");
        return job_id;
    }

    static String staticNATSettingResponseParser(String response) throws Exception {
        JSONObject fianlJsonObject = new JSONObject(response);
        JSONObject nc_enablestaticnatresponse = fianlJsonObject.getJSONObject("nc_enablestaticnatresponse");
        String staticNAT_ID = "";
        if (nc_enablestaticnatresponse.getBoolean("success") == true) {
            staticNAT_ID = nc_enablestaticnatresponse.getString("id");
            return staticNAT_ID;
        } else {
            KTCloudOpenAPI.LOGGER.trace("static NAT creation failed");
            throw new Exception();
        }
    }

    static String projectIDParser(String response) throws JSONException {
        JSONObject fianlJsonObject = new JSONObject(response);
        JSONObject token = fianlJsonObject.getJSONObject("token");
        JSONObject project = token.getJSONObject("project");
        String ID = project.getString("id");
        return ID;
    }

    static String firewallJobIdParser(String response) throws Exception {
        JSONObject fianlJsonObject = new JSONObject(response);
        JSONObject nc_createfirewallruleresponse = fianlJsonObject.getJSONObject("nc_createfirewallruleresponse");
        String jobId = nc_createfirewallruleresponse.getString("job_id");
        return jobId;
    }

    static String firewallIdParser(String response) throws JSONException {
        JSONObject fianlJsonObject = new JSONObject(response);
        JSONObject nc_queryasyncjobresultresponse = fianlJsonObject.getJSONObject("nc_queryasyncjobresultresponse");
        JSONObject result = nc_queryasyncjobresultresponse.getJSONObject("result");
        String firewallId = "";
        if (result.getBoolean("success") == true) {
            firewallId = result.getString("id");
            return firewallId;
        } else {
            return firewallId;
        }
    }

    static String parseFirewallList(String response) throws JSONException {

        String sourceNetworkId = "";
        JSONObject fianlJsonObject = new JSONObject(response);
        JSONObject nc_listfirewallrulesresponse = fianlJsonObject.getJSONObject("nc_listfirewallrulesresponse");
        JSONArray firewallrules = nc_listfirewallrulesresponse.getJSONArray("firewallrules");

        for (int i = 0; i < firewallrules.length(); i++) {
            JSONObject jsonObject0 = firewallrules.getJSONObject(i);
            JSONArray acls = jsonObject0.getJSONArray("acls");
            for (int j = 0; j < acls.length(); j++) {
                JSONObject firewall = acls.getJSONObject(j);
                JSONArray dstintfs = firewall.getJSONArray("dstintfs");
                for (int k = 0; k < dstintfs.length(); k++) {
                    JSONObject destinationNetwork = dstintfs.getJSONObject(k);
                    String networkname = destinationNetwork.getString("networkname");
                    if (networkname.equals("external")) {
                        sourceNetworkId = destinationNetwork.getString("networkid");
                        break;
                    }
                }
                   if(0 < sourceNetworkId.length()) {break;}
            }
            if(0 < sourceNetworkId.length()) {break;}
        }
        return sourceNetworkId;
    }


    static String lookupJobId(String jobId, String token, int timeout) throws Exception {
        String result = RestAPI.get(KTCloudOpenAPI.jobID_URL + jobId, token, timeout);
        String response = ResponseParser.statusCodeParser(result);
        return response;
    }

    static boolean lookupJobId(String jobId, String token, int timeout, int deletionCount, String log, int maximumWaitingTime, int requestCycle) throws Exception {
        int waitingCount = 0;

        while (true) {
            String result = RestAPI.get(KTCloudOpenAPI.jobID_URL + jobId, token, timeout);
            String response = ResponseParser.statusCodeParser(result);
            JSONObject fianlJsonObject = new JSONObject(response);
            JSONObject nc_queryasyncjobresultresponse = fianlJsonObject.getJSONObject("nc_queryasyncjobresultresponse");
            int state = nc_queryasyncjobresultresponse.getInt("state");
            if (state == 1) {
                KTCloudOpenAPI.LOGGER.trace(log + " has been done");
                return true;
            } else if (state == 0) {
                //waiting for completion
                KTCloudOpenAPI.LOGGER.trace(waitingCount + " " + log + " is in progress");
                waitingCount++;
                Thread.sleep(requestCycle * 1000);

            } else if (state == 2) {
                KTCloudOpenAPI.LOGGER.trace(deletionCount + " " + log + " failed");
                return false;
            } else {
                KTCloudOpenAPI.LOGGER.trace(log + " error");
                return false;
            }

            if (maximumWaitingTime <= waitingCount) {
                KTCloudOpenAPI.LOGGER.trace(log + " failed");
                return false;
            }

        }
    }

    static String lookupPublicIpJobId(String jobId, String token, int timeout, String log, int maximumWaitingTime, int requestCycle) throws Exception {
        int waitingCount = 0;

        while (true) {
            String result = RestAPI.get(KTCloudOpenAPI.jobID_URL + jobId, token, timeout);
            String response = ResponseParser.statusCodeParser(result);
            JSONObject fianlJsonObject = new JSONObject(response);
            JSONObject nc_queryasyncjobresultresponse = fianlJsonObject.getJSONObject("nc_queryasyncjobresultresponse");
            int state = nc_queryasyncjobresultresponse.getInt("state");
            if (state == 1) {
                KTCloudOpenAPI.LOGGER.trace(log + " has been done");
                JSONObject resultJson = nc_queryasyncjobresultresponse.getJSONObject("result");
                String publicIpId = resultJson.getString("id");
                return publicIpId;
            } else if (state == 0) {
                KTCloudOpenAPI.LOGGER.trace(waitingCount + " " + log + " is in progress");
                waitingCount++;
                Thread.sleep(requestCycle * 1000);

            } else if (state == 2) {
                KTCloudOpenAPI.LOGGER.trace(log + " failed");
                throw new Exception();
            } else {
                KTCloudOpenAPI.LOGGER.trace(log + " error");
                throw new Exception();
            }

            if (maximumWaitingTime <= waitingCount) {
                KTCloudOpenAPI.LOGGER.trace(log + " failed");
                throw new Exception();
            }

        }
    }


    static String lookupVmPrivateIp(String vmDetailUrl, String token, String vmId, String networkName, int timeout) throws Exception {
        String result = RestAPI.get(vmDetailUrl + vmId, token, timeout);
        String response = ResponseParser.statusCodeParser(result);
        String vmPrivateIp = ResponseParser.VmDetailResponseParser(response, networkName);
        Etc.check(vmPrivateIp);
        return vmPrivateIp;
    }


}
