docker run --rm --volumes-from kibana -v $(pwd):/backup tap:kibana tar cvf /backup/kibanaBK.tar ./
docker run --rm --volumes-from elasticsearch -v $(pwd):/backup tap:elasticsearch tar cvf /backup/elasticsearchBK.tar ./
