resource "aws_cloudwatch_dashboard" "main" {
  dashboard_name = var.prefix
  dashboard_body = <<DASHBOARD
{
  "widgets": [
    {
      "type": "metric",
      "x": 0,
      "y": 0,
      "width": 12,
      "height": 6,
      "properties": {
        "metrics": [
          ["${var.prefix}", "face_total_violations.value"],
          ["${var.prefix}", "face_total_valid.value"]
        ],
        "period": 300,
        "stat": "Maximum",
        "region": "eu-west-1",
        "title": "Face PPE Detection analysis"
      }
    },
    {
      "type": "metric",
      "x": 0,
      "y": 0,
      "width": 12,
      "height": 6,
      "properties": {
        "metrics": [
          ["${var.prefix}", "head_total_violations.value"],
          ["${var.prefix}", "head_total_valid.value"]
        ],
        "period": 300,
        "stat": "Maximum",
        "region": "eu-west-1",
        "title": "Head PPE Detection analysis"
      }
    },
    {
      "type": "metric",
      "x": 0,
      "y": 0,
      "width": 12,
      "height": 6,
      "properties": {
        "metrics": [
          ["${var.prefix}", "hands_total_violations.value"],
          ["${var.prefix}", "hands_total_valid.value"]
        ],
        "period": 300,
        "stat": "Maximum",
        "region": "eu-west-1",
        "title": "Hands PPE Detection analysis"
      }
    },
    {
     "type": "metric",
     "x": 18,
     "y": 60,
     "width": 6,
     "height": 6,
     "properties": {
        "metrics": [
          ["${var.prefix}", "exceeded_violation_alarm.value"]
        ],
        "view": "gauge",
        "stat": "Maximum",
        "region": "eu-west-1",
        "title": "Full PPE Violation counter",
        "yAxis": {
           "left": {
              "min": 0,
              "max": 5
           }
         }
       }
    },
    {
      "type": "metric",
      "x": 0,
      "y": 6,
      "width": 12,
      "height": 6,
      "properties": {
        "metrics": [
          [
            "${var.prefix}", "scanFullPPE_response_time.avg",
            "exception", "none",
            "method", "scanForPPE",
            "class", "com.example.s3rekognition.controller.RekognitionController"
          ],
          [
            "${var.prefix}", "scanForFacePPE_response_time.avg",
            "exception", "none",
            "method", "scanForFacePPE",
            "class", "com.example.s3rekognition.controller.RekognitionController"
          ],
          [
            "${var.prefix}", "scanForHeadPPE_response_time.avg",
            "exception", "none",
            "method", "scanForHeadPPE",
            "class", "com.example.s3rekognition.controller.RekognitionController"
          ],
          [
            "${var.prefix}", "scanForHandsPPE_response_time.avg",
            "exception", "none",
            "method", "scanForHandsPPE",
            "class", "com.example.s3rekognition.controller.RekognitionController"
          ]
        ],
        "period": 300,
        "stat": "Average",
        "region": "eu-west-1",
        "title": "Average Response Time for PPE analysis"
      }
    }
  ]
}
DASHBOARD
}

module "alarm" {
  source = "./alarm_module"
  alarm_email = var.alarm_email
  prefix = var.prefix
}