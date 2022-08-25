package com.solbox.delivery.ktcloudSDK;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

class RequestBody {
	static String getVm(String name, String imageID, String specs, String networkId, String sshKeyName) throws JSONException {
		JSONObject finalJsonObject = new JSONObject();
		JSONObject server = new JSONObject();
		JSONObject networks = new JSONObject();
		JSONObject block_device_mapping_v2 = new JSONObject();
		JSONArray jsonArray0 = new JSONArray();
		JSONArray jsonArray1 = new JSONArray();

		server.put("name", name);
		server.put("key_name", sshKeyName);
		server.put("flavorRef", specs);
		server.put("availability_zone", "DX-M1");
		networks.put("uuid", networkId);
		jsonArray0.put(networks);
		server.put("networks", jsonArray0);
		block_device_mapping_v2.put("destination_type", "volume");
		block_device_mapping_v2.put("boot_index", "0");
		block_device_mapping_v2.put("source_type", "image");
		block_device_mapping_v2.put("volume_size", 50);
		block_device_mapping_v2.put("uuid", imageID);
		jsonArray1.put(block_device_mapping_v2);
		server.put("block_device_mapping_v2", jsonArray1);
		finalJsonObject.put("server", server);

		return finalJsonObject.toString();
	}

	static String getToken(String accountId, String accountPassword) throws JSONException {
		JSONObject finalJsonObject = new JSONObject();
		JSONObject auth = new JSONObject();
		JSONObject identity = new JSONObject();
		JSONObject scope = new JSONObject();
		JSONObject password = new JSONObject();
		JSONObject user = new JSONObject();
		JSONObject domain = new JSONObject();
		JSONObject project = new JSONObject();
		JSONArray methods = new JSONArray();

		domain.put("id", "default");
		user.put("domain", domain);
		user.put("name", accountId);
		user.put("password", accountPassword);
		password.put("user", user);
		methods.put("password");
		identity.put("methods", methods);
		identity.put("password", password);
		project.put("name", accountId);
		project.put("domain", domain);
		scope.put("project", project);
		auth.put("scope", scope);
		auth.put("identity", identity);
		finalJsonObject.put("auth", auth);

		return finalJsonObject.toString();
	
	}
	
	static String getVolume(String name, String imageID ) throws JSONException {
		JSONObject finalJsonObject = new JSONObject();
		JSONObject volume = new JSONObject();

		volume.put("snapshot_id", imageID);
		volume.put("bootable", false);
		volume.put("name", name);
		volume.put("usage_plan_type", "hourly");
		volume.put("size", 500);
		volume.put("availability_zone", "DX-M1");
		finalJsonObject.put("volume", volume);
		
		return finalJsonObject.toString();
	}
	
	static String setStaticNat(String VmPrivateIP, String VmNetworkID, String publicIP_ID) throws JSONException {
		JSONObject finalJsonObject = new JSONObject();
		finalJsonObject.put("vmguestip", VmPrivateIP);
		finalJsonObject.put("vmnetworkid", VmNetworkID);
		finalJsonObject.put("entpublicipid", publicIP_ID);
		return finalJsonObject.toString();
	}
	
	static String openFirewall(String startPort, String endPort, String staticNAT_ID, String sourceNetworkID, 
			String destinationNetworkAddress, String protocol, String destinationNetworkID) throws JSONException {
		JSONObject finalJsonObject = new JSONObject();
		finalJsonObject.put("startport", startPort);
		finalJsonObject.put("endport", endPort);
		finalJsonObject.put("virtualipid", staticNAT_ID);
		finalJsonObject.put("action", "allow");
		finalJsonObject.put("srcnetworkid", sourceNetworkID);
		finalJsonObject.put("dstip", destinationNetworkAddress);
		finalJsonObject.put("protocol", protocol);
		finalJsonObject.put("dstnetworkid", destinationNetworkID);
		return finalJsonObject.toString();
	}

	static String connectVmAndVolume(String volumeId) throws JSONException {
		JSONObject finalJsonObject = new JSONObject();
		JSONObject volumeAttachment = new JSONObject();
		volumeAttachment.put("volumeId", volumeId);
		volumeAttachment.put("device", "/dev/vdb");
		finalJsonObject.put("volumeAttachment", volumeAttachment);
		return finalJsonObject.toString();
	}
	
	static String forceDeleteVm() throws JSONException {
		return "{\"forceDelete\": null}";
	}
	
	
}
