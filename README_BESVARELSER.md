## Oppgave 1
    * Changed name for hello_world folder to ppe_detection
    * Updated template.yaml to:
        - Use value from Parameter "MyBucketName" to BUCKET_NAME. Default is set to "kandidatnr-2038".
        - Added 
            Environment:
              Variables:
                MyBucketName: !Ref MyBucketName
          so AWS lambda can access it.
        













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