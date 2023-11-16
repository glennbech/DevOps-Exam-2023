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
          [ "cloudwatch-2038", "protection-violations.value"],
          [ "cloudwatch-2038", "protection-violations.value"],
          [ "cloudwatch-2038", "protection-violations.value"],
          [ "cloudwatch-2038", "protection-violations.value"],
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