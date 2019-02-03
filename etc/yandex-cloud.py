import os
import sys

VM = """
yc compute instance create \
 --name {} \
 --labels type=db \
 --metadata db=vm \
 --zone ru-central1-{} \
 --cores 2 \
 --memory 8 \
 --public-ip \
 --create-boot-disk type=network-nvme,size=16,image-folder-id=standard-images,image-family=ubuntu-1804-lts,auto-delete=true \
 --ssh-key ~/.ssh/cloud.yandex.ru.pub
"""

if __name__ == '__main__':
    if sys.argv[1] == 'vm':
        inst = [('vm1', 'a'), ('vm2', 'b'), ('vm3', 'c')]
        for n, z in inst:
            os.system(VM.format(n, z))
