resource "aws_cloudwatch_dashboard" "main" {
  dashboard_name = var.prefix
  dashboard_body = <<DASHBOARD
{
  "widgets": [
    {
      "type": "metric",
      "x": 0,
      "y": 0,
      "width": 16,
      "height": 6,
      "properties": {
        "metrics": [
          ["${var.prefix}", "total_violations.value"],
          ["${var.prefix}", "total_valid.value"]
        ],
        "period": 300,
        "stat": "Maximum",
        "region": "eu-west-1",
        "title": "Face PPE Detection analysis"
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
              "max": 10
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
            "${var.prefix}", "scanforPPE-latency.avg",
            "exception", "none",
            "method", "scanForPPE",
            "class", "com.example.s3rekognition.controller.RekognitionController"]
        ],
        "period": 300,
        "stat": "Average",
        "region": "eu-west-1",
        "title": "Average latency for Single PPE-piece scan"
      }
    },
    {
      "type": "metric",
      "x": 0,
      "y": 12,
      "width": 12,
      "height": 6,
      "properties": {
        "metrics": [
          [
            "${var.prefix}", "scanFullPPE-latency.avg",
            "exception", "none",
            "method", "scanFullPPE",
            "class", "com.example.s3rekognition.controller.RekognitionController"]
        ],
        "period": 300,
        "stat": "Average",
        "region": "eu-west-1",
        "title": "Average latency for Full PPE scan"
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