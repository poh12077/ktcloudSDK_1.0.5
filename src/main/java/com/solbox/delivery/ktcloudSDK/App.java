package com.solbox.delivery.ktcloudSDK;


public class App {

    public static void main(String[] args) {
        try {

//
//            ServerInformation serverInformation = KTCloudOpenAPI.createServer("nana", "03a6328b-76c8-4d15-8e3f-d5cae5cf1156", "61c68bc1-3a56-4827-9fd1-6a7929362bf6",
//                    10, "infra.op@solbox.com", "xJd*Qv*cBXpd7qX", "71655962-3e67-42d6-a17d-6ab61a435dfe",
//                    "71655962-3e67-42d6-a17d-6ab61a435dfe", "172.25.1.1/24");

            ServerInformation serverInformation = KTCloudOpenAPI.createServer("nana", "nana",
                    "03a6328b-76c8-4d15-8e3f-d5cae5cf1156","556aacd2-de16-47fc-b230-3db3a55be50d" ,"61c68bc1-3a56-4827-9fd1-6a7929362bf6",
                    20, "infra.op@solbox.com", "xJd*Qv*cBXpd7qX", "71655962-3e67-42d6-a17d-6ab61a435dfe",
                    "71655962-3e67-42d6-a17d-6ab61a435dfe", "172.25.1.1/24");

            KTCloudOpenAPI.deleteServer(serverInformation, 20, "infra.op@solbox.com", "xJd*Qv*cBXpd7qX");


            //Test.test();
//            String confPath = "C:\\Users\\young hwa park\\Desktop\\yhp\\source\\ktcloud\\ktcloud_sdk_1.0.4\\conf.json";
//            KTCloudOpenAPI.init(confPath,"infra.op@solbox.com", "xJd*Qv*cBXpd7qX");

        } catch (Exception e) {
            KTCloudOpenAPI.LOGGER.trace(e.toString());
        }
    }
}