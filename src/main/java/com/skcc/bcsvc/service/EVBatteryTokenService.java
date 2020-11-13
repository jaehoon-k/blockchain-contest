package com.skcc.bcsvc.service;

import org.web3j.abi.datatypes.Uint;
import org.web3j.crypto.CipherException;
import org.web3j.protocol.exceptions.TransactionException;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.Map;

public interface EVBatteryTokenService {
    String getWeb3ClientVersion() throws IOException;
    String generateNewWalletFile(String password, String pathname) throws Exception;
    String generateWalletFile(String password, String mnemonics, int index) throws Exception;
    Uint getBalance(String ownerAddr) throws IOException, CipherException, ExecutionException, InterruptedException;
    String mint(String ownerAddr, Uint amount) throws IOException, CipherException, TransactionException;
    
    Uint getDeposit(String account) throws IOException, CipherException, ExecutionException, InterruptedException;
    Map<String, Object> deposit(String seller, Uint amount) throws IOException, CipherException, TransactionException;
    Map<String, Object> release(String seller) throws IOException, CipherException, TransactionException;
    String status(String seller, boolean stat) throws IOException, CipherException, TransactionException;
    String refund(String seller) throws IOException, CipherException, TransactionException;

}
