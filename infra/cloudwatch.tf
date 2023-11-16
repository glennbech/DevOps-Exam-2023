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
          [ "cloudwatch-2038", "protection_violations.value"],
          [ "cloudwatch-2038", "valid_protection.value"],
          [ "cloudwatch-2038", "images_found.value"],
          [ "cloudwatch-2038", "people_found.value"]
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