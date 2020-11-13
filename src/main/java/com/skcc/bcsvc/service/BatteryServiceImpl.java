package com.skcc.bcsvc.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.skcc.bcsvc.dto.BatteryCertificatesDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.FunctionReturnDecoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.*;
import org.web3j.abi.datatypes.generated.Bytes32;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.crypto.*;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.request.Transaction;
import org.web3j.protocol.core.methods.response.EthCall;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.protocol.exceptions.TransactionException;
import org.web3j.tx.FastRawTransactionManager;
import org.web3j.tx.response.PollingTransactionReceiptProcessor;
import org.web3j.tx.response.TransactionReceiptProcessor;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.ExecutionException;

@Service
public class BatteryServiceImpl implements BatteryService{
    @Autowired
    private Web3j web3j;

    @Autowired
    private HashMap<String, Credentials> credentialsMap;

    @Value("${smartContracts.batteryCertificates.address}")
    private String batteryCertificatesAddress;

    private static final Logger log = LoggerFactory.getLogger(com.skcc.bcsvc.Application.class);
/*

    public Uint getBalance(String ownerAddr) throws IOException, CipherException, ExecutionException, InterruptedException {
        */
/*
            1. Call Transaction; Read values from smart contracts.
            - Get current balance of `ownerAddr`
            - Calling the function `balanceOf` of SimpleCoin.sol
        *//*


        // Address of a `SimpleCoin` Smart Contract deployed on ChainZ Besu Mainnet.
        // Source code at /contracts/SimpleCoin.sol.
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

        Transaction tx = Transaction.createEthCallTransaction(callerAddr, contractAddr, encodedFunc1);
        EthCall resp = web3j.ethCall(tx, DefaultBlockParameterName.LATEST).sendAsync().get();

        List<Type> output = FunctionReturnDecoder.decode(resp.getResult(), func1.getOutputParameters());

        log.info("The output size : {}", output.size());

        Uint balance = (Uint)output.get(0);

        log.info("The balance : {}", balance.getValue().toString());

        return balance;
    }
*/

    @Override
    public Map<String, Object> verifyCertificate(String ownerAddr, BigInteger tokenId, String tokenURI) throws ExecutionException, InterruptedException {
        String functionName = "verifyCertificate";

        RestTemplate restTemplate = new RestTemplate();
        BatteryCertificatesDTO batteryCertificatesDTO
                = restTemplate.getForObject(tokenURI, BatteryCertificatesDTO.class);

        log.info("Verify Battery Certificate(id:{}) of {}", batteryCertificatesDTO.getName(), ownerAddr);

        Function func = new Function(
                functionName,
                Arrays.asList(
                        new Uint256(tokenId),
                        new Address(ownerAddr),
                        new Utf8String(batteryCertificatesDTO.getName()),
                        new Utf8String(batteryCertificatesDTO.getAttributes().get("manufacturer").toString()),
                        new Utf8String(batteryCertificatesDTO.getAttributes().get("modelNumber").toString()),
                        new Uint256(new BigInteger(String.valueOf(batteryCertificatesDTO.getAttributes().get("dateManufacture")))),
                        new Utf8String(tokenURI)),
                Arrays.asList(new TypeReference<Bool>() {})
        );

        final String encodedFunc = FunctionEncoder.encode(func);

        Transaction tx = Transaction.createEthCallTransaction(credentialsMap.get("buyer").getAddress(), batteryCertificatesAddress, encodedFunc);
        EthCall resp = web3j.ethCall(tx, DefaultBlockParameterName.LATEST).sendAsync().get();

        List<Type> output = FunctionReturnDecoder.decode(resp.getResult(), func.getOutputParameters());

        log.info("The output size : {}", output.size());

        Map<String, Object> result = new HashMap<>();
        result.put("owner", ownerAddr);
        result.put("tokenId", tokenId);
        result.put("tokenURI", tokenURI);
        result.put("batteryId", batteryCertificatesDTO.getName());
        result.put("manufacturer", batteryCertificatesDTO.getAttributes().get("manufacturer").toString());
        result.put("modelNumber", batteryCertificatesDTO.getAttributes().get("modelNumber").toString());
        result.put("dateManufacture", batteryCertificatesDTO.getAttributes().get("dateManufacture").toString());
        result.put("isValid", output.get(0).getValue());

        return result;
    }

    @Override
    public Map<String, Object> issueCertificate(String ownerAddr, String batteryId, String manufacturer, String modelNumber, BigInteger dateManufacture) throws IOException, TransactionException {
        String functionName = "issueCertificate";

        // Create BatteryCertificateDTO
        BatteryCertificatesDTO batteryCertificatesDTO = new BatteryCertificatesDTO();
        batteryCertificatesDTO.setName(batteryId);
        batteryCertificatesDTO.setDescription("");
        batteryCertificatesDTO.setImage("");

        Map<String, Object> attributes = new HashMap<>();
        attributes.put("manufacturer", manufacturer);
        attributes.put("modelNumber", modelNumber);
        attributes.put("dateManufacture", dateManufacture);

        batteryCertificatesDTO.setAttributes(attributes);

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(SerializationFeature.INDENT_OUTPUT, true);
        objectMapper.writeValue(new File("upload-dir/" + batteryId + ".json"), batteryCertificatesDTO);

        final String tokenURI = "http://localhost:5000/files/" + batteryId + ".json";

        log.info("Issue Battery Certificate(id:{}) to {}", batteryId, ownerAddr );

        Function func = new Function(
                functionName,
                Arrays.asList(
                        new Address(ownerAddr),
                        new Utf8String(batteryId),
                        new Utf8String(manufacturer),
                        new Utf8String(modelNumber),
                        new Uint256(dateManufacture),
                        new Utf8String(tokenURI)),
                Collections.emptyList()
        );

        final String encodedFunc = FunctionEncoder.encode(func);

        FastRawTransactionManager txMgr = new FastRawTransactionManager(web3j, credentialsMap.get("default"));

        String txHash = txMgr.sendTransaction(BigInteger.ZERO, BigInteger.valueOf(10_000_000), batteryCertificatesAddress, encodedFunc, BigInteger.ZERO).getTransactionHash();
        log.info("Polling receipt - txHahs: {}, function: {}, contract: {}", new Object[]{txHash, functionName, batteryCertificatesAddress});
        TransactionReceiptProcessor receiptProcessor = new PollingTransactionReceiptProcessor(web3j, 100, 1000);
        TransactionReceipt receipt = receiptProcessor.waitForTransactionReceipt(txHash);

        if (!receipt.isStatusOK())
            throw new RuntimeException("Transaction Failed: " + receipt);

        List<String> topics = receipt.getLogs().get(0).getTopics();

        log.info("Receipt: " + receipt);
        log.info("New Battery Certificate issued: Owner => {}, TokenId => {}, Certificate File => {}",
                new Address(topics.get(2)),
                topics.get(3).substring(2),
                tokenURI
        );

        Map<String,Object> result = new HashMap<>();
        result.put("txHash", txHash);
        result.put("certificate", tokenURI);
        result.put("owner", new Address(topics.get(2)).getValue());
        result.put("tokenId", topics.get(3).substring(2));

        try {
            verifyCertificate(ownerAddr, new BigInteger(topics.get(3).substring(2), 16), tokenURI);
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        /*// Event definition
        final Event MY_EVENT = new Event("Transfer", Arrays.<TypeReference<?>>asList(new TypeReference<Address>(true) {}, new TypeReference<Address>(true) {}, new TypeReference<Uint256>(true) {}));

        // Event definition hash
        final String MY_EVENT_HASH = EventEncoder.encode(MY_EVENT);

        // Filter
        EthFilter filter = new EthFilter(DefaultBlockParameterName.EARLIEST, DefaultBlockParameterName.LATEST, batteryCertificatesAddress);

        // Pull all the events for this contract
        web3j.ethLogFlowable(filter).subscribe(logs -> {
            String eventHash = logs.getTopics().get(0); // Index 0 is the event definition hash

            if(eventHash.equals(MY_EVENT_HASH)) { // Only MyEvent. You can also use filter.addSingleTopic(MY_EVENT_HASH)
                // address indexed _arg1
                Address arg1 = (Address) FunctionReturnDecoder.decodeIndexedValue(logs.getTopics().get(1), new TypeReference<Address>() {});
                // Address indexed _arg2
                Address arg2 = (Address) FunctionReturnDecoder.decodeIndexedValue(logs.getTopics().get(2), new TypeReference<Address>() {});
                // Address indexed _arg3
                Address arg3 = (Address) FunctionReturnDecoder.decodeIndexedValue(logs.getTopics().get(3), new TypeReference<Address>() {});

                log.info("<Event> from: " + arg1 + " to: " + arg2 + " token id: " + arg3);
            }
        });*/

        return result;
    }
}
