package com.solbox.delivery.ktcloudSDK;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KTCloudOpenAPI {

    static Logger LOGGER = LoggerFactory.getLogger(KTCloudOpenAPI.class);

    static final String getVm_URL = "https://api.ucloudbiz.olleh.com/d1/server/servers";
    static final String vmForceDeleteUrl = "https://api.ucloudbiz.olleh.com/d1/server/servers/";
    static final String vmListUrl = "https://api.ucloudbiz.olleh.com/d1/server/servers/detail";
    static final String VmDetail_URL = "https://api.ucloudbiz.olleh.com/d1/server/servers/";

    static final String getVolume_URL = "https://api.ucloudbiz.olleh.com/d1/volume/";
    static String volumeStatusCheck = "https://api.ucloudbiz.olleh.com/d1/volume/";
    static final String connectVmAndVolume_URL = "https://api.ucloudbiz.olleh.com/d1/server/servers/";
    static final String listOfAllVolumeUrl = "https://api.ucloudbiz.olleh.com/d1/volume/";
    static final String volumeDeleteUrl = "https://api.ucloudbiz.olleh.com/d1/volume/";
    static final String getIP_URL = "https://api.ucloudbiz.olleh.com/d1/nc/IpAddress";
    static final String deleteIP_URL = "https://api.ucloudbiz.olleh.com/d1/nc/IpAddress/";
    static final String IPList_URL = "https://api.ucloudbiz.olleh.com/d1/nc/IpAddress";

    static final String setStaticNAT_URL = "https://api.ucloudbiz.olleh.com/d1/nc/StaticNat";
    static final String staticNATList_URL = "https://api.ucloudbiz.olleh.com/d1/nc/StaticNat";
    static final String DeleteStaticNAT_URL = "https://api.ucloudbiz.olleh.com/d1/nc/StaticNat/";

    static final String firewallUrl = "https://api.ucloudbiz.olleh.com/d1/nc/Firewall";
    static final String closeFirewall_URL = "https://api.ucloudbiz.olleh.com/d1/nc/Firewall/";

    static final String tokenIssuaceUrl = "https://api.ucloudbiz.olleh.com/d1/identity/auth/tokens";
    static final String jobID_URL = "https://api.ucloudbiz.olleh.com/d1/nc/Etc?command=queryAsyncJob&jobid=";

    static final String GET = "GET";
    static final String DELETE = "DELETE";
    static final String POST = "POST";

    public static ServerInformation createServer(String serverName, String serverImage, String specs, int timeout, String accountId, String accountPassword,
                                                 String networkId, String destinationNetworkId, String destinationNetworkAddress, String sshKeyName,
                                                 String networkName, int vmCreationTimeout, int resourceProcessingTimeoutBesideVm) throws Exception {
        ServerInformation serverInformation = new ServerInformation();

        try {
            String protocol = "ALL";
            String inputPort = "1935";
            String outputPort = "80";
            int requestCycle = 1;

            LOGGER.trace("Server creation has started");
            serverInformation.setNetworkId(networkId);
            TokenResponse tokenResponse = ResourceHandler.getToken(tokenIssuaceUrl, accountId, accountPassword, timeout);
            String token = tokenResponse.getToken();
            String projectId = tokenResponse.getProjectId();
            serverInformation.setProjectId(projectId);
            String vmId = ResourceHandler.getVm(getVm_URL, token, serverName, serverImage, specs, networkId, sshKeyName, timeout);
            serverInformation.setVmId(vmId);
            String publicIpId = ResourceHandler.getPublicIp(getIP_URL, token, timeout, resourceProcessingTimeoutBesideVm, requestCycle);
            serverInformation.setPublicIpId(publicIpId);
            boolean isVmCreated = ResourceHandler.checkVmCreationStatus(VmDetail_URL, token, vmId, timeout, vmCreationTimeout, requestCycle);
            String vmPrivateIp = "";
            if (isVmCreated) {
                vmPrivateIp = ResponseParser.lookupVmPrivateIp(VmDetail_URL, token, vmId, networkName, timeout);
            } else {
                LOGGER.trace("vm creation failed");
                throw new Exception();
            }
            String staticNatId = ResourceHandler.setStaticNat(setStaticNAT_URL, token, networkId, vmPrivateIp, publicIpId, timeout);
            serverInformation.setStaticNatId(staticNatId);
            String firewallJobIdOfInputPort = ResourceHandler.openFirewall(firewallUrl, token, inputPort, inputPort, staticNatId, 
                    destinationNetworkAddress, protocol, destinationNetworkId, timeout, resourceProcessingTimeoutBesideVm, requestCycle);
            serverInformation.setFirewallJobIdOfInputPort(firewallJobIdOfInputPort);
            String firewallJobIdOfOutputPort = ResourceHandler.openFirewall(firewallUrl, token, outputPort, outputPort, staticNatId, 
                    destinationNetworkAddress, protocol, destinationNetworkId, timeout, resourceProcessingTimeoutBesideVm, requestCycle);
            serverInformation.setFirewallJobIdOfOutputPort(firewallJobIdOfOutputPort);

            LOGGER.trace("server creation has succeeded");
            return serverInformation;
        } catch (Exception e) {
            LOGGER.trace(e.toString());
            LOGGER.trace("server creation failed");
            LOGGER.trace("rollback has started");
            KTCloudOpenAPI.deleteServer(serverInformation, timeout, accountId, accountPassword, resourceProcessingTimeoutBesideVm);
            LOGGER.trace("rollback has finished");
            throw new Exception();
        }
    }

    public static ServerInformation createServer(String serverName, String volumeName, String serverImage, String volumeImage, String specs, int timeout, String accountId, String accountPassword,
                                                 String networkId, String destinationNetworkId, String destinationNetworkAddress, String sshKeyName, String networkName,
                                                 int vmCreationTimeout, int resourceProcessingTimeoutBesideVm) throws Exception {
        ServerInformation serverInformation = new ServerInformation();
        try {
            String protocol = "ALL";
            String inputPort = "1935";
            String outputPort = "80";
            int requestCycle = 1;

            LOGGER.trace("Server creation has started");
            serverInformation.setNetworkId(networkId);
            TokenResponse tokenResponse = ResourceHandler.getToken(tokenIssuaceUrl, accountId, accountPassword, timeout);
            String token = tokenResponse.getToken();
            String projectId = tokenResponse.getProjectId();
            serverInformation.setProjectId(projectId);
            String vmId = ResourceHandler.getVm(getVm_URL, token, serverName, serverImage, specs, networkId, sshKeyName, timeout);
            serverInformation.setVmId(vmId);
            String volumeId = ResourceHandler.getVolume(getVolume_URL, token, volumeName, volumeImage, projectId, timeout);
            serverInformation.setVolumeId(volumeId);
            String publicIpId = ResourceHandler.getPublicIp(getIP_URL, token, timeout, resourceProcessingTimeoutBesideVm, requestCycle);
            serverInformation.setPublicIpId(publicIpId);
            boolean isVolumeCreated = ResourceHandler.checkVolumeCreationStatus(volumeStatusCheck, token, volumeId, projectId, timeout, resourceProcessingTimeoutBesideVm, requestCycle);
            boolean isVmCreated = ResourceHandler.checkVmCreationStatus(VmDetail_URL, token, vmId, timeout, vmCreationTimeout, requestCycle);

            if (isVmCreated && isVolumeCreated) {
                ResourceHandler.connectVmAndVolume(connectVmAndVolume_URL, token, vmId, volumeId, timeout);
            } else {
                LOGGER.trace("vm or volume creation failed");
                throw new Exception();
            }

            String vmPrivateIp = ResponseParser.lookupVmPrivateIp(VmDetail_URL, token, vmId, networkName, timeout);
            String staticNatId = ResourceHandler.setStaticNat(setStaticNAT_URL, token, networkId, vmPrivateIp, publicIpId, timeout);
            serverInformation.setStaticNatId(staticNatId);
            String firewallJobIdOfInputPort = ResourceHandler.openFirewall(firewallUrl, token, inputPort, inputPort, staticNatId, 
                    destinationNetworkAddress, protocol, destinationNetworkId, timeout, resourceProcessingTimeoutBesideVm, requestCycle);
            serverInformation.setFirewallJobIdOfInputPort(firewallJobIdOfInputPort);
            String firewallJobIdOfOutputPort = ResourceHandler.openFirewall(firewallUrl, token, outputPort, outputPort, staticNatId, 
                    destinationNetworkAddress, protocol, destinationNetworkId, timeout, resourceProcessingTimeoutBesideVm, requestCycle);
            serverInformation.setFirewallJobIdOfOutputPort(firewallJobIdOfOutputPort);

            LOGGER.trace("server creation has succeeded");
            return serverInformation;
        } catch (Exception e) {
            LOGGER.trace(e.toString());
            LOGGER.trace("server creation failed");
            LOGGER.trace("rollback has started");
            KTCloudOpenAPI.deleteServer(serverInformation, timeout, accountId, accountPassword, resourceProcessingTimeoutBesideVm);
            LOGGER.trace("rollback has finished");
            throw new Exception();
        }
    }


    public static String deleteServer(ServerInformation serverInformation, int timeout, String accountId, String accountPassword, int resourceProcessingTimeoutBesideVm) throws Exception {
        boolean isVmDeleleted = false;
        boolean isVolumeDeleleted = false;
        boolean isFirewallOfInputPortCloseed = false;
        boolean isFirewallOfOutputPortCloseed = false;
        boolean isStaticNatDisabled = false;
        boolean isPublicIpDeleleted = false;
        int requestCycle = 1;

        try {
            LOGGER.trace("server deletion has started");
            TokenResponse tokenResponse = ResourceHandler.getToken(tokenIssuaceUrl, accountId, accountPassword, timeout);
            String token = tokenResponse.getToken();
            isVmDeleleted = ResourceHandler.deleteVmOnly(vmForceDeleteUrl, vmListUrl, serverInformation.getVmId(), token, timeout, resourceProcessingTimeoutBesideVm, requestCycle);
            isVolumeDeleleted = ResourceHandler.deleteVolume(volumeDeleteUrl, listOfAllVolumeUrl, serverInformation.getVolumeId(), serverInformation.getProjectId(), token, timeout, resourceProcessingTimeoutBesideVm, requestCycle);
            isFirewallOfInputPortCloseed = ResourceHandler.closeFirewall(serverInformation.getFirewallJobIdOfInputPort(), token, timeout, resourceProcessingTimeoutBesideVm, requestCycle);
            isFirewallOfOutputPortCloseed = ResourceHandler.closeFirewall(serverInformation.getFirewallJobIdOfOutputPort(), token, timeout, resourceProcessingTimeoutBesideVm, requestCycle);
            isStaticNatDisabled = ResourceHandler.deleteStaticNat(serverInformation.getStaticNatId(), token, timeout, resourceProcessingTimeoutBesideVm, requestCycle);
            isPublicIpDeleleted = ResourceHandler.deletePublicIp(serverInformation.getPublicIpId(), token, timeout, resourceProcessingTimeoutBesideVm, requestCycle);

            LOGGER.trace("server deletion has succeeded");

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

    public static void init(int timeout, String accountId, String accountPassword) throws Exception {
        //read conf
//        String confString = Etc.read(confPath);
//        JSONObject conf = new JSONObject(confString);
//        JSONObject http = conf.getJSONObject("http");
//        int timeout = http.getInt("timeout");

        LOGGER.trace("initialization has started");

        String result;
        String response;

//        result = RestAPI.request(tokenIssuaceUrl, POST, RequestBody.getToken(accountId, accountPassword));
//        String token = ResponseParser.statusCodeParser(result);
//        String projectID = ResponseParser.getProjectIdFromToken(result);

        TokenResponse tokenResponse = ResourceHandler.getToken(tokenIssuaceUrl, accountId, accountPassword, timeout);
        String token = tokenResponse.getToken();
        String projectId = tokenResponse.getProjectId();

        // close firewall
        result = RestAPI.request(firewallUrl, GET, token, "");
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
        result = RestAPI.request(vmListUrl, GET, token, "");
        response = ResponseParser.statusCodeParser(result);
        Initialization.deleteAllVm(response, token);

        //delete volume
        result = RestAPI.get(listOfAllVolumeUrl + projectId + "/volumes/detail", token, timeout);
        response = ResponseParser.statusCodeParser(result);
        Initialization.deleteAllVolume(response, token, projectId, timeout);


        LOGGER.trace("initialization has finished");
    }

    static void lookup() throws Exception {
    }

}
