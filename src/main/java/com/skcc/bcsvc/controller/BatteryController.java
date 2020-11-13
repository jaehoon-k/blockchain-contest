package com.skcc.bcsvc.controller;

import com.skcc.bcsvc.service.BatteryService;
import com.skcc.bcsvc.service.Web3jService;
import com.skcc.bcsvc.storage.StorageFileNotFoundException;
import com.skcc.bcsvc.storage.StorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.math.BigInteger;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
public class BatteryController {

    @Autowired
    private Web3jService web3jService;

    @Autowired
    private BatteryService batteryService;

    private final StorageService storageService;

    @Autowired
    public BatteryController(StorageService storageService) {
        this.storageService = storageService;
    }

    @PostMapping("battery/certificates")
    public Map<String, Object> issue(
            @RequestParam(name="ownerAddr") String ownerAddr,
            @RequestParam(name="batteryId") String batteryId,
            @RequestParam(name="manufacturer") String manufacturer,
            @RequestParam(name="modelNumber") String modelNumber,
            @RequestParam(name="dateManufacture") BigInteger dateManufacture) throws Exception {

        return batteryService.issueCertificate(ownerAddr, batteryId, manufacturer, modelNumber, dateManufacture);
    }

    @GetMapping("battery/certificates")
    public Map<String, Object> verify(
            @RequestParam(name="ownerAddr") String ownerAddr,
            @RequestParam(name="tokenId") BigInteger tokenId,
            @RequestParam(name="tokenURI") String tokenURI) throws Exception {

//        return batteryService.verifyCertificate(ownerAddr, new BigInteger(tokenId, 16), tokenURI);
        return batteryService.verifyCertificate(ownerAddr, tokenId, tokenURI);
    }

    @PutMapping("battery/certificates")
    public Map<String, Object> transfer(
            @RequestParam(name="fromAddr") String fromAddr,
            @RequestParam(name="toAddr") String toAddr,
            @RequestParam(name="tokenId") BigInteger tokenId) throws Exception {

        return batteryService.transferFrom(fromAddr, toAddr, tokenId);
    }

    @GetMapping("/files")
    public Object listUploadedFiles(Model model) throws IOException {

        model.addAttribute("files", storageService.loadAll().map(
                path -> MvcUriComponentsBuilder.fromMethodName(BatteryController.class,
                        "serveFile", path.getFileName().toString()).build().toUri().toString())
                .collect(Collectors.toList()));

        return model.asMap().get("files");
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
