scp server/target/integracao-server.war integra@192.168.0.159:~/ 
scp client/target/integracao-client.war integra@192.168.0.159:~/

echo Restarting 159
ssh integra@192.168.0.159 ./deploy.sh > /dev/null

scp integra@192.168.0.159:~/integracao-client.war integra@192.168.0.66:~/

echo Restarting 66
ssh integra@192.168.0.66 ./deploy.sh > /dev/null

scp integra@192.168.0.159:~/integracao-client.war integra@192.168.0.2:~/
scp integra@192.168.0.159:~/integracao-client.war integra@192.168.0.160:~/        & scp client/target/integracao-client.war integra@192.168.0.163:~/
scp integra@192.168.0.159:~/integracao-client.war integra@192.168.0.161:~/        
scp integra@192.168.0.159:~/integracao-client.war integra@192.168.0.164:~/
scp integra@192.168.0.159:~/integracao-client.war integra@192.168.0.162:~/        
scp integra@192.168.0.159:~/integracao-client.war integra@192.168.0.165:~/
scp integra@192.168.0.159:~/integracao-client.war integra@192.168.0.123:~/


echo Restarting 66
ssh integra@192.168.0.66 ./deploy.sh > /dev/null

echo Restarting 160
ssh integra@192.168.0.160 ./deploy.sh > /dev/null

echo Restarting 161
ssh integra@192.168.0.161 ./deploy.sh > /dev/null

echo Restarting 162
ssh integra@192.168.0.162 ./deploy.sh > /dev/null

echo Restarting 163
ssh integra@192.168.0.163 ./deploy.sh

echo Restarting 164
ssh integra@192.168.0.164 ./deploy.sh > /dev/null

echo Restarting 165
ssh integra@192.168.0.165 ./deploy.sh > /dev/null

echo Restarting 2
ssh integra@192.168.0.2 ./deploy.sh        > /dev/null

echo Restarting 123
ssh integra@192.168.0.123 ./deploy.sh > /dev/null


