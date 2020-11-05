package com.skcc.bcsvc.controller;

import com.skcc.bcsvc.service.Web3jService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.Uint;
import org.web3j.crypto.CipherException;
import org.web3j.protocol.exceptions.TransactionException;

import java.io.IOException;
import java.math.BigInteger;
import java.util.List;
import java.util.concurrent.ExecutionException;

@RestController
public class Web3jController {

    @Autowired
    private Web3jService web3jService;

    @GetMapping("/web3j/client/version")
    public String getClientVersion() throws IOException {
        return web3jService.getWeb3ClientVersion();
    }

    @PostMapping("web3j/credentials")
    public String generateWalletFile(@RequestParam(name="password") String password,
                                     @RequestParam(name="mnemonics") String mnemonics,
                                     @RequestParam(name="index") int index) throws Exception {
        return web3jService.generateWalletFile(password, mnemonics, index);
    }

    @PostMapping("web3j/credentials/walletfile")
    public String generateNewWalletFile(@RequestParam(name="password") String password,
                                        @RequestParam(name="pathname") String pathname) throws Exception {
        return web3jService.generateNewWalletFile(password, pathname);
    }

    @GetMapping("simple-coin/balance/{ownerAddr}")
    public BigInteger getBalance(
            @PathVariable(required = true) String ownerAddr) throws InterruptedException, ExecutionException, CipherException, IOException {
        return web3jService.getBalance(ownerAddr).getValue();
    }

    @PostMapping("simple-coin/balance")
    public String mint(@RequestParam(name="ownerAddr") String ownerAddr,
                       @RequestParam(name="amount") BigInteger amount) throws TransactionException, CipherException, IOException {
        return web3jService.mint(ownerAddr, new Uint(amount));
    }
}
