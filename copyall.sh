scp server/target/integracao-server.war integra@192.168.0.159:~/ & scp client/target/integracao-client.war integra@192.168.0.159:~/
scp client/target/integracao-client.war integra@192.168.0.66:~/ 
scp client/target/integracao-client.war integra@192.168.0.2:~/
scp client/target/integracao-client.war integra@192.168.0.160:~/        & scp client/target/integracao-client.war integra@192.168.0.163:~/
scp client/target/integracao-client.war integra@192.168.0.161:~/        
scp client/target/integracao-client.war integra@192.168.0.164:~/
scp client/target/integracao-client.war integra@192.168.0.162:~/        
scp client/target/integracao-client.war integra@192.168.0.165:~/

echo Restarting 159
ssh integra@192.168.0.159 ./deploy.sh > /dev/null

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

echo 
echo Restarting 164
ssh integra@192.168.0.164 ./deploy.sh

echo
echo Restarting 165
ssh integra@192.168.0.165 ./deploy.sh



scp -q client/target/integracao-client.war integra@192.168.0.123:~/     


ssh integra@192.168.0.2 ./deploy.sh        > /dev/null
ssh integra@192.168.0.123 ./deploy.sh > /dev/null


