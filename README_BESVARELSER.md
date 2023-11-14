## Oppgave 1
    Fixed variables, deployed app to aws with sam deploy --guided.
        * Changed name for hello_world folder to ppe_detection
        * Updated template.yaml to:
            - Use value from Parameter "MyBucketName" to BUCKET_NAME. Default is set to "kandidatnr-2038".
            - Added 
                Environment:
                  Variables:
                    MyBucketName: !Ref MyBucketName
              so AWS lambda can access it.
        
    Create Github Actions workflow for sam.
        * Added workflows for Github Actions.
            - Added envs to repo with aws secrets.
            - Push to main: should build and deploy             - OK
            - Push to other branches: should only build         - OK

    How to get Github Actions workflow to run
        * First you must create a S3 bucket and a CloudFormation stack. This can be done in AWS or by using the code in this project
          Outside Cloud9 you might need AWS cli and sam cli installed.
          In Cloud9:
            1. Run sam deploy --guided
            2. Enter name of your sam app
            3. Enter name of your S3 bucket. (Currently this is set by default to 2038)
                These two values are used in .github/workflows/sam-deployment.yaml
            4. Change variable name of x, y
            5. Get secrets from AWS and add to your Github repo in settings
            
            









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