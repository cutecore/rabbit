docker build -f user.Dockerfile -t user:1.0 .
docker stop user
docker rm user
docker run --name user -d -p 8081:8080  -v /mnt:/mnt --restart=always user:1.0 
