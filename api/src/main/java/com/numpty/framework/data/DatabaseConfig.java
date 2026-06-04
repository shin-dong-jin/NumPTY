//package com.numpty.framework.data;
//
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//public class DatabaseConfig {
//    private static final Logger log = LoggerFactory.getLogger(DatabaseConfig.class);
//
//    public static void startH2Console() {
//        try {
//            org.h2.tools.Server.createTcpServer("-tcp", "-tcpAllowOthers", "-ifNotExists").start();
//            org.h2.tools.Server.createWebServer("-web", "-webAllowOthers", "-ifNotExists").start();
//
//            System.out.println("✅ H2 Web Console: http://localhost:8082");
//        } catch (Exception e) {
//            log.error("H2 console error occurs: {}", e.getMessage(), e);
//        }
//    }
//}
