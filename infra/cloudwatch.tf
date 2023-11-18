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
              "max": 5
           }
         }
       }
    },

  ]
}
DASHBOARD
}

module "alarm" {
  source = "./alarm_module"
  alarm_email = var.alarm_email
  prefix = var.prefix
}