import os
import sys

VM = """
yc compute instance create \
 --name {} \
 --labels type=db \
 --metadata db=cockroachdb \
 --zone ru-central1-{} \
 --cores 2 \
 --memory 8 \
 --public-ip \
 --create-boot-disk type=network-nvme,size=16,image-folder-id=standard-images,image-family=ubuntu-1804-lts,auto-delete=true \
 --ssh-key ~/.ssh/cloud.yandex.ru.pub
"""

if __name__ == '__main__':
    print(sys.argv)
    if sys.argv[1] == 'cockroach':
        inst = [('cockroach1', 'a'), ('cockroach2', 'b'), ('cockroach3', 'c')]
        for n, z in inst:
            os.system(VM.format(n, z))
