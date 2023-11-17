variable "service_name" {
#  default = "app-runner-kandidatnr-2038"
  type = string
}

variable "aws_iam_role_name" {
#  default = "policy-app-service-2038"
  type = string
}

variable "aws_iam_policy_name" {
#  default = "iam-role-2038"
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

# default is set from github actions during docker push to ecr.
variable "image_tag" {
  type = string
}