package com.skcc.bcsvc.service;

import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.Uint;
import org.web3j.crypto.CipherException;
import org.web3j.protocol.exceptions.TransactionException;

import java.io.IOException;
import java.math.BigInteger;
import java.util.List;
import java.util.concurrent.ExecutionException;

public interface Web3jService {
    String getWeb3ClientVersion() throws IOException;
    String generateNewWalletFile(String password, String pathname) throws Exception;
    String generateWalletFile(String password, String mnemonics, int index) throws Exception;
    Uint getBalance(String ownerAddr) throws IOException, CipherException, ExecutionException, InterruptedException;
    String mint(String ownerAddr, Uint amount) throws IOException, CipherException, TransactionException;
}
