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


## Oppgave 3
    A.
    Changed some rows in main.tf
        Added variables for:
            aws_apprunner_service
            aws_iam_role
            aws_iam_policy
            port

        variables.tf
            Here i set a default value for all of them, but can be overridden.

        Added instance_configuration for modifying cpu and memory usage
    


















