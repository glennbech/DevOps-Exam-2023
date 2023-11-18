# Oppgave 1 - Kjells Pythoncode

---

### A. SAM & GitHub actions workflow
- [x] Lage S3-bucket med kandidat-nummer.
- [x] Fjern hardkoding av S3 bucket navnet og leser verdien "BUCKET_NAME" fra en miljövariabel.
- [x] Testa API:et
- [x] Opprette en GitHub Actions-workflow for SAM applikationen.
  - [x] Push till main branch: bygge og deploye Lambda-funksjonen.
  - [x] Push till andra branches: kun bygges.
- [x] Forklaring hva sensor må gjöre för att GitHub Actions workflow ska köra.


    BUCKET_NAME now uses envionment variable. This is set to "kandidatnr-2038" as default in template.yaml
    GitHub Actions workflow file builds and/or deploys the sam app depending on push to main or other branches.
    Updated template.yaml to:
      - Use value from Parameter "MyBucketName" to BUCKET_NAME. Default is set to "kandidatnr-2038".
      - Added
          Parameters - To be able to set the BucketName.
          Environment:
            Variables:
              MyBucketName: !Ref MyBucketName
                so AWS lambda can access it.

          The Lambda app could not access the S3 bucket because of permissions. Had to add the following under Policies:
            - AmazonS3ReadOnlyAccess

    How to get GitHub Actions workflow to run
      * Create Access keys in AWS:
        1. Search for IAM.
        2. Go to "My security credentials" on the right panel.
![Oppgave 1 - Create Access keys 1.png](images%2FOppgave%201%20-%20Create%20Access%20keys%201.png)

        3. Click "Create access key".
![Oppgave 1 - Create Access keys 2.png](images%2FOppgave%201%20-%20Create%20Access%20keys%202.png)

        4. Check "Command Line Interface (CLI) and continue.
           Now you have created AWS access keys. It should then look like the image below:
![Oppgave 1 - Create Access keys 3.jpg](images%2FOppgave%201%20-%20Create%20Access%20keys%203.jpg)

        5. Now open a new tab and go to your GitHub repo.
        6. Go to "Settings -> Secrets and variables -> Actions, like the image below:
![Oppgave 1 - Create Access keys 4.png](images%2FOppgave%201%20-%20Create%20Access%20keys%204.png)

        7. Click New repository secret
          Name should be as in the image. Copy the respective keys from AWS that you created and add them here.
            Name = AWS_ACCESS_KEY_ID
            Secret = enter the access key id from AWS IAM
          Do the same with:
            Name = AWS_SECRET_ACCESS_KEY
            Secret = enter the secret access key from AWS IAM
        
      * Now github actions should run.

### B. Docker container
- [x] Lag en Dockerfile som bygger et container image som kör python koden.

---

# Oppgave 2 - Overgang til Java og Spring boot

---

### A. Dockerfile
- [x] Lag en Dockerfile for Java-applikasjonen.
  - [x] Multi stage Dockerfile som kompilerer og kjörer applikationen.

### B. GitHub Actions workflow for container image og ECR
- [x] Lag ny GitHub Actions workflow fil.
  - [x] Lager og publiserer er nytt Container image till et ECR repository vid push till main branch.
  - [x] Kompilere og bygge et nytt container image, men ikke publisere till ECR dersom ikke main branch.
- [x] Selv lage et ECR repository i AWS miljöet.
- [x] Container image skal ha en tag som er.
  - [x] Lik commit-hash som i Git.
  - [x] Sista versjon skal i tillegg ha taggen "latest".
  

    I created a new ECR repository in AWS named: ecr-kandidatnr-2038

---

# Oppgave 3 - Terraform, AWS Apprunner og Infrastruktur som kode

---

### A. Kodeendringer og forbedringer
- [x] Fjern hardkodingen av service_name, slik at du kan bruke ditt kandidatnummer eller noe annet som service navn.
- [x] Se etter andre hard-kodede verdier og se om du kan forbedre kodekvaliteten.
- [x] Reduser CPU til 256, og Memory til 1024 (defaultverdiene er høyere)


    Added some variables that uses variables.tf
      - service_name
      - aws_iam_role_name
      - aws_iam_policy_name
      - port
      - image_tag

    Many of the values here uses a default value, to make it more practical for this exam.
    Otherwise, it would most likely be more practical without them if someone else used it.
    But it can be overridden with -var="<the_variable>=<your_name>"
    These variables are overridden in the GitHub Actions workflow file.
    Port might usually not be changed. But if it is used already, it can be practical to be able to change here.

    Changed ecr from kjell to my ECR.

### B. Terraform i GitHub Actions
- [ ] 
- [ ] 
- [ ] 
- [ ] 
- [ ] 
- [ ] 






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

            

## Oppgave 4 - Feedback

---
### A.



### B. Cloudwatch Alarm og Terraform moduler
- [x] Lag en CloudWatch-alarm.
- [x] Senda varsel till Epost dersom den utlöses.
- [x] Skriv redgjörelse for valget.
- [x] Lages ved hjelp av Terraformkode.
- [x] Skal lages som separat Terraform modul
- [x] Undvik hardkoding.
- [x] Pass på at brukere av modulen ikke må sette mange variabler vid bruk.

---
### Explanation
        The module for the alarm is created as a module in Terraform.
        It uses the same "prefix" variable from the main Terraform code to make it easier to set the correct namings,
        especially the namespace, so the CloudWatch namespace and alarm namespace are the asme one.
        The only variable that needs to be set is the email.
        The prefix has a default value, but can be overridden easily when doing terraform apply.

        The alarm chosen here triggers when the gauge widget reaches 5. It is only set for the scanFullPPE endpoint.
        The logic behind this alarm/gauge is that when a bucket is scanned and the total number of people is found, 
        if 30% of them violates the PPE requirement, the gauge increments by 1.
        If this happens 5 times, the alarm will be triggered.
        This is set to be quite strict, so a lot of email could possibly be sent in a short period of time.
        This is just an example. If there are a lot of people, limit can easily be changed to a higher number before sending an alarm.
        Two places needs to be changed in that case:
            infra/alarm_module/variables.tf:
                Change the default value = 5 under "threshold" to change at what value the alarm should trigger at.

            infra/alarm_module/main.tf:
                evaluation_periods and period, can be modified so it wont be as sensitive.

            infra/cloudwatch.tf:
                Change the "max" value here to modify the maximum value of the gauge widget.

            RekognitionController.java: 
                Change violationLimit. Default is set to 5.
                Change violationPercentage. Default is set to 0.3 (30%).
---




# NOTES:
    Seems to be a known issue that apprunner does not always manage to find the correct ECR image with the :latest tag.
    A solution seems to be to use rev instead since it will always be the latest one pushed.