scp server/target/integracao-server.war integra@192.168.0.159:~/
scp client/target/integracao-client.war integra@192.168.0.159:~/
scp client/target/integracao-client.war integra@192.168.0.66:~/
scp client/target/integracao-client.war integra@192.168.0.2:~/
scp client/target/integracao-client.war integra@192.168.0.123:~/
scp client/target/integracao-client.war integra@192.168.0.157:~/
ssh integra@192.168.0.159 ./deploy.sh
ssh integra@192.168.0.2 ./deploy.sh
ssh integra@192.168.0.157 ./deploy.sh
ssh integra@192.168.0.66 ./deploy.sh
ssh integra@192.168.0.123 ./deploy.sh

