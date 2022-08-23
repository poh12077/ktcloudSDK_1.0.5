package com.solbox.delivery.ktcloudSDK;

class Test {
   static void test() throws Exception{
        String  result = RestAPI.post(KTCloudOpenAPI.getToken_URL, RequestBody.getToken("infra.op@solbox.com", "xJd*Qv*cBXpd7qX"), 10);
        String token = ResponseParser.statusCodeParser(result);
        Etc.check(token);
        String projectId = ResponseParser.getProjectIdFromToken(result);
        Etc.check(projectId);

    //  boolean isStaticNatDisabled = ResourceHandler.deleteStaticNat("d47f640d-0797-4cb0-b1fd-44a7336e2105", token, 100, 500, 1);

      // result = RestAPI.request(KTCloudOpenAPI.staticNATList_URL, "GET", token, "");

        String jobId= "4aaf7478-b3a7-4dc6-aaf8-2857cb481321";
        String response = ResponseParser.lookupJobId(jobId, token, 10);

        return;
    }

}
