package com.skcc.bcsvc.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.web3j.abi.*;
import org.web3j.abi.datatypes.*;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.crypto.*;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.request.EthFilter;
import org.web3j.protocol.core.methods.request.Transaction;
import org.web3j.protocol.core.methods.response.*;
import org.web3j.protocol.exceptions.TransactionException;
import org.web3j.tx.FastRawTransactionManager;
import org.web3j.tx.TransactionManager;
import org.web3j.tx.Transfer;
import org.web3j.tx.response.PollingTransactionReceiptProcessor;
import org.web3j.tx.response.TransactionReceiptProcessor;
import org.web3j.utils.Convert;
import org.web3j.utils.Numeric;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static org.web3j.crypto.Bip32ECKeyPair.HARDENED_BIT;

@Service
public class Web3jServiceImpl implements Web3jService{
    @Autowired
    private Web3j web3j;

    @Value("${sampleContracts.simpleCoin.address}")
    private String simpleCoinAddress;

    @Value("${credentials.default.password}")
    private String credentialPassword;

    @Value("${credentials.default.path}")
    private String credentialPath;

    private static final Logger log = LoggerFactory.getLogger(com.skcc.bcsvc.Application.class);

    @Override
    public String getWeb3ClientVersion() throws IOException {
        try {
            Web3ClientVersion web3ClientVersion = web3j.web3ClientVersion().send();
            String clientVersion = web3ClientVersion.getWeb3ClientVersion();

            return clientVersion;
        } catch (IOException e) {
            return e.getMessage();
        }
    }

    @Override
    public String generateNewWalletFile(String password, String pathname) throws Exception {
        String fileName = WalletUtils.generateNewWalletFile(
                password,
                new File(pathname));
        return fileName;
    }

    @Override
    public String generateWalletFile(String password, String mnemonics, int index) throws Exception {
        Credentials credentials = generateCredentialsFromMnemonics(password, mnemonics, index);
        return "Public Key: " + credentials.getEcKeyPair().getPublicKey() + "\n" +
                "Address: " + credentials.getAddress();
    }

    private Credentials generateCredentialsFromMnemonics(String password, String mnemonics, int index) {
        byte[] seed = MnemonicUtils.generateSeed(mnemonics, password);
        Bip32ECKeyPair masterKeypair = Bip32ECKeyPair.generateKeyPair(seed);
        final int[] path = {44 | HARDENED_BIT, 60 | HARDENED_BIT, 0 | HARDENED_BIT, 0, index};
        Bip32ECKeyPair childKeypair = Bip32ECKeyPair.deriveKeyPair(masterKeypair, path);
        Credentials credentials = Credentials.create(childKeypair);
        return credentials;
    }

    @Override
    public Uint getBalance(String ownerAddr) throws IOException, CipherException, ExecutionException, InterruptedException {
        /*
            1. Call Transaction; Read values from smart contracts.
            - Get current balance of `ownerAddr`
            - Calling the function `balanceOf` of SimpleCoin.sol
        */

        // Address of a `SimpleCoin` Smart Contract deployed on ChainZ Besu Mainnet.
        // Source code at /contracts/SimpleCoin.sol.
        String contractAddr = this.simpleCoinAddress;
        String functionName = "balanceOf";
        Address owner = new Address(ownerAddr);

        Credentials credentials =
                WalletUtils.loadCredentials(
                        credentialPassword,
                        credentialPath);
        log.info("Credentials loaded");

        String callerAddr = credentials.getAddress();

        log.info("Check balance of {}.", ownerAddr);

        Function func1 = new Function(
                functionName,
                Arrays.asList(owner),
                Arrays.asList(new TypeReference<Uint256>() {})
        );

        final String encodedFunc1 = FunctionEncoder.encode(func1);

        log.info("The address to process : {}", callerAddr);
        log.info("ABI encode for 'balanceOf(address:...)' : {}", encodedFunc1);

        org.web3j.protocol.core.methods.request.Transaction tx = Transaction.createEthCallTransaction(callerAddr, contractAddr, encodedFunc1);
        EthCall resp = web3j.ethCall(tx, DefaultBlockParameterName.LATEST).sendAsync().get();

        List<Type> output = FunctionReturnDecoder.decode(resp.getResult(), func1.getOutputParameters());

        log.info("The output size : {}", output.size());

        Uint balance = (Uint)output.get(0);

        log.info("The balance : {}", balance.getValue().toString());

        return balance;
    }

    @Override
    public String mint(String ownerAddr, Uint amount) throws IOException, CipherException, TransactionException {
        /*
            2. Send Transaction; Update values of smart contracts
            - Add balances to `ownerAdr`
            - Calling `mint` of SimpleCoin.sol
         */
        String contractAddr = this.simpleCoinAddress;
        String functionName2 = "mint";
        Address owner = new Address(ownerAddr);

        Credentials credentials =
                WalletUtils.loadCredentials(
                        credentialPassword,
                        credentialPath);
        log.info("Credentials loaded");
//        log.info("Private Key:" + Numeric.toHexString(credentials.getEcKeyPair().getPrivateKey().toByteArray()));

        log.info("Mint {} tokens to {}", amount.getValue(), owner );

        Function func2 = new Function(
                functionName2,
                Arrays.asList(owner, amount),
                Collections.emptyList()
        );

        final String encodedFunc2 = FunctionEncoder.encode(func2);

        FastRawTransactionManager txMgr = new FastRawTransactionManager(web3j, credentials);

        String txHash = txMgr.sendTransaction(BigInteger.ZERO, BigInteger.valueOf(10_000_000), contractAddr, encodedFunc2, BigInteger.ZERO).getTransactionHash();
        log.info("Waiting receipt - txHahs: {}, function: {}, contract: {}", new Object[]{txHash, functionName2, contractAddr});
        TransactionReceiptProcessor receiptProcessor = new PollingTransactionReceiptProcessor(web3j, 100, 1000);
        TransactionReceipt receipt = receiptProcessor.waitForTransactionReceipt(txHash);

        log.info("Recetipt: " + receipt);

        return txHash;
    }
}
