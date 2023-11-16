variable "service_name" {
  type = string
}

variable "aws_iam_role_name" {
  type = string
}

variable "aws_iam_policy_name" {
  type = string
}

variable "port" {
  default = "8080"
  type = string
}

variable "kandidat" {
  default = "dashboard-2038"
  type = string
}

variable "cloudwatch_namespace" {
  default = "cloudwatch-2038"
  type = string
}

variable "image_tag" {
  type = string
}