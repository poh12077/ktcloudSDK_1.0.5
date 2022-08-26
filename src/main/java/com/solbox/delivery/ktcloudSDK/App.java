package com.solbox.delivery.ktcloudSDK;


public class App {

    public static void main(String[] args) {
        try {

//
//            ServerInformation serverInformation = KTCloudOpenAPI.createServer("nana", "03a6328b-76c8-4d15-8e3f-d5cae5cf1156", "61c68bc1-3a56-4827-9fd1-6a7929362bf6",
//                    10, "edge2.ktcloud@solbox.com", "xJd*Qv*cBXpd7qX", "71655962-3e67-42d6-a17d-6ab61a435dfe",
//                    "71655962-3e67-42d6-a17d-6ab61a435dfe", "172.25.1.1/24");
//
//            int count =0;
//            while (count<100) {
//
//                ServerInformation serverInformation = KTCloudOpenAPI.createServer("yh", "yh",
//                        "1827b8d8-007a-4429-ab73-2ab668fffab1", "1bd87456-f3ae-4e9d-963c-3b93d52ec395", "61c68bc1-3a56-4827-9fd1-6a7929362bf6",
//                        60, "edge2.ktcloud@solbox.com", "xJd*Qv*cBXpd7qX", "cdcf5e13-6d30-483d-846a-0ce41038f70e",
//                        "cdcf5e13-6d30-483d-846a-0ce41038f70e", "172.25.0.1/24", "yh0", "DMZ", 500, 300);
//
//       //         KTCloudOpenAPI.deleteServer(serverInformation, 60, "edge2.ktcloud@solbox.com", "xJd*Qv*cBXpd7qX", 300);
//            count++;
//            }

            ServerInformation serverInformation = KTCloudOpenAPI.createServer("yh", "yh",
                    "1827b8d8-007a-4429-ab73-2ab668fffab1", "1bd87456-f3ae-4e9d-963c-3b93d52ec395", "61c68bc1-3a56-4827-9fd1-6a7929362bf6",
                    60, "edge2.ktcloud@solbox.com", "xJd*Qv*cBXpd7qX", "cdcf5e13-6d30-483d-846a-0ce41038f70e",
                    "cdcf5e13-6d30-483d-846a-0ce41038f70e", "172.25.0.1/24", "yh0", "DMZ", 500, 300);

                     KTCloudOpenAPI.deleteServer(serverInformation, 60, "edge2.ktcloud@solbox.com", "xJd*Qv*cBXpd7qX", 300);

            //String confPath = "C:\\Users\\young hwa park\\Desktop\\yhp\\source\\ktcloud\\ktcloud_sdk_1.0.4\\conf.json";
          //  KTCloudOpenAPI.init(30,"edge2.ktcloud@solbox.com", "xJd*Qv*cBXpd7qX");

        } catch (Exception e) {
            KTCloudOpenAPI.LOGGER.trace(e.toString());
        }
    }
}