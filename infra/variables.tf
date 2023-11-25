variable "port" {
  default = "8080"
  type = string
}

# Used for both namespace for CloudWatch dashboard and alarm.
variable "prefix" {
  default = "kandidatnr-2038"
  type = string
}

# Alarm module
variable "alarm_email" {
  type = string
}