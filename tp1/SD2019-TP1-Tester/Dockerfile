FROM smduarte/java

MAINTAINER Sérgio Duarte <smd@fct.unl.pt>

WORKDIR /home/sd

COPY target/lib/*.jar /home/sd/ 

COPY target/*.jar /home/sd/

COPY test.sh /

RUN chmod a+x /test.sh

ENTRYPOINT ["/bin/bash", "-e", "/test.sh"]