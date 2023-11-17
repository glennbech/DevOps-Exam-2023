resource "aws_cloudwatch_dashboard" "main" {
  dashboard_name = var.kandidat
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
          [ "${var.cloudwatch_namespace}", "total_violations.value"],
          [ "${var.cloudwatch_namespace}", "total_valid.value"]
        ],
        "period": 300,
        "stat": "Maximum",
        "region": "eu-west-1",
        "title": "PPE Detection analysis"
      }
    },
    {
     "type": "metric",
     "x": 18,
     "y"; 60,
     "width": 6,
     "height": 6,
     "properties": {
        "metrics": [
          ["${var.cloudwatch_namespace}, "exceeded_violation_alarm"]
        ],
        "view": "gauge",
        "title": "Violation alarm counter",
        "region": "eu-west-1",
        "yAxis": {
           "left": {
              "min": 0,
              "max": 100,
           }
         }
       }
     }
  ]
}
DASHBOARD
}