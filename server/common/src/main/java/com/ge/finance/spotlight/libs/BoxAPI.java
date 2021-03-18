package com.ge.finance.spotlight.libs;

import java.io.*;
import java.security.NoSuchAlgorithmException;
import java.security.Security;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.crypto.Cipher;
import javax.servlet.http.HttpServletRequest;

import com.box.sdk.BoxAPIException;
import com.box.sdk.BoxConfig;
import com.box.sdk.BoxDeveloperEditionAPIConnection;
import com.box.sdk.BoxFile;
import com.box.sdk.BoxItem;
import com.box.sdk.StandardCharsets;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;

public class BoxAPI {

    public BoxAPI() {
    }

    static FileReader reader;

    @Value("${spring.boxapi.location}")
    private Resource templateLocation;

    BoxFile boxFile = null;

    public BoxFile getCSVData(String sharedLink, HttpServletRequest request)
            throws BoxAPIException, IOException, NoSuchFieldException, SecurityException, ClassNotFoundException,
            IllegalArgumentException, IllegalAccessException, NoSuchAlgorithmException {

        try {

            //System.setProperty("https.proxyHost", "PITC-Zscaler-Americas-Cincinnati3PR.proxy.corporate.ge.com");

            //System.setProperty("https.proxyPort", "80");

            // Security.addProvider(new BouncyCastleProvider());

            InputStream inputStream = getClass().getResourceAsStream("/config.json");

            InputStreamReader streamReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);

            BufferedReader reader = new BufferedReader(streamReader);

            BoxConfig config = BoxConfig.readFrom(reader);

            BoxDeveloperEditionAPIConnection api = BoxDeveloperEditionAPIConnection.getAppEnterpriseConnection(config);

            BoxItem.Info itemInfo = BoxItem.getSharedItem(api, sharedLink);

            boxFile = new BoxFile(api, itemInfo.getID());

            // BoxFile.Info info = file.getInfo();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return boxFile;
    }

}
