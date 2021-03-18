package com.ge.finance.spotlight.controllers;

import com.box.sdk.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.*;
import java.net.Socket;

@RestController
@RequestMapping("/v1/box")
public class BoxController {

    @GetMapping("/test")
    String test() {
        try {
            Socket socket = new Socket("api.box.com", 443);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            StringBuilder stringBuffer = new StringBuilder();
            String line;
            while ((line = in.readLine()) != null) {
                stringBuffer.append(line);
            }
            in.close();
            return stringBuffer.toString();
        } catch (Exception e) {
            return e.getMessage();
        }
    }

    @GetMapping("/download")
    String download2(@RequestParam String sharedLink) {
        System.out.println("Download endpoint: " + sharedLink);
        try {
            InputStream inputStream = getClass().getResourceAsStream("/config.json");
            InputStreamReader streamReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
            BufferedReader reader = new BufferedReader(streamReader);
            System.out.println("Starting to call Box API");
            BoxConfig config = BoxConfig.readFrom(reader);
            BoxDeveloperEditionAPIConnection api = BoxDeveloperEditionAPIConnection.getAppEnterpriseConnection(config);
            BoxItem.Info itemInfo = BoxItem.getSharedItem(api, sharedLink);
            BoxFile boxFile = new BoxFile(api, itemInfo.getID());
            System.out.println("Starting downloading file");
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            boxFile.download(byteArrayOutputStream);
            String result = byteArrayOutputStream.toString();
            System.out.println(result);
            byteArrayOutputStream.close();
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return e.getMessage();
        }
    }

}
