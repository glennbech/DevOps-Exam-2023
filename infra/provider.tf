provider "aws" {
  region = "eu-west-1"
}

terraform {
  required_providers {
    aws = {
      source = "hashicorp/aws"
      version = "5.25.0"
    }
  }
  backend "s3" {
    bucket = "kandidatnr-2038"
    key = "2038-apprunner-new-state.state"
    region = "eu-west-1"
  }
}