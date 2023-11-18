package com.example.s3rekognition.controller;

import com.amazonaws.services.rekognition.AmazonRekognition;
import com.amazonaws.services.rekognition.AmazonRekognitionClientBuilder;
import com.amazonaws.services.rekognition.model.*;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.ListObjectsV2Result;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.example.s3rekognition.PPEClassificationResponse;
import com.example.s3rekognition.PPEResponse;
import io.micrometer.core.annotation.Timed;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;


@RestController
public class RekognitionController implements ApplicationListener<ApplicationReadyEvent> {
    private final Map<String, Integer> scanResult = new HashMap<>();
    private int exceededViolationCounter = 0;
    private final int violationLimit = 5;
    private final double violationPercentage = 0.3;
    private final AmazonS3 s3Client;
    private final AmazonRekognition rekognitionClient;
    private final MeterRegistry meterRegistry;

    private static final Logger logger = Logger.getLogger(RekognitionController.class.getName());

    @Autowired
    public RekognitionController(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        this.s3Client = AmazonS3ClientBuilder.standard().build();
        this.rekognitionClient = AmazonRekognitionClientBuilder.standard().build();
    }

    /**
     * This endpoint takes an S3 bucket name in as an argument, scans all the
     * Files in the bucket for Protective Gear Violations.
     * <p>
     *
     * @param bucketName
     * @return
     */
    @GetMapping(value = "/scan-ppe", consumes = "*/*", produces = "application/json")
    @ResponseBody
//    @Timed(value = "scanforPPE-latency", description = "single piece scan latency")
    public ResponseEntity<PPEResponse> scanForPPE(@RequestParam String bucketName) {
        // Used for metrics
        int violationCounter = 0;
        int validCounter = 0;

        // List all objects in the S3 bucket
        ListObjectsV2Result imageList = s3Client.listObjectsV2(bucketName);

        // This will hold all of our classifications
        List<PPEClassificationResponse> classificationResponses = new ArrayList<>();

        // This is all the images in the bucket
        List<S3ObjectSummary> images = imageList.getObjectSummaries();

        // Iterate over each object and scan for PPE
        for (S3ObjectSummary image : images) {
            logger.info("scanning " + image.getKey());

            // This is where the magic happens, use AWS rekognition to detect PPE
            DetectProtectiveEquipmentRequest request = new DetectProtectiveEquipmentRequest()
                    .withImage(new Image()
                            .withS3Object(new S3Object()
                                    .withBucket(bucketName)
                                    .withName(image.getKey())))
                    .withSummarizationAttributes(new ProtectiveEquipmentSummarizationAttributes()
                            .withMinConfidence(80f)
                            .withRequiredEquipmentTypes("FACE_COVER"));

            DetectProtectiveEquipmentResult result = rekognitionClient.detectProtectiveEquipment(request);

            // If any person on an image lacks PPE on the face, it's a violation of regulations
            boolean violation = isViolation(result);
            if (violation) {
                violationCounter++;
            } else {
                validCounter++;
            }

            logger.info("scanning " + image.getKey() + ", violation result " + violation);
            // Categorize the current image as a violation or not.
            int personCount = result.getPersons().size();
            PPEClassificationResponse classification = new PPEClassificationResponse(image.getKey(), personCount, violation);
            classificationResponses.add(classification);
        }

        PPEResponse ppeResponse = new PPEResponse(bucketName, classificationResponses);
        ppeResponse.setNumberOfViolations(violationCounter);
        ppeResponse.setNumberOfValid(validCounter);

        scanResult.put("Violations", ppeResponse.getNumberOfViolations());
        scanResult.put("Valid", ppeResponse.getNumberOfValid());


//        To Cloudwatch - want to put these 4 in a single graph.
        meterRegistry.counter("violations").increment();
        meterRegistry.counter("valid").increment();

        return ResponseEntity.ok(ppeResponse);
    }

    @GetMapping(value = "/scan-full-ppe", consumes = "*/*", produces = "application/json")
//    @Timed(value = "scanFullPPE-latency", description = "full ppe scan latency")
    @ResponseBody
    public ResponseEntity<PPEResponse> scanFullPPE(@RequestParam String bucketName) {
        int violationCounter = 0;
        int validCounter = 0;
        int totalPersonCount = 0;

        ListObjectsV2Result imageList = s3Client.listObjectsV2(bucketName);             // Get content from bucket
        List<PPEClassificationResponse> classificationResponses = new ArrayList<>();
        List<S3ObjectSummary> images = imageList.getObjectSummaries();                  // Get information about each file in bucket.

        for (S3ObjectSummary image : images) {
            logger.info("Scanning " + image.getKey());          // Key here = file name.

            // AWS Rekognition
            DetectProtectiveEquipmentRequest request = new DetectProtectiveEquipmentRequest()
                    .withImage(new Image()                          // Set image to be scanned with Rekognition
                            .withS3Object(new S3Object()            // From S3 bucket
                                    .withBucket(bucketName)         // Named bucketName
                                    .withName(image.getKey())))     // Set specific image to be scanned (key = filename)

                    .withSummarizationAttributes(new ProtectiveEquipmentSummarizationAttributes()
                            .withMinConfidence(80f)
                            .withRequiredEquipmentTypes("FACE_COVER", "HAND_COVER", "HEAD_COVER"));

            DetectProtectiveEquipmentResult result = rekognitionClient.detectProtectiveEquipment(request);


            boolean violation = isStrictViolation(result);
            if (violation) {
                violationCounter++;
            } else {
                validCounter++;
            }
            logger.info("Scanning " + image.getKey() + ", Protection analysis: " + violation);

            int personCount = result.getPersons().size();
            totalPersonCount += personCount;

            PPEClassificationResponse classification = new PPEClassificationResponse(image.getKey(), personCount, violation);
            classificationResponses.add(classification);
        }

        PPEResponse ppeResponse = new PPEResponse(bucketName, classificationResponses);
        ppeResponse.setNumberOfViolations(violationCounter);
        ppeResponse.setNumberOfValid(validCounter);

        // This should check if 30% of the scanned people are violations, it should increment by 1 to the widget.
        // If it reaches 5 times, it should send off an alarm.
        if (ppeResponse.getNumberOfViolations() >= totalPersonCount * violationPercentage) {
            exceededViolationCounter++;
            meterRegistry.counter("violation_alarm").increment();
        }

        // If it reaches 5, alarm should be triggered.
        // When it reaches over 5, another violation occurs - "reset" counter to 1
        // The alarm set for this widget has evaluation_period = 1.
        if (exceededViolationCounter > violationLimit) {
            exceededViolationCounter = 1;
            meterRegistry.gauge("exceeded_violation_alarm", exceededViolationCounter);
        }

        logger.info("Number of people scanned: " + totalPersonCount + ". Number of violations: " + ppeResponse.getNumberOfViolations());
        logger.info("Current violation counter: " + exceededViolationCounter);
        return ResponseEntity.ok(ppeResponse);
    }

    /**
     * Detects if the image has a protective gear violation for the FACE bodypart-
     * It does so by iterating over all persons in a picture, and then again over
     * each body part of the person. If the body part is a FACE and there is no
     * protective gear on it, a violation is recorded for the picture.

     * @param result
     * @return
     */
    private static boolean isViolation(DetectProtectiveEquipmentResult result) {
        return result.getPersons().stream()
                .flatMap(p -> p.getBodyParts().stream())
                .anyMatch(bodyPart -> bodyPart.getName().equals("FACE")
                        && bodyPart.getEquipmentDetections().isEmpty());
    }

    private static boolean isStrictViolation(DetectProtectiveEquipmentResult result) {
        return result.getPersons().stream()
                .flatMap(p -> p.getBodyParts().stream())
                .anyMatch(bodyPart ->
                        (bodyPart.getName().equals("FACE")
                                || bodyPart.getName().equals("LEFT_HAND")
                                || bodyPart.getName().equals("RIGHT_HAND")
                                || bodyPart.getName().equals("HEAD"))
                                && bodyPart.getEquipmentDetections().isEmpty());
    }

    @Override
    public void onApplicationEvent(ApplicationReadyEvent applicationReadyEvent) {
        Gauge.builder("total_violations", scanResult,
                        violation -> violation.getOrDefault("Violations", 0))
                .register(meterRegistry);

        Gauge.builder("total_valid", scanResult,
                        valid -> valid.getOrDefault("Valid", 0))
                .register(meterRegistry);

        Gauge.builder("exceeded_violation_alarm", this, obj -> obj.exceededViolationCounter).register(meterRegistry);
    }

}
