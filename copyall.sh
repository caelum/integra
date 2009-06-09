scp -q server/target/integracao-server.war integra@192.168.0.159:~/ & scp -q client/target/integracao-client.war integra@192.168.0.159:~/
scp -q client/target/integracao-client.war integra@192.168.0.66:~/        & scp -q client/target/integracao-client.war integra@192.168.0.2:~/
scp -q client/target/integracao-client.war integra@192.168.0.160:~/        & scp -q client/target/integracao-client.war integra@192.168.0.163:~/
scp -q client/target/integracao-client.war integra@192.168.0.161:~/        
scp -q client/target/integracao-client.war integra@192.168.0.164:~/
scp -q client/target/integracao-client.war integra@192.168.0.162:~/        
scp -q client/target/integracao-client.war integra@192.168.0.165:~/



ssh integra@192.168.0.159 ./deploy.sh
ssh integra@192.168.0.66 ./deploy.sh

echo Restarting 160
ssh integra@192.168.0.160 ./deploy.sh

echo Restarting 161
ssh integra@192.168.0.161 ./deploy.sh

echo Restarting 162
ssh integra@192.168.0.162 ./deploy.sh

ssh integra@192.168.0.163 ./deploy.sh
ssh integra@192.168.0.164 ./deploy.sh
ssh integra@192.168.0.165 ./deploy.sh



scp -q client/target/integracao-client.war integra@192.168.0.123:~/     





ssh integra@192.168.0.2 ./deploy.sh      
ssh integra@192.168.0.123 ./deploy.sh


