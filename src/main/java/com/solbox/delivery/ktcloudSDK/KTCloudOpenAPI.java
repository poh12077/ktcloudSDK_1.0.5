package com.solbox.delivery.ktcloudSDK;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KTCloudOpenAPI {

    static Logger LOGGER = LoggerFactory.getLogger(KTCloudOpenAPI.class);

    static final String getVm_URL = "https://api.ucloudbiz.olleh.com/d1/server/servers";
    static final String forceDeleteVm_URL = "https://api.ucloudbiz.olleh.com/d1/server/servers/";
    static final String VmList_URL = "https://api.ucloudbiz.olleh.com/d1/server/servers/detail";
    static final String VmDetail_URL = "https://api.ucloudbiz.olleh.com/d1/server/servers/";

    static final String getVolume_URL = "https://api.ucloudbiz.olleh.com/d1/volume/";
    static String volumeStatusCheck = "https://api.ucloudbiz.olleh.com/d1/volume/";
    static final String connectVmAndVolume_URL = "https://api.ucloudbiz.olleh.com/d1/server/servers/";
    static final String deleteAllVolume_URL = "https://api.ucloudbiz.olleh.com/d1/volume/";
    static final String deleteVolume_URL = "https://api.ucloudbiz.olleh.com/d1/volume/";
    static final String getIP_URL = "https://api.ucloudbiz.olleh.com/d1/nc/IpAddress";
    static final String deleteIP_URL = "https://api.ucloudbiz.olleh.com/d1/nc/IpAddress/";
    static final String IPList_URL = "https://api.ucloudbiz.olleh.com/d1/nc/IpAddress";

    static final String setStaticNAT_URL = "https://api.ucloudbiz.olleh.com/d1/nc/StaticNat";
    static final String staticNATList_URL = "https://api.ucloudbiz.olleh.com/d1/nc/StaticNat";
    static final String DeleteStaticNAT_URL = "https://api.ucloudbiz.olleh.com/d1/nc/StaticNat/";

    static final String openFirewall_URL = "https://api.ucloudbiz.olleh.com/d1/nc/Firewall";
    static final String firewall_List_URL = "https://api.ucloudbiz.olleh.com/d1/nc/Firewall";
    static final String closeFirewall_URL = "https://api.ucloudbiz.olleh.com/d1/nc/Firewall/";

    static final String getToken_URL = "https://api.ucloudbiz.olleh.com/d1/identity/auth/tokens";
    static final String jobID_URL = "https://api.ucloudbiz.olleh.com/d1/nc/Etc?command=queryAsyncJob&jobid=";

    static final String GET = "GET";
    static final String DELETE = "DELETE";
    static final String POST = "POST";

    public static ServerInformation createServer(String serverName, String serverImage, String specs, int timeout, String accountId, String accountPassword,
                                                 String networkId, String destinationNetworkId, String destinationNetworkAddress) throws Exception {
        ServerInformation serverInformation = new ServerInformation();

        try {
            String result;
            String protocol = "ALL";
            String inputPort = "1935";
            String outputPort = "80";
            String sourceNetworkId = "6b812762-c6bc-4a6d-affb-c469af1b4342";
            int maximumWatingTimeGenerally = 30;
            int maximumWatingTimeForVm = 500;
            int requestCycle=1;

            LOGGER.trace("Server creation has started");

            serverInformation.setNetworkId(networkId);
            // token
            result = RestAPI.post(getToken_URL, RequestBody.getToken(accountId, accountPassword), timeout);
            String token = ResponseParser.statusCodeParser(result);
            Etc.check(token);
            LOGGER.trace("token has been issued");
            String projectId = ResponseParser.getProjectIdFromToken(result);
            Etc.check(projectId);
            LOGGER.trace("project id has been issued");
            serverInformation.setProjectId(projectId);
            String vmId = ResourceHandler.getVm(getVm_URL, token, serverName, serverImage, specs, timeout);
            serverInformation.setVmId(vmId);
            String publicIpId = ResourceHandler.getPublicIp(getIP_URL, token, timeout, maximumWatingTimeGenerally, requestCycle);
            serverInformation.setPublicIpId(publicIpId);
            boolean isVmCreated = ResourceHandler.checkVmCreationStatus(VmDetail_URL, token, vmId, timeout, maximumWatingTimeForVm, requestCycle);
            String vmPrivateIp = "";
            if (isVmCreated) {
                vmPrivateIp = ResponseParser.lookupVmPrivateIp(VmDetail_URL, token, vmId, timeout);
            } else {
                LOGGER.trace("vm creation error");
                deleteServer(serverInformation, timeout, accountId, accountPassword);
                throw new Exception();
            }
            String staticNatId = ResourceHandler.setStaticNat(setStaticNAT_URL, token, networkId, vmPrivateIp, publicIpId, timeout);
            serverInformation.setStaticNatId(staticNatId);
            String firewallJobIdOfInputPort = ResourceHandler.openFirewall(openFirewall_URL, token, inputPort, inputPort, staticNatId, sourceNetworkId,
                    destinationNetworkAddress, protocol, destinationNetworkId, timeout, maximumWatingTimeGenerally, requestCycle);
            serverInformation.setFirewallJobIdOfInputPort(firewallJobIdOfInputPort);
            String firewallJobIdOfOutputPort = ResourceHandler.openFirewall(openFirewall_URL, token, outputPort, outputPort, staticNatId, sourceNetworkId,
                    destinationNetworkAddress, protocol, destinationNetworkId, timeout, maximumWatingTimeGenerally, requestCycle);
            serverInformation.setFirewallJobIdOfOutputPort(firewallJobIdOfOutputPort);

            LOGGER.trace("server has been created");
            return serverInformation;
        } catch (Exception e) {
            LOGGER.trace(e.toString());
            LOGGER.trace("server creation failed");
            LOGGER.trace("rollback has been started");
            KTCloudOpenAPI.deleteServer(serverInformation, timeout, accountId, accountPassword);
            LOGGER.trace("rollback has been done");
            throw new Exception();
        }
    }

    public static ServerInformation createServer(String serverName, String volumeName, String serverImage, String volumeImage, String specs, int timeout, String accountId, String accountPassword,
                                                 String networkId, String destinationNetworkId, String destinationNetworkAddress) throws Exception {
        ServerInformation serverInformation = new ServerInformation();
        try {
            String result;
            String protocol = "ALL";
            String inputPort = "1935";
            String outputPort = "80";
            String sourceNetworkId = "6b812762-c6bc-4a6d-affb-c469af1b4342";
            int maximumWatingTimeGenerally = 30;
            int maximumWatingTimeForVm = 500;
            int requestCycle=1;

            LOGGER.trace("Server creation has started");

            serverInformation.setNetworkId(networkId);
            // token
            result = RestAPI.post(getToken_URL, RequestBody.getToken(accountId, accountPassword), timeout);
            String token = ResponseParser.statusCodeParser(result);
            Etc.check(token);
            String projectId = ResponseParser.getProjectIdFromToken(result);
            Etc.check(projectId);
            serverInformation.setProjectId(projectId);
            String vmId = ResourceHandler.getVm(getVm_URL, token, serverName, serverImage, specs, timeout);
            serverInformation.setVmId(vmId);
            String volumeId = ResourceHandler.getVolume(getVolume_URL, token, volumeName, volumeImage, projectId, timeout);
            serverInformation.setVolumeId(volumeId);
            String publicIpId = ResourceHandler.getPublicIp(getIP_URL, token, timeout, maximumWatingTimeGenerally, requestCycle);
            serverInformation.setPublicIpId(publicIpId);
            boolean isVolumeCreated = ResourceHandler.checkVolumeCreationStatus(volumeStatusCheck , token, volumeId, projectId, timeout, maximumWatingTimeGenerally, requestCycle);
            boolean isVmCreated = ResourceHandler.checkVmCreationStatus(VmDetail_URL, token, vmId, timeout, maximumWatingTimeForVm, requestCycle);

            if (isVmCreated && isVolumeCreated ) {
                ResourceHandler.connectVmAndVolume(connectVmAndVolume_URL, token, vmId, volumeId, timeout);
            } else {
                LOGGER.trace("vm or volume creation error");
                deleteServer(serverInformation, timeout, accountId, accountPassword);
                throw new Exception();
            }

            String vmPrivateIp = ResponseParser.lookupVmPrivateIp(VmDetail_URL, token, vmId, timeout);
            String staticNatId = ResourceHandler.setStaticNat(setStaticNAT_URL, token, networkId, vmPrivateIp, publicIpId, timeout);
            serverInformation.setStaticNatId(staticNatId);
            String firewallJobIdOfInputPort = ResourceHandler.openFirewall(openFirewall_URL, token, inputPort, inputPort, staticNatId, sourceNetworkId,
                    destinationNetworkAddress, protocol, destinationNetworkId, timeout, maximumWatingTimeGenerally, requestCycle);
            serverInformation.setFirewallJobIdOfInputPort(firewallJobIdOfInputPort);
            String firewallJobIdOfOutputPort = ResourceHandler.openFirewall(openFirewall_URL, token, outputPort, outputPort, staticNatId, sourceNetworkId,
                    destinationNetworkAddress, protocol, destinationNetworkId, timeout, maximumWatingTimeGenerally, requestCycle);
            serverInformation.setFirewallJobIdOfOutputPort(firewallJobIdOfOutputPort);

            LOGGER.trace("server has been created");
            return serverInformation;
        } catch (Exception e) {
            LOGGER.trace(e.toString());
            LOGGER.trace("server creation failed");
            LOGGER.trace("rollback has been started");
            KTCloudOpenAPI.deleteServer(serverInformation, timeout, accountId, accountPassword);
            LOGGER.trace("rollback has been done");
            throw new Exception();
        }
    }


    public static String deleteServer(ServerInformation serverInformation, int timeout, String accountId, String accountPassword) throws Exception {
        boolean isVmDeleleted = false;
        boolean isVolumeDeleleted = false;
        boolean isFirewallOfInputPortCloseed = false;
        boolean isFirewallOfOutputPortCloseed = false;
        boolean isStaticNatDisabled = false;
        boolean isPublicIpDeleleted = false;
        int maximumWatingTimeGenerally = 30;
        int requestCycle=1;

        try {
            LOGGER.trace("server deletion has started");
            // token
            String response = RestAPI.post(getToken_URL, RequestBody.getToken(accountId, accountPassword), timeout);
            String token = ResponseParser.statusCodeParser(response);
            Etc.check(token);
            LOGGER.trace("token has been issued");
            isVmDeleleted = ResourceHandler.deleteVmOnly(serverInformation.getVmId(), token, timeout);
            isVolumeDeleleted = ResourceHandler.deleteVolume(serverInformation.getVolumeId(), serverInformation.getProjectId(), token, timeout, maximumWatingTimeGenerally, requestCycle);
            isFirewallOfInputPortCloseed = ResourceHandler.closeFirewall(serverInformation.getFirewallJobIdOfInputPort(), token, timeout, maximumWatingTimeGenerally, requestCycle);
            isFirewallOfOutputPortCloseed = ResourceHandler.closeFirewall(serverInformation.getFirewallJobIdOfOutputPort(), token, timeout, maximumWatingTimeGenerally, requestCycle);
            isStaticNatDisabled = ResourceHandler.deleteStaticNat(serverInformation.getStaticNatId(), token, timeout, maximumWatingTimeGenerally, requestCycle);
            isPublicIpDeleleted = ResourceHandler.deletePublicIp(serverInformation.getPublicIpId(), token, timeout, maximumWatingTimeGenerally, requestCycle);

            LOGGER.trace("server has been deleted");

            JSONObject result = new JSONObject();
            result.put("isVmDeleleted", isVmDeleleted);
            result.put("isPublicIpDeleleted", isPublicIpDeleleted);
            result.put("isFirewallOfInputPortCloseed", isFirewallOfInputPortCloseed);
            result.put("isFirewallOfOutputPortCloseed", isFirewallOfOutputPortCloseed);
            result.put("isStaticNatDisabled", isStaticNatDisabled);
            result.put("isVolumeDeleleted", isVolumeDeleleted);
            LOGGER.trace(result.toString());
            return result.toString();
        } catch (Exception e) {
            LOGGER.trace("server deletion failed");
            LOGGER.trace(e.toString());
            JSONObject result = new JSONObject();
            result.put("isVmDeleleted", isVmDeleleted);
            result.put("isPublicIpDeleleted", isPublicIpDeleleted);
            result.put("isFirewallOfInputPortCloseed", isFirewallOfInputPortCloseed);
            result.put("isFirewallOfOutputPortCloseed", isFirewallOfOutputPortCloseed);
            result.put("isStaticNatDisabled", isStaticNatDisabled);
            result.put("isVolumeDeleleted", isVolumeDeleleted);
            LOGGER.trace(result.toString());
            return result.toString();
        }
    }

    static void init(String confPath, String accountId, String accountPassword) throws Exception {
        //read conf
        String confString = Etc.read(confPath);
        JSONObject conf = new JSONObject(confString);
        JSONObject http = conf.getJSONObject("http");
        int timeout = http.getInt("timeout");

        String result;
        String response;
        result = RestAPI.request(getToken_URL, POST, RequestBody.getToken(accountId, accountPassword));
        String token = ResponseParser.statusCodeParser(result);
        String projectID = ResponseParser.getProjectIdFromToken(result);

        // close firewall
        result = RestAPI.request(firewall_List_URL, GET, token, "");
        response = ResponseParser.statusCodeParser(result);
        Initialization.closeAllFirewall(response, token);

        // unlock static NAT
        result = RestAPI.request(staticNATList_URL, GET, token, "");
        response = ResponseParser.statusCodeParser(result);
        Initialization.deleteAllStaticNAT(response, token);

        // delete ip
        result = RestAPI.request(IPList_URL, GET, token, "");
        response = ResponseParser.statusCodeParser(result);
        Initialization.deleteAllIP(response, token);

        // delete vm
        result = RestAPI.request(VmList_URL, GET, token, "");
        response = ResponseParser.statusCodeParser(result);
        Initialization.deleteAllVm(response, token);

        //delete volume
        result = RestAPI.get(deleteAllVolume_URL + projectID + "/volumes/detail", token, timeout);
        response = ResponseParser.statusCodeParser(result);
        Initialization.deleteAllVolume(response, token, projectID, timeout);

        System.out.println("initialization is done");
    }

    static void lookup() throws Exception {
    }

}
