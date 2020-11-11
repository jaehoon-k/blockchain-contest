package com.skcc.bcsvc.controller;

import com.skcc.bcsvc.service.Web3jService;
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
public class Web3jController {

    @Autowired
    private Web3jService web3jService;

    private final StorageService storageService;

    @Autowired
    public Web3jController(StorageService storageService) {
        this.storageService = storageService;
    }

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

    @PostMapping("battery/certificates")
    @ResponseStatus(HttpStatus.CREATED)
    public List<String> upload(@RequestPart List<MultipartFile> files) throws Exception {
        List<String> list = new ArrayList<>();
        for (MultipartFile file : files) {
            String originalfileName = file.getOriginalFilename();
            File dest = new File("C:/certificate/" + originalfileName);
            file.transferTo(dest);
            // TODO
        }
        return list;
    }

    @GetMapping("/")
    public String listUploadedFiles(Model model) throws IOException {

        model.addAttribute("files", storageService.loadAll().map(
                path -> MvcUriComponentsBuilder.fromMethodName(Web3jController.class,
                        "serveFile", path.getFileName().toString()).build().toUri().toString())
                .collect(Collectors.toList()));

        return "uploadForm";
    }

    @GetMapping("/files/{filename:.+}")
    @ResponseBody
    public ResponseEntity<Resource> serveFile(@PathVariable String filename) {

        Resource file = storageService.loadAsResource(filename);
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=\"" + file.getFilename() + "\"").body(file);
    }

    @PostMapping("/")
    public String handleFileUpload(@RequestParam("file") MultipartFile file,
                                   RedirectAttributes redirectAttributes) {

        storageService.store(file);
        redirectAttributes.addFlashAttribute("message",
                "You successfully uploaded " + file.getOriginalFilename() + "!");

        return "redirect:/";
    }

    @ExceptionHandler(StorageFileNotFoundException.class)
    public ResponseEntity<?> handleStorageFileNotFound(StorageFileNotFoundException exc) {
        return ResponseEntity.notFound().build();
    }

}
