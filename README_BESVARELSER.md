## Oppgave 1
    A.
    Fixed variables, deployed app to aws with sam deploy --guided.
        * Updated template.yaml to:
            - Use value from Parameter "MyBucketName" to BUCKET_NAME. Default is set to "kandidatnr-2038".
            - Added 
                Environment:
                  Variables:
                    MyBucketName: !Ref MyBucketName
              so AWS lambda can access it.

        The Lambda app could not access the S3 bucket because of permissions. Had to add the following:
            - AmazonS3ReadOnlyAccess

        
    Create Github Actions workflow for sam.
        * Added workflows for Github Actions.
            - Added envs to repo with aws secrets.
            - Push to main: should build and deploy             - OK
            - Push to other branches: should only build         - OK


    How to get Github Actions workflow to run
        * Create Secrets in AWS IAM.
            1. In AWS search for AIM
            2. Go to My security credentials -> Create Access key.
            3. Check "Command Line Interface (CLI) and continue
                Now open new tab and go to your github repo.
                Go to Settings -> Secrets and variables -> Actions
                    Click "New Repository secret"
                        Name = AWS_ACCESS_KEY_I
                        Secret = enter the access key id from AWS IAM
                    Do the same with:
                        Name = AWS_SECRET_ACCESS_KEY
                        Secret = enter the secret access key from AWS IAM
        
        * Now github actions should run.
                    
    B.
    Dockerfile created for app.py.
    Handles AWS credentials.
    Runs the sam app accordingly with the script example provided in Oppgave B.

    
## Oppgave 2
    A.
    Created a multi stage Dockerfile for java-app.
    Builds and then runs when running the example script.

    B.
    Github actions for java app med container image och ecr
        ECR created in AWS Interface
        Build image and publish to ECR repo only when pushing to main       - OK
        Build image only when pushing to other than main                    - OK
        Uses ref and latest

    Running the docker script does not work in intellij (mvn spring-boot:run gives weird errors)
    But this does not happen in Cloud9, and works fine.


## Oppgave 3
    A.
    Changed some rows in main.tf
        * Added variables for:
            aws_apprunner_service
            aws_iam_role
            aws_iam_policy
            port

        * Changed ecr from kjell to my ECR.

        * variables.tf
            Here i set a default value for all of them, but can be overridden.

        * Added lines in instance_configuration for modifying
            cpu 
            memory usage
    
    Running terraform init and then terraform apply works fine in intellij.
    But in Cloud9 i get errors and no not know why...

    B.
    Pre-requisites to run oppgave-2-workflows.yaml for github actions:
        Will need Secrets added into Github secrets from AWS as described in Oppgave 1.
        S3 bucket
        ECR Repository
    
    Changes:
        IAM Role and IAM Policy needs to have a unique name.
        In the oppgave-2-workflows.yaml file, change the values under "env":
        env:
          ECR_REPO: 244530008913.dkr.ecr.eu-west-1.amazonaws.com/<your-ecr-repo>
          SERVICE_NAME: <set-unique-appr-name>
          AWS_IAM_ROLE_NAME: <set-unique-role-name>
          AWS_IAM_POLICY_NAME: <set-unique-policy-name>
          PORT: <set-your-port>
        
        Otherwise they will use default values.








