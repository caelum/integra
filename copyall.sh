scp server/target/integracao-server.war integra@192.168.0.159:~/ & scp client/target/integracao-client.war integra@192.168.0.159:~/
scp client/target/integracao-client.war integra@192.168.0.66:~/        & scp client/target/integracao-client.war integra@192.168.0.2:~/



 ssh integra@192.168.0.159 ./deploy.sh & ssh integra@192.168.0.66 ./deploy.sh

echo 159 restarted and 66 restarted

scp client/target/integracao-client.war integra@192.168.0.123:~/       & scp client/target/integracao-client.war integra@192.168.0.157:~/





 ssh integra@192.168.0.2 ./deploy.sh      &  ssh integra@192.168.0.123 ./deploy.sh
echo 2 restarted and 123 restarted

ssh integra@192.168.0.157 ./deploy.sh
echo 157 restarted

