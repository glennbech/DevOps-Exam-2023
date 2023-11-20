# Oppgave 1 - Kjells Pythoncode

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

### A. Kodeendringer og forbedringer
- [x] Fjern hardkodingen av service_name, slik at du kan bruke ditt kandidatnummer eller noe annet som service navn.
- [x] Se etter andre hard-kodede verdier og se om du kan forbedre kodekvaliteten.
- [x] Reduser CPU til 256, og Memory til 1024 (defaultverdiene er høyere).


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
- [x] Utvid din GitHub Actions workflow som lager et Docker image, til også å kjøre terraformkoden.
- [x] På hver push til main, skal Terraformkoden kjøres etter jobber som bygger Docker container image.
  - [x] Du kan bruke samme S3 bucket som vi har brukt til det formålet i øvingene.
- [x] Du må lege til Terraform provider og backend-konfigurasjon. Dette har Kjell glemt. 
- [x] Beskriv også hvilke endringer, om noen, sensor må gjøre i sin fork, GitHub Actions workflow eller kode for å få denne til å kjøre i sin fork.


    Using the S3 bucket from class: pgr301-2021-terraform-state
    State file: 2038-apprunner-new-state.state
    
    Assuming that you have AWS Secrets correctly setup in GitHub, if you are running your own you should have:
      Your own ECR repository
      Change ECR repository in main.tf to your under "image_identifier"
      Change S3 Bucket in provider.tf (if not using the provided one)

      Apprunner, IAM Role and IAM Policy needs to have a unique name.
        Change the following env. in the workflow file:
          env:
            ECR_REPO: <your-ecr-repo>
            SERVICE_NAME: <you-appr-name>
            AWS_IAM_ROLE_NAME: <you-role-name>
            AWS_IAM_POLICY_NAME: <you-policy-name>
            PORT: <port>, (could most stay as 8080, but added this in case)
      
      Also check so the ECR is correct under the step: "Login to AWS ECR"

      Then it should work fine.

### NOTES:
    * Sometimes i get a deploymeny error regarding port used. But i cant find the reason why as it should be free. When re-running it, everything works.

    Seems to be a known issue that apprunner does not always manage to find the correct ECR image with the :latest tag.
    A solution seems to be to use rev instead since it will always be the latest one pushed.




---




# Oppgave 4 - Feedback

### A. Utvid applikasjonen og legg inn "Måleinstrumenter"
- [x] Lag minst et nytt endepunkt.
- [x] Utvid gjerne også den eksisterende koden med mer funksjonalitet.
- [x] Gjør nødvendige endringer i Java-applikasjonen til å bruke Micrometer rammeverket for Metrics.
  - [x] Konfigurer for leveranse av Metrics til CloudWatch.
- [x] Dere skal skrive en kort begrunnelse for hvorfor dere har valgt måleinstrumentene dere har gjort, og valgene må være relevante.
- [x] Minst tre ulike måleinstrumenter.
  - [x] Widget 1 - 
  - [x] Widget 2 - .
  - [x] Widget 3 - Average response time for Single PPE-piece scan.
  - [x] Widget 4 - Average response time for Full PPE scan.
![Oppgave 3 - CloudWatch Dashboard - Widgets.png](images%2FOppgave%203%20-%20CloudWatch%20Dashboard%20-%20Widgets.png)


    New endpoint: scanFullPPE:
      Scans head, face and both hands for PPE.

    Widget choice:
      Widget 1 - Face PPE Detection analysis:
        This widget uses the scanForPPE endpoint.
        The graph shows the number of violations and number of valid PPE-detections in one graph.
        It could be relevant for statistics to measure how many that fails, which would probably need some changes in a system to improve this.
        Having both in one graph makes it easier to see this.


      Widget 2 - Full PPE Violation counter:
        This widget is only for the scanFullPPE endpoint, which considers face, head and both hands.
        The condition for this gauge to update is:
          After finding the total numbers of people in all images in a bucket, if 30% of them violates the PPE detection,
          the gauge will increment by 1.
          When the gauge reaches 5, it will trigger an alarm.
          After reaching 6, it will reset to 1.
            I chose to not reset when reaching 5 because then the gauge would never show 5. It would go to 4 and then to 0.
![Oppgave 3-4 - Max value 5.png](images%2FOppgave%203-4%20-%20Max%20value%205.png)

        The idea is that this could be used for very strict environments where PPE is very important, which means 
        compliance must be good. So for example if violations happen x times a day, thats very bad.
        Limit is set to 5 in this exam, but this is a very small number, which likely spams the email inbox.
        This is easily changed, more in B. about alarms.


      Widget 3 - Average response time for Single PPE-piece scan
        Shows the response time for calling the scanForPPE endpoint.
        It can be important to know the response time because of performance-checking and debugging.
        For example stress test to see how well it handles multiple calls. If it runs too slow, it might need improvement.
        If it takes very very long, it might point to some error.


      Widget 4 - Average response time for Full PPE scan
        Same as described in Widget 3.
        Additionally, it also shows that a full PPE scan does take longer than just checking face.


    I used applicaton.properties for an envionment variable to set the cloudwatch.namespace in MetricsConfig.java.
    Since it didnt otherwise contain secret/sensitive data, its pushed to GitHub.
    ***
    IMPORTANT! The cloudwatch namespace must be same here and in the Terraform code to be able to connect and send data correctly.
    ***


### B. Cloudwatch Alarm og Terraform moduler
- [x] Lag en CloudWatch-alarm som sender et varsel på Epost dersom den utløses.
- [x] Dere velger selv kriteriet for kriterier til at alarmen skal løses ut, men dere må skrive en kort redgjørelse for valget.
- [x] Alarmen skal lages ved hjelp av Terraformkode.
- [x] Koden skal lages som en separat Terraform modul.
- [x] Legg vekt på å unngå hardkoding av verdier i modulen for maksimal gjenbrukbarhet.
- [x] Pass samtidig på at brukere av modulen ikke må sette mange variabler når de inkluderer den i koden sin

  
    The alarm is created in Terraform as a module. It uses the same "prefix" variable from the "main" Terraform code.
    This makes it easier to maintain the requirement that Cloudwatch and the alarm uses the same name for the namespace.
    The only variable that needs input is the email for the alarm.
![Oppgave 4 - Alarm.png](images%2FOppgave%204%20-%20Alarm.png)

    When running terraform apply with the new alarm module, you will get an email asking you to subscribe to it.
    You must accept if you want email notifications from the alarm. See image below.
![Oppgave 4 - Email notification for Alarm - 1.png](images%2FOppgave%204%20-%20Email%20notification%20for%20Alarm%20-%201.png)
![Oppgave 4 - Accepted Alarm subscription - 2.png](images%2FOppgave%204%20-%20Accepted%20Alarm%20subscription%20-%202.png)

    The alarm uses the gauge widget and triggers when it reaches 5. An email will then be sent.
![Oppgave 4 - Alarm triggered - 3.png](images%2FOppgave%204%20-%20Alarm%20triggered%20-%203.png)

    The condition for it to trigger is described in Part A. under Widget 2.
    If you want to change the condition for the trigger:
      infra/alarm_module/variables.tf:
        Change the default value under "threshold". Default is 5.

      infra/alarm_module/main.tf:
        evaluation_periods and period, can be modified so it wont be as sensitive as the default values currently.

      infra/cloudwatch.tf:
        For the gauge: Change the "max" value here accordingly. Default is 5.

      src/main/java/com.example.s3rekognition/controller/RekognitionController.java:
        Change violationLimit accordingly. Default is set to 5.
        Change violationPercentage. Default is set to 0.3 (30%).




---




# Oppgave 5 - Drøfteoppgaver

### A. Kontinuerlig Integrering
Forklar hva kontinuerlig integrasjon (CI) er og diskuter dens betydning i utviklingsprosessen. I ditt svar,
vennligst inkluder:
- [x] En definisjon av kontinuerlig integrasjon.
- [x] Fordelene med å bruke CI i et utviklingsprosjekt - hvordan CI kan forbedre kodekvaliteten og effektivisere utviklingsprosessen.
- [x] Hvordan jobber vi med CI i GitHub rent praktisk? For eskempel i et utviklingsteam på fire/fem utivklere?


    Continuous integration, as the name suggests, is the practice of frequent integrations of new code (updates for example)
    to the main branch.
    This helps avoiding big changes which can cause harder times to debug eventual errors.
    Many and smaller commits and merges of updates to the main branch introduces small changes at a time, which will be much easier to debug if error happens.
    It is also easier to do changes upon.
    For example if many developers work on the same project, if huge code changes are commited each time, it will most likely lead to conflicts.
    Code might not work with each other, people have spent a lot of time on their code which in the end might need to be fully changed.
    If each of them commits and merge small changes each time, less time will be spent on checking if it works.
    This also makes it easier for the team to see who works with what.

    Having that said, CI should be automated to have checks.
    For example:
      - that nobody can directly commit to main but only to branches.
      - Should contain tests.
      - Merges should be reviewed by others on the team.

    By having these steps, it is fast and easy to get feedback on the code that you want to merge. For example if tests fail.


### B. Sammenligning av Scrum/Smidig og DevOps fra et Utviklers Perspektiv
1. Scrum/Smidig Metodikk:
- [x] Beskriv kort, hovedtrekkene i Scrum metodikk og dens tilnærming til programvareutvikling.
- [x] Diskuter eventuelle utfordringer og styrker ved å bruke Scrum/Smidig i programvareutviklingsprosjekter.


    Scrum is used for agile methodology which have been shown to work well for software develpoment.
    It is an iterative process to progressively work towards the projects goal.
    A product backlog which contain a list of tasks to be done. This can change from sprint to sprint by adding/removing
    tasks.

    In a scrum there are different roles:
      - Product owner.
      - Scrum master.
      - Development team.

    Scrum/Agile is a very efficient way of working.
    Strengths:
      - Time-efficient:
          Tasks are divided into smaller tasks, which makes it easier to work with.
          Identifying if things wont work out will save you time.
      - Flexibility:
          Allows of changes if a plan does not work. If a planned sprint does not work out as planned, 
          adjustments can be made for the next sprint and so on.
      - Communication:
          It is normal to have a longer stand-up at the beginning of the sprint to discuss the plan for the sprint.
          Then normal to have a shorter daily sprint to catch up on what the team has done.
          A retrospective is held at the end of the sprint to discuss how things have gone, if it worked out, future improvements etc.
          This leads to transparency. And it usually promotes everyone on the team have a say and contribute.

    Challenges:
      - Communcation:
          Using Scrum/Agile reguires a good communcation between the team members. Scrum/Agile usually promotes input from
          everyone on the team. But some might be more quiet than others which might have more to say but does not "find" the oppotunity.
          Important to have everyone have their say.
          Having a good team leader/scrum master would help with this.
      - Commitment:
          The team members are dependant on each other for it to work, which requires responsibility from everyone.


2. DevOps Metodikk:
- [x] Forklar grunnleggende prinsipper og praksiser i DevOps, spesielt med tanke på integrasjonen av utvikling og drift.
- [x] Analyser hvordan DevOps kan påvirke kvaliteten og leveransetempoet i programvareutvikling.
- [x] Reflekter over styrker og utfordringer knyttet til bruk av DevOps i utviklingsprosjekter.


    DevOps encourages close working culture between developers and operations to make the development process as efficient as possible.
    This means that both shares the responsiblity for everything.
    By having this, it ensures a smoother and more efficient development process and supports continuous delivery of software.

    The three main princples are:
      Flow: 
        CI as explained above is a big part of this.
        Pipelining and automation is important to identify errors fast to keep improving the software.
        Continuous delivery is another aspect that ensures that the software is always ready for deployment and release.

      Feedback:
        This principle puts emphasis on information from and to developers and operations.
        Having a feedback loop allows for fast identification of issues which allows for fast debugging and fixes.
        By using for example telemetry and surveillance, logging, metrics helps with this.
        A/B-testing is also used for feedback, for example testing a new desgin on a webpage. It allows us to see whichever is better when tested on
        public opinion.

      Continuous improvement:
        Encourages a culture to learn and improve.
        Learning from mistakes for example or trying new things to solve a problem more efficient will all lead to a 
        better software development process.

    By utilizing the DevOps principles, it helps ensuring software with good quality and quick releases.
    Flow helps with quick and small improvements for releases and Feedback helps with identifying errors fast.

    Strengths:
      - Efficiency:
          CI/CD ensures a fast and efficient software delivery.
          Automations such as tests ensures a good quality of the software.
          Feedback provides fast responsitivity for errors.
          
    Challenges:
      - Communcation:
          Can be a big cultural change of working environment. Requires good teamwork and understanding of DevOps principles to work.
          This can be quite complex to get a hold on at first.


3. Sammenligning og Kontrast:
- [x] Sammenlign Scrum/Smidig og DevOps i forhold til deres påvirkning på programvarekvalitet og leveransetempo.
- [x] Diskuter hvilke aspekter ved hver metodikk som kan være mer fordelaktige i bestemte utviklingssituasjoner.


    Many parts of Scrum/Agile and DevOps are similar.
    Both put emphasis on software quality by for example incorporating testing. Feedback is also used to identify errors or improvements.
    Both also encourages small commits and merges to avoid issues. Both has transparnecy so everyone in the team can see
    what is being worked on.
    Changes and adjustments can be done throughout the process to deliver the best product possible.

    Scrum/Agile emphasises more on the iterative process of product development.
    Delivery time is considered differently since sprints are done in periods (ex. 3 weeks). After that, the product can be shown if ready.
    Feedback here is usually through people like PO, user testing and from team members. In DevOps it is usually automated
    to identify issues fast by using for example telemetry.
    DevOps puts more emphasis on the automating part of development process of software delivery. 
    Both methodologies aim to improve software quality and delivery time but covers this from different angles.

    In the beginning of a project, during the development process it would make more sense to do Scrum/Agile.
    Especially if the end goal isnt very clearly defined yet or if frequent changes are expected.
    In a more certain project that needs to be reliable and available like a bank application, DevOps would be preferred.
  
    For example if a project is about developing a creative suggestion AI for furnitures, in the start and early stages,
    using Scrum/Agile would fit better because it allows for changes and flexibility.
    The start phase would include a lot of testing different technologies and reseach. Planning would most likely change a lot.
    Then it would require feedback in the form of user testing to see what people think of it, how easy it is to use etc.
    Rinse and repeat till the product seems more satisfactory.
    Then further down when the product gets more defined and is ready for deployment it would be a good idea to switch to DevOps.
    CI/CD helps with efficient improvements and deployments.
    Feedback provides objective data in the form of numbers. Automated feedback such as telemetry, monitoring and metrics can identify
    areas to improve or identify issues.




### C. Det Andre Prinsippet - Feedback

    For a new functionality i would like to perform A/B testing to see how well users perceive it.
    It would be interesting to use telemetry and monitoring for collecting data on user interaction such as:
      - Interaction frequency.
      - Time spent using it.
      - User experience.
      - User issues.

    Then also use specific metrics for performance measuring such as response time, up-time and number of times users
    found/clicked on the new functionality. I would also use alarms for metrics for safety purposes.
    For example if response time is high and goes over a threshold, it could mean that there is an issue.

    With the measurements it would be possible to find bugs, issues and areas that needs improvements.
    Logging could then be used for troubleshooting.

    In the later stages after A/B testing, when functionality seems satisfactory, i would consider rolling it out for everyone to try out.
    Telemetry and monitoring, metrics and logging would all still be relevant to track long-term use of the functionality.
    When fully incorporated and working well, specific feedback might not be needed anymore, but shifts back to the
    application as a whole (assuming the application has a sufficient and effective feedback system).