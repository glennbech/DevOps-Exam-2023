## Oppgave 1
    A.
    Fixed variables, deployed app to aws with sam deploy --guided.
        * Changed name for hello_world folder to ppe_detection
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
        * First you must create a S3 bucket and a CloudFormation stack. This can be done in AWS or by using the code in this project
          Outside Cloud9 you might need AWS CLI and sam CLI installed.
          In Cloud9:
            Navigate to where the template.yaml is for sam.
            In this case:
                1. cd DevOps_Exam_2023/kjell
                2. Run sam deploy --guided
                3. Enter name of your sam app
                4. Enter name of your S3 bucket. (Currently this is set to 2038 by default)
                    These two values are used in .github/workflows/sam-deployment.yaml
                5. If not using default, change the name of "S3_BUCKET_NAME" and "SAM_APP" in sam-deployment.yaml
                6. Now you need to create access keys in AWS.
                    In AWS, search for IAM -> My security credentials -> Create Access key.
                    Check "Command Line Interface (CLI) and continue.
                    Open a new tab and go to your Github repo and go to Settings -> Secret and variables -> Actions
                    Click "New Repository secret"
                        Name = AWS_ACCESS_KEY_ID
                        Secret = enter the access key id from AWS IAM
                    Do the same but:
                        Name = AWS_SECRET_ACCESS_KEY
                        Secret = enter the secret access key from AWS IAM
        
        Now everything should be able to run after the configuation here.
                    
    B.
    









# Notes
    * AWS Rekognition for The Personal Protective Equipment (PPE) detection
        - Bilder
        - Videor
        - Text

    * Utstyr att se på
        - Hjelmer
        - Vernebriller
        - Hansker
        - Verneklär


    * Upload images or videos
    * Get response back on where/if PPE is found.