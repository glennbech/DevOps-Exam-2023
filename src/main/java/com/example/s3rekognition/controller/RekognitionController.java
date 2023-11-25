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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import java.util.logging.Logger;


@RestController
public class RekognitionController implements ApplicationListener<ApplicationReadyEvent> {
    private PPEResponse ppeFaceResponse = new PPEResponse();
    private PPEResponse ppeHeadResponse = new PPEResponse();
    private PPEResponse ppeHandsResponse = new PPEResponse();
    private final Supplier<PPEResponse> ppeFaceResponseSupplier = () -> ppeFaceResponse;
    private final Supplier<PPEResponse> ppeHeadResponseSupplier = () -> ppeHeadResponse;
    private final Supplier<PPEResponse> ppeHandsResponseSupplier = () -> ppeHandsResponse;
    private final int violationLimit = 5;               // Change this value for when to reset Gauge
    private final double violationPercentage = 0.3;     // Change this value for sensitivity to increment to Gauge
    private final AtomicInteger exceededViolationGauge;
    private int exceededViolationCounter = 0;
    private int violationCounter = 0;
    private int validCounter = 0;
    private int totalPersonCount = 0;
    private final AmazonS3 s3Client;
    private final AmazonRekognition rekognitionClient;
    private final MeterRegistry meterRegistry;
    private static final Logger logger = Logger.getLogger(RekognitionController.class.getName());

    @Autowired
    public RekognitionController(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        this.s3Client = AmazonS3ClientBuilder.standard().build();
        this.rekognitionClient = AmazonRekognitionClientBuilder.standard().build();
        this.exceededViolationGauge = meterRegistry.gauge("exceeded_violation_alarm", new AtomicInteger(0));
    }

    /**
     * This endpoint takes an S3 bucket name in as an argument, scans all the
     * Files in the bucket for Protective Gear Violations.
     * <p>
     *
     * @param bucketName
     * @return
     */
    // FACE, HEAD, HANDS SCAN
    @GetMapping(value = "/scan-ppe", consumes = "*/*", produces = "application/json")
    @ResponseBody
    @Timed(value = "scanFullPPE_response_time", description = "response time of full ppe scan")
    public ResponseEntity<PPEResponse> scanForPPE(@RequestParam String bucketName) {
        resetViolationCounter();
        resetTotalPersonCount();

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
            }

            logger.info("Scanning " + image.getKey() + ", Protection analysis: " + violation);

            int personCount = result.getPersons().size();
            totalPersonCount += personCount;

            PPEClassificationResponse classification = new PPEClassificationResponse(image.getKey(), personCount, violation);
            classificationResponses.add(classification);
        }

        PPEResponse ppeResponse = new PPEResponse(bucketName, classificationResponses);
        ppeResponse.setNumberOfViolations(violationCounter);

        // Update Gauge metrics if condition are met.
        updateOrResetGaugeMetric(ppeResponse, totalPersonCount);

        logger.info("Number of people scanned: " + totalPersonCount + ". Number of violations: " + ppeResponse.getNumberOfViolations());
        logger.info("Current violation counter: " + exceededViolationCounter);
        return ResponseEntity.ok(ppeResponse);
    }

    // FACE SCAN
    @GetMapping(value = "/scan-face-ppe", consumes = "*/*", produces = "application/json")
    @ResponseBody
    @Timed(value = "scanForFacePPE_response_time", description = "response time of face ppe scan")
    public ResponseEntity<PPEResponse> scanForFacePPE(@RequestParam String bucketName) {
        // Used for metrics
        resetViolationCounter();
        resetValidCounter();

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
            boolean violation = isViolation(result, "FACE");
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

        ppeFaceResponse = new PPEResponse(bucketName, classificationResponses);
        ppeFaceResponse.setNumberOfViolations(violationCounter);
        ppeFaceResponse.setNumberOfValid(validCounter);
        return ResponseEntity.ok(ppeFaceResponse);
    }

    // HEAD SCAN
    @GetMapping(value = "/scan-head-ppe", consumes = "*/*", produces = "application/json")
    @ResponseBody
    @Timed(value = "scanForHeadPPE_response_time", description = "response time of head ppe scan")
    public ResponseEntity<PPEResponse> scanForHeadPPE(@RequestParam String bucketName) {
        resetViolationCounter();
        resetValidCounter();

        ListObjectsV2Result imageList = s3Client.listObjectsV2(bucketName);
        List<PPEClassificationResponse> classificationResponses = new ArrayList<>();
        List<S3ObjectSummary> images = imageList.getObjectSummaries();

        for (S3ObjectSummary image : images) {
            logger.info("scanning " + image.getKey());

            DetectProtectiveEquipmentRequest request = new DetectProtectiveEquipmentRequest()
                    .withImage(new Image()
                            .withS3Object(new S3Object()
                                    .withBucket(bucketName)
                                    .withName(image.getKey())))
                    .withSummarizationAttributes(new ProtectiveEquipmentSummarizationAttributes()
                            .withMinConfidence(80f)
                            .withRequiredEquipmentTypes("HEAD_COVER"));

            DetectProtectiveEquipmentResult result = rekognitionClient.detectProtectiveEquipment(request);

            boolean violation = isViolation(result, "HEAD");
            if (violation) {
                violationCounter++;
            } else {
                validCounter++;
            }

            logger.info("scanning " + image.getKey() + ", violation result " + violation);

            int personCount = result.getPersons().size();
            PPEClassificationResponse classification = new PPEClassificationResponse(image.getKey(), personCount, violation);
            classificationResponses.add(classification);
        }

        ppeHeadResponse = new PPEResponse(bucketName, classificationResponses);
        ppeHeadResponse.setNumberOfViolations(violationCounter);
        ppeHeadResponse.setNumberOfValid(validCounter);
        return ResponseEntity.ok(ppeHeadResponse);
    }

    // HANDS SCAN
    @GetMapping(value = "/scan-hands-ppe", consumes = "*/*", produces = "application/json")
    @ResponseBody
    @Timed(value = "scanForHandsPPE_response_time", description = "response time of hands ppe scan")
    public ResponseEntity<PPEResponse> scanForHandsPPE(@RequestParam String bucketName) {
        resetViolationCounter();
        resetValidCounter();

        ListObjectsV2Result imageList = s3Client.listObjectsV2(bucketName);
        List<PPEClassificationResponse> classificationResponses = new ArrayList<>();
        List<S3ObjectSummary> images = imageList.getObjectSummaries();

        for (S3ObjectSummary image : images) {
            logger.info("scanning " + image.getKey());

            DetectProtectiveEquipmentRequest request = new DetectProtectiveEquipmentRequest()
                    .withImage(new Image()
                            .withS3Object(new S3Object()
                                    .withBucket(bucketName)
                                    .withName(image.getKey())))
                    .withSummarizationAttributes(new ProtectiveEquipmentSummarizationAttributes()
                            .withMinConfidence(80f)
                            .withRequiredEquipmentTypes("HAND_COVER"));

            DetectProtectiveEquipmentResult result = rekognitionClient.detectProtectiveEquipment(request);

            boolean violationRightHand = isViolation(result, "RIGHT_HAND");
            boolean violationLeftHand = isViolation(result, "LEFT_HAND");
            boolean violation = true;

            if (!violationRightHand && !violationLeftHand) {
                validCounter++;
                violation = false;
            } else {
                violationCounter++;
            }

            logger.info("scanning " + image.getKey() + ", violation result " + violation);

            int personCount = result.getPersons().size();
            PPEClassificationResponse classification = new PPEClassificationResponse(image.getKey(), personCount, violation);
            classificationResponses.add(classification);
        }

        ppeHandsResponse = new PPEResponse(bucketName, classificationResponses);
        ppeHandsResponse.setNumberOfViolations(violationCounter);
        ppeHandsResponse.setNumberOfValid(validCounter);
        return ResponseEntity.ok(ppeHandsResponse);
    }

    private void updateOrResetGaugeMetric(PPEResponse ppeResponse, int totalPersonCount) {
        // This should check if 30% of the scanned people are violations, it should increment by 1 to the widget.
        // If it reaches 5 times, it should send off an alarm.
        if (ppeResponse.getNumberOfViolations() >= totalPersonCount * violationPercentage) {
            exceededViolationCounter++;
            exceededViolationGauge.getAndIncrement();
        }

        // If it reaches 5, alarm should be triggered.
        // When it reaches over 5 that means another violation has occured -> reset counter to 1
        // The alarm set for this widget has evaluation_period = 1 (very strict).
        if (exceededViolationCounter > violationLimit) {
            exceededViolationCounter = 1;
            exceededViolationGauge.set(1);
        }
    }

    /**
     * Detects if the image has a protective gear violation for the FACE bodypart-
     * It does so by iterating over all persons in a picture, and then again over
     * each body part of the person. If the body part is a FACE and there is no
     * protective gear on it, a violation is recorded for the picture.

     * @param result
     * @return
     */
    private static boolean isViolation(DetectProtectiveEquipmentResult result, String typeOfPPE) {
        return result.getPersons().stream()
                .flatMap(p -> p.getBodyParts().stream())
                .anyMatch(bodyPart -> bodyPart.getName().equals(typeOfPPE)
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

    private void resetViolationCounter() {
        violationCounter = 0;
    }

    private void resetValidCounter() {
        validCounter = 0;
    }

    private void resetTotalPersonCount() {
        totalPersonCount = 0;
    }

    @Override
    public void onApplicationEvent(ApplicationReadyEvent applicationReadyEvent) {
        Gauge.builder("face_total_violations", ppeFaceResponseSupplier, supplier -> supplier.get().getNumberOfViolations()).register(meterRegistry);
        Gauge.builder("face_total_valid", ppeFaceResponseSupplier, supplier -> supplier.get().getNumberOfValid()).register(meterRegistry);

        Gauge.builder("head_total_violations", ppeHeadResponseSupplier, supplier -> supplier.get().getNumberOfViolations()).register(meterRegistry);
        Gauge.builder("head_total_valid", ppeHeadResponseSupplier, supplier -> supplier.get().getNumberOfValid()).register(meterRegistry);

        Gauge.builder("hands_total_violations", ppeHandsResponseSupplier, supplier -> supplier.get().getNumberOfViolations()).register(meterRegistry);
        Gauge.builder("hands_total_valid", ppeHandsResponseSupplier, supplier -> supplier.get().getNumberOfValid()).register(meterRegistry);

        Gauge.builder("exceeded_violation_alarm", this, obj -> obj.exceededViolationCounter).register(meterRegistry);
    }

}