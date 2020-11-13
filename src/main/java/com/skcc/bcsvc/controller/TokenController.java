package com.skcc.bcsvc.controller;

import com.skcc.bcsvc.service.EVBatteryTokenService;
import com.skcc.bcsvc.storage.StorageFileNotFoundException;
import com.skcc.bcsvc.storage.StorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.web3j.abi.datatypes.Bool;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.Uint;
import org.web3j.crypto.CipherException;
import org.web3j.protocol.exceptions.TransactionException;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@RestController
public class TokenController {

    @Autowired
    private EVBatteryTokenService evBatteryTokenService;

    private final StorageService storageService;

    @Autowired
    public TokenController(StorageService storageService) {
        this.storageService = storageService;
    }

    @GetMapping("EVToken/balance/{ownerAddr}")
    public BigInteger getBalance(
            @PathVariable(required = true) String ownerAddr) throws InterruptedException, ExecutionException, CipherException, IOException {
        return evBatteryTokenService.getBalance(ownerAddr).getValue();
    }

    @PostMapping("EVToken/balance/Mint")
    public String mint(@RequestParam(name="ownerAddr") String ownerAddr,
                       @RequestParam(name="amount") BigInteger amount) throws TransactionException, CipherException, IOException {
        return evBatteryTokenService.mint(ownerAddr, new Uint(amount));
    }

    @GetMapping("EVToken/deposit/{accountAddr}")
    public BigInteger getDeposit(
            @PathVariable(required = true) String accountAddr) throws InterruptedException, ExecutionException, CipherException, IOException {
        return evBatteryTokenService.getDeposit(accountAddr).getValue();
    }
    @PostMapping("EVToken/deposit/")
    public String deposit(@RequestParam(name="sellerAddr") String sellerAddr,
                       @RequestParam(name="amount") BigInteger amount) throws TransactionException, CipherException, IOException {
        return evBatteryTokenService.deposit(sellerAddr, new Uint(amount));
    }
    @PostMapping("EVToken/release/")
    public String release(@RequestParam(name="sellerAddr") String sellerAddr
                       ) throws TransactionException, CipherException, IOException {
        return evBatteryTokenService.release(sellerAddr);
    }
    @PostMapping("EVToken/status/")
    public String status(@RequestParam(name="sellerAddr") String sellerAddr, @RequestParam(name="stat") boolean stat
                       ) throws TransactionException, CipherException, IOException {
        return evBatteryTokenService.status(sellerAddr, stat);
    }
    @PostMapping("EVToken/refund/")
    public String status(@RequestParam(name="sellerAddr") String sellerAddr
                       ) throws TransactionException, CipherException, IOException {
        return evBatteryTokenService.refund(sellerAddr);
    }
}
