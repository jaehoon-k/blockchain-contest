package com.skcc.bcsvc.service;

import org.web3j.crypto.CipherException;
import org.web3j.protocol.exceptions.TransactionException;

import java.io.IOException;
import java.math.BigInteger;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public interface BatteryService {
    Map<String, Object> issueCertificate(String ownerAddr, String batteryId, String manufacturer, String modelNumber, BigInteger dateManufacture) throws IOException, TransactionException, CipherException;
    Map<String, Object> verifyCertificate(String ownerAddr, BigInteger tokenId, String tokenURI) throws ExecutionException, InterruptedException;
}
