resource "aws_cloudwatch_metric_alarm" "threshold" {
  alarm_name  = "${var.prefix}-threshold"
  namespace   = var.prefix
  metric_name = "exceeded_violation_alarm.value"

  comparison_operator = "GreaterThanOrEqualToThreshold"
  threshold           = var.threshold
  evaluation_periods  = "1"
  period              = "60"
  statistic           = "Maximum"

  alarm_description = "This alarm goes off when number of violations of PPE-detection is equal or greater than 10"
  alarm_actions     = [aws_sns_topic.user_updates.arn]
}

resource "aws_sns_topic" "user_updates" {
  name = "${var.prefix}-alarm-topic"
}

resource "aws_sns_topic_subscription" "user_updates_sqs_target" {
  topic_arn = aws_sns_topic.user_updates.arn
  protocol  = "email"
  endpoint  = var.alarm_email
}