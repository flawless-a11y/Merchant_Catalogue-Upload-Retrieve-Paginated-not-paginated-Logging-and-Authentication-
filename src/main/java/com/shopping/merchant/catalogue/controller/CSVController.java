package com.shopping.merchant.catalogue.controller;

import com.shopping.merchant.catalogue.entity.Merchant;
import com.shopping.merchant.catalogue.helper.CSVHelper;
import com.shopping.merchant.catalogue.message.ResponseMessage;
import com.shopping.merchant.catalogue.message.ResponseMessage1;
import com.shopping.merchant.catalogue.service.CSVService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@CrossOrigin("*")
@RestController
@RequestMapping
public class CSVController {

    @Autowired
    CSVService fileService;

    @Operation(summary = "This is to upload merchant data through csv file ")
    @ApiResponse(responseCode = "200",
            description = "File uploaded successfully and data is saved to db",
            content = {@Content(mediaType = "application/csv")})
    @ApiResponse(responseCode = "400",
            description = "Please Upload a CSV file")
    @ApiResponse(responseCode = "417",
            description = "Could not upload the file")
    @PostMapping("/merchant/upload")
    public ResponseEntity<ResponseMessage> uploadFile(@RequestParam("file") MultipartFile file) {
        String message = "";
        if (CSVHelper.hasCSVFormat(file)) {
            try {
                message = fileService.save(file);
                String status = "";
                if (message == "Data uploaded and saved successfully")
                    status = " Success";
                else
                    status = "Failure";
                message += " File :" + file.getOriginalFilename();

                return ResponseEntity.status(HttpStatus.OK).body(new ResponseMessage(status, message));
            } catch (Exception e) {
                message = "Could not upload the file:" + file.getOriginalFilename() + "!";
                return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(new ResponseMessage("Failure", message));
            }
        }
        message = "Please upload a csv file!";
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ResponseMessage("Failure", message));
    }


    @Operation(summary = "This is to fetch all data at once in json format")
    @ApiResponse(responseCode = "204",
            description = "Database is empty")
    @ApiResponse(responseCode = "200",
            description = "Data retrieved successfully")
    @ApiResponse(responseCode = "500",
            description = "Unknown error occurred")
    @GetMapping("/merchant/all")
    public ResponseEntity<?> getAllMerchants() {
        try {
            List<Merchant> merchants = fileService.getAllMerchants();
            if (merchants.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NO_CONTENT).body(new ResponseMessage("Failure", "Database empty no data exits"));
            }
            return ResponseEntity.status(HttpStatus.OK).body(new ResponseMessage1("Success", "Successfully retrieved data", merchants));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ResponseMessage("Failure", "Something has gone wrong on the website's server"));
        }
    }


    @Operation(summary = "This is to fetch data of one or more merchant with specified merchants ids")
    @ApiResponse(responseCode = "204",
            description = "Database is empty")
    @ApiResponse(responseCode = "200",
            description = "Data retrieved successfully")
    @ApiResponse(responseCode = "500",
            description = "Unknown error occurred")
    @GetMapping("/merchant/{ids}")
    public ResponseEntity<?> getMerchantByIds(@PathVariable("ids") List<Integer> ids) {

        List<Merchant> merchants = new ArrayList<>();
        for (Integer id : ids) {
            try {
                Merchant merchant =  fileService.getMerchantById(id);
                merchants.add(merchant);
            } catch (Exception e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ResponseMessage("Failure",
                        "Something has gone wrong on the website's server"));
            }
        }
        if (merchants.isEmpty()) {
            return (ResponseEntity<?>) ResponseEntity.status(HttpStatus.NO_CONTENT);
        }
        return ResponseEntity.status(HttpStatus.OK).body(new ResponseMessage1("Success",
                "Data of the requested merchants is retrieved",merchants));
    }
}


