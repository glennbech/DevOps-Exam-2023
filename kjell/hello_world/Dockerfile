FROM python:3.9-slim
WORKDIR /app
COPY . /spp

RUN pip install --no-cache-dir -r requirements.txt

EXPOSE 80

ENV BUCKET_NAME="should be taken from input"

CMD ["python", "./app.py"]

