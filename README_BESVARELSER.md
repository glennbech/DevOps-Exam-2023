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
    The state will be saved in the S3 bucket after running terraform init and apply locally.
    If no changes are, it will just pass.

    Pre-requisites to run oppgave-2-workflows.yaml for github actions:
        Will need Secrets added into Github secrets from AWS as described in Oppgave 1.
        S3 bucket
        ECR Repository
    
    Changes:
        provider.tf contains information about the s3 bucket.
            Change:
                bucket - to your s3 bucket
                key - (if you want your own name to where state is saved)
        
        Apprunner, IAM Role and IAM Policy needs to have a unique name.
        In the oppgave-2-workflows.yaml file:
        Change the values under "env":
            env:
              ECR_REPO: 244530008913.dkr.ecr.eu-west-1.amazonaws.com/<your-ecr-repo>
              SERVICE_NAME: <set-unique-appr-name>
              AWS_IAM_ROLE_NAME: <set-unique-role-name>
              AWS_IAM_POLICY_NAME: <set-unique-policy-name>
              PORT: <set-your-port>, (could most stay as 8080, but added this in case)        

            comment out:    "run: terraform apply -auto-approve"
            and use: "run:  terraform apply -var="service_name=$SERVICE_NAME" -var="aws_iam_role_name=$AWS_IAM_ROLE_NAME" -var="aws_iam_policy_name=$AWS_IAM_POLICY_NAME" -var="port=$PORT"  -auto-approve"


## Oppgave 4
    A.
    * KRAV: Minst en till endpoint
            Minst 3 måleinstrumenter
            FÖRKLARA BRA VALEN
    * Frivilligt: Förbättre koden osv.

        * Extra endpoints - Hands, Head. Kanske en med allt också?
            Räkna antal violations och valid från bilder


    * Additions
        scanForPPE:
            Added metrics for number of violations and valid scans of PPE protection in scanForPPE endpoint
            These two metrics shows in one widget on Cloudwatch.
            It could be interesting to see how many that actually correctly wear PPE. This way each scanned image
            can show the number of people violating it and number of people who passes.
        
        scanFullPPE:
            New endpoint to scan all three parts: head, face and hands for PPE.
            New Gauge widget added here which reports number of violations.
            If the violation reaches 10, alarm should trigger and send an email notifiation.
                The idea is that this could be used for very strict environments where PPE is very important and
                compliance must be good. So for example if it happens 10 times a day, thats very bad.
                Now this value can easily be changed in cloudwatch.tf under min/max,
                it wouldnt make sense to have 10 times a day for a place with 5 employees.
            
        Latency:
            Two new metrics for both endpoints above to measure the time it takes for each to complete.
                With @Timed, we can measure the time it takes for the endpoint to complete.
                If it takes too long, there could be an error. But this also depends on the amount of images scanned.

            
        

    B.
    Cloudwatch alarm som skickar varsel till epost.
        - Vid invalid
    Lages via terraform kod som separat modul.
        

    




# NOTES:
    Seems to be a known issue that apprunner does not always manage to find the correct ECR image with the :latest tag.
    A solution seems to be to use rev instead since it will always be the latest one pushed.


    Error:
        Github actions fails because it says apprunner is in a state, how to fix this?
        Seems like it started ot happen after setting apprunner to use :rev instead of :latest
        BUT APPRUNNER GETS DEPLOYED ANYWAYS. WHY?

    Now:
        This commit succeeded, no errors in apprunner regarding cloudwatch: 
            https://github.com/TobiasLiu1990/DevOps-Exam-2023/commit/bce3ee363a3a4ded72574d2a58791a325069aff3

        Last test: re-deploy: 