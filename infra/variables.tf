# Used for both namespace for CloudWatch dashboard and alarm.
variable "prefix" {
  default = "cloudwatch-kandidatnr-2038"
  type = string
}

variable "name" {
  default = "kandidatnr-2038"
  type = string
}
variable "port" {
  default = "8080"
  type = string
}

# Alarm module
variable "alarm_email" {
  type = string
}