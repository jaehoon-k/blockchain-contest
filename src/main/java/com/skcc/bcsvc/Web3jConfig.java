package com.skcc.bcsvc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.web3j.crypto.CipherException;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.WalletUtils;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Positive;
import java.io.IOException;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class Web3jConfig {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Bean
    public Web3j web3j(
            @Value("${ethereum.client.protocol}") @NotEmpty final String protocol,
            @Value("${ethereum.client.host}") @NotEmpty final String host,
            @Value("${ethereum.client.port}") @Positive final int port,
            @Value("${ethereum.client.jwt}") @Positive final String jwt) {

        return this.buildWeb3j(protocol, host, port, jwt);

    }

    private Web3j buildWeb3j(@NotEmpty final String protocol,
                             @NotEmpty final String host,
                             @Positive final int port,
                             @NotEmpty final String jwt) {
        Web3j web3j = null;

        try {
            HttpService httpService = new HttpService(String.format("%s://%s:%d/", protocol, host, port));
            httpService.addHeader("Authorization", "Bearer " + jwt);
            web3j = Web3j.build(httpService);

            logger.info("Successfully connected to Besu client for {}://{}:{}/", protocol, host, port);
        } catch (Exception ex) {
            logger.error(String.format("Fail to connect Besu client for %s://%s:%d/", protocol, host, port), ex);
        }

        return web3j;

    }

    @Bean
    public HashMap<String, Credentials> defaultCredential(
            @Value("${credentials.default.password}") String defaultCredentialPassword,
            @Value("${credentials.default.path}") String defaultCredentialPath,
            @Value("${credentials.buyer.password}") String buyerCredentialPassword,
            @Value("${credentials.buyer.path}") String buyerCredentialPath,
            @Value("${credentials.seller.password}") String sellerCredentialPassword,
            @Value("${credentials.seller.path}") String sellerCredentialPath) throws IOException, CipherException {

        HashMap<String, Credentials> credentialsMap = new HashMap<>();
        credentialsMap.put("default", loadCredential(defaultCredentialPassword, defaultCredentialPath));
        credentialsMap.put("buyer", loadCredential(buyerCredentialPassword, buyerCredentialPath));
        credentialsMap.put("seller", loadCredential(sellerCredentialPassword, sellerCredentialPath));

        return credentialsMap;
    }

    private Credentials loadCredential(String password, String path) throws IOException, CipherException  {
        Credentials credentials =
                WalletUtils.loadCredentials(password, path);
        logger.info("Credentials loaded. {}", credentials.getAddress());

        return credentials;
    }
}
