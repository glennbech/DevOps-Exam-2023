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
          [ "var.cloudwatch-namespace", "protection_violations.value"],
          [ "var.cloudwatch-namespace", "valid_protection.value"],
          [ "var.cloudwatch-namespace", "images_found.value"],
          [ "var.cloudwatch-namespace", "people_found.value"]
        ],
        "period": 300,
        "stat": "Maximum",
        "region": "eu-west-1",
        "title": "PPE Detection analysis"
      }
    }
  ]
}
DASHBOARD
}