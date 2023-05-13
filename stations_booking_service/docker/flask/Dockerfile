FROM python:3.8-slim-buster

WORKDIR /var/www/backend/python

RUN apt-get update && apt-get install -y python3 python3-pip
RUN pip3 install flask flask_cors openrouteservice

COPY . .

CMD [ "python3", "-m" , "flask", "run", "--host=0.0.0.0"]