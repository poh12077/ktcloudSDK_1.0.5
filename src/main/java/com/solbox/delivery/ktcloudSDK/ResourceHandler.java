package com.solbox.delivery.ktcloudSDK;

import org.json.JSONObject;

import java.net.HttpURLConnection;
import java.util.logging.Logger;

import org.json.JSONArray;
import org.json.JSONException;
import org.omg.PortableInterceptor.LOCATION_FORWARD;


class ResourceHandler {

    static void getToken() throws Exception {

    }

    static String getVm(String getVmUrl, String token, String serverName, String vmImageId, String specs, String networkId, String sshKeyName, int timeout) throws Exception {
        String requestBody = RequestBody.getVm(serverName, vmImageId, specs, networkId, sshKeyName);
        String result = RestAPI.post(getVmUrl, token, requestBody, timeout);
        String response = ResponseParser.statusCodeParser(result);
        String vmId = ResponseParser.VmCreateResponseParser(response);
        Etc.check(vmId);
        KTCloudOpenAPI.LOGGER.trace("VM creation has started");
        return vmId;
    }


    static String getVolume(String getVolumeUrl, String token, String volumeName, String volumeImageId, String projectId, int timeout) throws Exception {
        String requestBody = RequestBody.getVolume(volumeName, volumeImageId);
        String result = RestAPI.post(getVolumeUrl + projectId + "/volumes", token, requestBody, timeout);
        String response = ResponseParser.statusCodeParser(result);
        String volumeID = ResponseParser.volumeCreateResponseParser(response);
        Etc.check(volumeID);
        KTCloudOpenAPI.LOGGER.trace("volume creation has started");
        return volumeID;
    }

    static void connectVmAndVolume(String connectVmAndVolumeUrl, String token, String vmId, String volumeId, int timeout) throws Exception {
        String requestBody = RequestBody.connectVmAndVolume(volumeId);
        String result = RestAPI.post(connectVmAndVolumeUrl + vmId + "/os-volume_attachments", token, requestBody, timeout);
        ResponseParser.statusCodeParser(result);
        KTCloudOpenAPI.LOGGER.trace("connecting disk with VM has succeeded");
    }
    static String getPublicIp(String getPublicIpUrl, String token, int timeout, int resourceProcessingTimeoutBesideVm, int requestCycle) throws Exception {
        String publicIpJobId = "";
        String result = RestAPI.post(getPublicIpUrl, token, "", timeout);
        JSONObject fianlJsonObject = new JSONObject(result);
        String responseString = fianlJsonObject.getString("response");
        JSONObject response = new JSONObject(responseString);
        JSONObject nc_associateentpublicipresponse = response.getJSONObject("nc_associateentpublicipresponse");
        if (nc_associateentpublicipresponse.has("job_id")) {
            publicIpJobId = ResponseParser.IPCreateResponseParser(responseString);
            String publicIpId = ResponseParser.lookupPublicIpJobId(publicIpJobId,token,timeout,"public IP creation",resourceProcessingTimeoutBesideVm,requestCycle);
            Etc.check(publicIpId);
            return publicIpId;
        } else {
            throw new Exception();
        }
    }


    static String setStaticNat(String setStaticNatUrl, String token, String networkId, String vmPrivateIp, String publicIpId, int timeout) throws Exception {
        String requestBody = RequestBody.setStaticNat(vmPrivateIp, networkId, publicIpId);
        String result = RestAPI.post(setStaticNatUrl, token, requestBody, timeout);
        String response = ResponseParser.statusCodeParser(result);
        String staticNatId = ResponseParser.staticNATSettingResponseParser(response);
        Etc.check(staticNatId);
        KTCloudOpenAPI.LOGGER.trace("static NAT creation has succeeded");
        return staticNatId;
    }

   static String getSourceNetworkId(String firewallUrl, String token, int timeout) throws Exception {
        String result = RestAPI.get(firewallUrl,token,timeout);
        String response = ResponseParser.statusCodeParser(result);
        String sourceNetworkId = ResponseParser.parseFirewallList(response);
        return sourceNetworkId;
    }
    static String openFirewall(String openFirewallUrl, String token, String startPort, String endPort, String staticNatId,
                               String destinationNetworkAddress, String protocol, String destinationNetworkId, int timeout,
                               int resourceProcessingTimeoutBesideVm, int requestCycle ) throws Exception {
        int count = 0;
        while (true) {
            String sourceNetworkId = getSourceNetworkId(openFirewallUrl, token, timeout);
            if(sourceNetworkId.equals("")){
                KTCloudOpenAPI.LOGGER.trace("firewall activation has failed, since sourceNetwork ID is empty. check out the firewall outbound setting");
                throw new Exception();
            }
            String requestBody = RequestBody.openFirewall(startPort, endPort, staticNatId, sourceNetworkId, destinationNetworkAddress, protocol, destinationNetworkId);
            String result = RestAPI.post(openFirewallUrl, token, requestBody, timeout);
            String response = ResponseParser.statusCodeParser(result);
            String firewallJobId = ResponseParser.firewallJobIdParser(response);
            Etc.check(firewallJobId);
            boolean isFirewallOpened = ResponseParser.lookupJobId(firewallJobId, token, timeout, count,  "(port " + startPort + ") firewall activation", resourceProcessingTimeoutBesideVm, requestCycle);
            if (isFirewallOpened) {
                return firewallJobId;
            }

            count++;
            Thread.sleep(requestCycle * 1000);

            if (resourceProcessingTimeoutBesideVm <= count) {
                KTCloudOpenAPI.LOGGER.trace("(port " + startPort + ") firewall activation has failed");
                throw new Exception();
            }
        }
    }

    static boolean checkVmCreationStatus(String vmDetailUrl, String token, String vmId, int timeout, int maximumWaitingTime, int requestCycle) throws Exception {
        //KTCloudOpenAPI.LOGGER.trace("VM creation is in progress ");
        int count = 0;
        while (true) {
            String result = RestAPI.get(vmDetailUrl + vmId, token, timeout);
            String response = ResponseParser.statusCodeParser(result);
            JSONObject fianlJsonObject = new JSONObject(response);
            JSONObject server = fianlJsonObject.getJSONObject("server");
            int power_state = server.getInt("OS-EXT-STS:power_state");
            if (power_state == 1) {
                KTCloudOpenAPI.LOGGER.trace("VM creation has succeeded");
                return true;
            }
            Thread.sleep(requestCycle * 1000);
            count++;
            //KTCloudOpenAPI.LOGGER.trace("timer "+count);

            if (maximumWaitingTime <= count) {
                return false;
            }
        }
    }

    static boolean checkVolumeCreationStatus(String volumeStatusCheck, String token, String volumeId, String projectId, int timeout, int maximumWaitingTime, int requestCycle) throws Exception {
        //KTCloudOpenAPI.LOGGER.trace("volume creation is in progress ");
        int count = 0;
        while (true) {
            String result = RestAPI.get(volumeStatusCheck + projectId + "/volumes/" + volumeId, token, timeout);
            String response = ResponseParser.statusCodeParser(result);
            JSONObject fianlJsonObject = new JSONObject(response);
            JSONObject volume = fianlJsonObject.getJSONObject("volume");
            String status = volume.getString("status");
            if (status.equals("available")) {
                KTCloudOpenAPI.LOGGER.trace("volume creation has succeeded");
                return true;
            }
            Thread.sleep(requestCycle * 1000);
            count++;
            //KTCloudOpenAPI.LOGGER.trace("timer "+count);


            if (maximumWaitingTime <= count) {
                return false;
            }
        }
    }


    static boolean deleteVmOnly(String serverID, String token, int timeout) {
        try {
            if (serverID.length() == 0) {
                KTCloudOpenAPI.LOGGER.trace("VM deletion failed, since no VM id");
                return false;
            }
            String requestBody = RequestBody.forceDeleteVm();
            String result = RestAPI.post(KTCloudOpenAPI.forceDeleteVm_URL + serverID + "/action", token, requestBody,
                    timeout);
            return ResponseParser.statusCodeParserInDeletion(result, "VM deletion has started", "VM deletion failed");
        } catch (Exception e) {
            KTCloudOpenAPI.LOGGER.trace("VM deletion failed");
            KTCloudOpenAPI.LOGGER.trace(e.toString());
            return false;
        }
    }

    static boolean deleteVolume(String volumeID, String projectID, String token, int timeout, int maximumWaitingTime, int requestCycle) {
        try {
            if (volumeID.length() == 0) {
                KTCloudOpenAPI.LOGGER.trace("volume deletion has failed, since no volume id");
                return false;
            }
            KTCloudOpenAPI.LOGGER.trace("volume deletion has started");
            int count = 0;
            while (true) {
                String result = RestAPI.delete(KTCloudOpenAPI.deleteVolume_URL + projectID + "/volumes/" + volumeID, token,
                        timeout);
                if (ResponseParser.statusCodeParserInDeletion(result, "volume deletion has started", "")) {
                    return true;
                } else {
                    //System.out.print(count + " ");
                }
                count++;
                Thread.sleep(requestCycle * 1000);

                if (maximumWaitingTime <= count) {
                    KTCloudOpenAPI.LOGGER.trace("volume deletion failed");
                    return false;
                }
            }
        } catch (Exception e) {
            KTCloudOpenAPI.LOGGER.trace("volume deletion failed");
            KTCloudOpenAPI.LOGGER.trace(e.toString());
            return false;
        }
    }

    static boolean deleteStaticNat(String staticNatId, String token, int timeout, int maximumWaitingTime, int requestCycle) {
        try {
            if (staticNatId.length() == 0) {
                KTCloudOpenAPI.LOGGER.trace("static NAT activation has failed, since no static nat id");
                return false;
            }

            int count = 0;
            while (true) {
                String result = RestAPI.delete(KTCloudOpenAPI.DeleteStaticNAT_URL + staticNatId, token, timeout);
                JSONObject fianlJsonObject = new JSONObject(result);
                String responseString = fianlJsonObject.getString("response");
                JSONObject response = new JSONObject(responseString);
                JSONObject nc_disablestaticnatresponse = response.getJSONObject("nc_disablestaticnatresponse");
                if (nc_disablestaticnatresponse.has("job_id")) {
                    String jobId = nc_disablestaticnatresponse.getString("job_id");
                    boolean isStaticNatDeleted = ResponseParser.lookupJobId(jobId, token, timeout, count, "disabling static NAT", maximumWaitingTime, requestCycle);
                    if (isStaticNatDeleted) {
                        return true;
                    }
                } else {
                    KTCloudOpenAPI.LOGGER.trace(count + "static NAT deletion has failed, since no job id");
                }

                count++;
                Thread.sleep(requestCycle * 1000);

                if (maximumWaitingTime <= count) {
                    KTCloudOpenAPI.LOGGER.trace("static NAT deletion has failed");
                    return false;
                }
            }
        } catch (Exception e) {
            KTCloudOpenAPI.LOGGER.trace(e.toString());
            return false;
        }
    }

    static boolean deletePublicIp(String publicIpId, String token, int timeout, int maximumWaitingTime,
                                  int requestCycle) {
        try {
            if (publicIpId.length() == 0) {
                KTCloudOpenAPI.LOGGER.trace("public ip deletion has failed, since no public ip id");
                return false;
            }
            int count = 0;
            while (true) {
                String result = RestAPI.delete(KTCloudOpenAPI.deleteIP_URL + publicIpId, token, timeout);
                JSONObject fianlJsonObject = new JSONObject(result);
                String responseString = fianlJsonObject.getString("response");
                JSONObject response = new JSONObject(responseString);
                JSONObject nc_disassociateentpublicipresponse = response.getJSONObject("nc_disassociateentpublicipresponse");
                if (nc_disassociateentpublicipresponse.has("job_id")) {
                    String jobId = nc_disassociateentpublicipresponse.getString("job_id");
                    boolean isPublicIpDeleted = ResponseParser.lookupJobId(jobId, token, timeout, count, "Public IP deletion ", maximumWaitingTime, requestCycle);
                    if (isPublicIpDeleted) {
                        return true;
                    }
                } else {
                    //System.out.print(count + " ");
                }
                count++;
                Thread.sleep(requestCycle * 1000);

                if (maximumWaitingTime <= count) {
                    KTCloudOpenAPI.LOGGER.trace("public IP deletion has failed");
                    return false;
                }
            }
        } catch (Exception e) {
            KTCloudOpenAPI.LOGGER.trace(e.toString());
            return false;
        }
    }

    static boolean closeFirewall(String firewallJobId, String token, int timeout, int maximumWaitingTime, int requestCycle) {
        try {
            if (firewallJobId.length() == 0) {
                KTCloudOpenAPI.LOGGER.trace("disabling firewall has failed, since no firewall job id");
                return false;
            }
            int count = 0;
            while (true) {
                String response = ResponseParser.lookupJobId(firewallJobId, token, timeout);
                String firewallId = ResponseParser.firewallIdParser(response);
                String result = RestAPI.delete(KTCloudOpenAPI.closeFirewall_URL + firewallId, token, timeout);
                response = ResponseParser.statusCodeParser(result);
                JSONObject fianlJsonObject = new JSONObject(response);
                JSONObject nc_deletefirewallruleresponse = fianlJsonObject.getJSONObject("nc_deletefirewallruleresponse");
                String jobId = nc_deletefirewallruleresponse.getString("job_id");
                boolean isFirewallClosed = ResponseParser.lookupJobId(jobId, token, timeout, count, "disabling firewall ", maximumWaitingTime, requestCycle);
                if (isFirewallClosed) {
                    return true;
                }

                count++;
                Thread.sleep(requestCycle * 1000);

                if (maximumWaitingTime <= count) {
                    KTCloudOpenAPI.LOGGER.trace("disabling firewall has failed");
                    return false;
                }
            }
        } catch (Exception e) {
            KTCloudOpenAPI.LOGGER.trace(e.toString());
            return false;
        }
    }


}
